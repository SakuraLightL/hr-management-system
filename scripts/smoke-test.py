#!/usr/bin/env python3
"""Exercise a real LOCAL session on an isolated, loopback-only demo database.

Python 3 standard library only. Never prints credentials, cookies or CSRF tokens.
Run 'flow', restart the app, then run 'persisted' and 'cleanup'.
"""
import argparse
import http.cookiejar
import json
import os
from pathlib import Path
import re
import secrets
import sys
import time
from html.parser import HTMLParser
from urllib.error import HTTPError, URLError
from urllib.parse import urlencode, urlparse
from urllib.request import Request, build_opener, HTTPCookieProcessor, HTTPRedirectHandler


class NoRedirect(HTTPRedirectHandler):
    def redirect_request(self, req, fp, code, msg, headers, newurl):
        return None


class Tokens(HTMLParser):
    def __init__(self, html):
        super().__init__()
        self.token = None
        self.header = "X-CSRF-TOKEN"
        self.feed(html)

    def handle_starttag(self, tag, attrs):
        attrs = dict(attrs)
        if tag == "input" and attrs.get("name") == "_csrf":
            self.token = attrs.get("value")
        if tag == "meta" and attrs.get("name") == "_csrf":
            self.token = attrs.get("content")
        if tag == "meta" and attrs.get("name") == "_csrf_header":
            self.header = attrs.get("content")


class Client:
    def __init__(self, base):
        self.base = base
        self.opener = build_opener(HTTPCookieProcessor(http.cookiejar.CookieJar()), NoRedirect())
        self.csrf = None

    def request(self, method, path, *, form=None, data=None, csrf=False):
        headers = {}
        body = None
        if form is not None:
            form = dict(form)
            if csrf:
                form["_csrf"] = self.csrf.token
            body = urlencode(form).encode()
            headers["Content-Type"] = "application/x-www-form-urlencoded"
        if data is not None:
            body = json.dumps(data).encode()
            headers["Content-Type"] = "application/json"
        if csrf and form is None:
            headers[self.csrf.header] = self.csrf.token
        request = Request(self.base + path, data=body, headers=headers, method=method)
        try:
            response = self.opener.open(request, timeout=10)
        except HTTPError as error:
            response = error
        with response:
            return response.code, response.headers, response.read().decode("utf-8")

    def login(self, username, password):
        status, _, html = self.request("GET", "/login")
        require(status == 200, "login page")
        self.csrf = Tokens(html)
        require(bool(self.csrf.token), "login CSRF token")
        status, headers, _ = self.request("POST", "/login", csrf=True,
                                         form={"username": username, "password": password})
        require(status == 302 and urlparse(headers.get("Location", "")).path == "/dashboard",
                "LOCAL login establishes a session")
        status, _, html = self.request("GET", "/employees")
        require(status == 200, "authenticated employee page")
        self.csrf = Tokens(html)
        require(bool(self.csrf.token), "post-login CSRF token")


def require(condition, label):
    if not condition:
        raise AssertionError(label)


def expect(client, method, path, status, **kwargs):
    actual, _, body = client.request(method, path, **kwargs)
    require(actual == status, f"{method} {path}: expected {status}, got {actual}")
    return body


def wait_ready(client, username, password):
    for _ in range(90):
        try:
            if client.request("GET", "/login")[0] == 200:
                # The HTTP port can open before the bootstrap ApplicationRunner finishes.
                client.login(username, password)
                return
        except (URLError, OSError, AssertionError):
            pass
        time.sleep(2)
    raise AssertionError("app and administrator login did not become ready in 180 seconds")


def flow(admin, base, state_path):
    anonymous = Client(base)
    expect(anonymous, "GET", "/api/employees", 401)
    expect(admin, "POST", "/api/employees", 403,
           data={"name": "Smoke", "email": "smoke@example.test", "position": "Engineer"})
    suffix = secrets.token_hex(6)
    employee = {"name": "Smoke-" + suffix, "email": "smoke-" + suffix + "@example.test",
                "position": "Engineer", "employmentStatus": "ACTIVE",
                "employmentType": "PERMANENT", "hireDate": "2026-01-01"}
    invalid = dict(employee, employmentStatus="RETIRED")
    expect(admin, "POST", "/api/employees", 400, csrf=True, data=invalid)
    record = json.loads(expect(admin, "POST", "/api/employees", 201, csrf=True, data=employee))["data"]
    employee_id = record["id"]
    expect(admin, "POST", "/api/employees", 409, csrf=True, data=employee)
    results = json.loads(expect(admin, "GET", "/api/employees?" + urlencode({"name": employee["name"]}), 200))
    require(any(e["id"] == employee_id for e in results["data"]["content"]), "prefix search finds new employee")
    retired = dict(employee, employmentStatus="RETIRED", retirementDate="2026-10-01")
    updated = json.loads(expect(admin, "PUT", f"/api/employees/{employee_id}", 200, csrf=True, data=retired))["data"]
    require(updated["employmentStatus"] == "RETIRED", "HR status update")
    # Create the read-only account through the same form administrators use.
    reader_name = "smoke-reader-" + suffix
    reader_password = secrets.token_urlsafe(24)
    expect(admin, "POST", "/users/save", 302, csrf=True,
           form={"username": reader_name, "password": reader_password, "role": "USER", "provider": "LOCAL"})
    html = expect(admin, "GET", "/users?size=100&sort=id,desc", 200)
    reader_id = None
    for row in re.findall(r"<tr\b[^>]*>(.*?)</tr>", html, re.S):
        if reader_name in row:
            match = re.search(r"/users/edit/(\d+)", row)
            if match:
                reader_id = int(match.group(1))
                break
    require(reader_id is not None, "reader account created")
    reader = Client(base)
    reader.login(reader_name, reader_password)
    expect(reader, "GET", "/api/employees", 200)
    expect(reader, "POST", "/api/employees", 403, csrf=True, data=employee)
    expect(reader, "PUT", f"/api/employees/{employee_id}", 403, csrf=True, data=employee)
    expect(reader, "DELETE", f"/api/employees/{employee_id}", 403, csrf=True)
    expect(reader, "GET", "/users", 403)
    expect(admin, "GET", f"/employees/delete/{employee_id}", 405)
    expect(admin, "POST", "/logout", 403)  # No CSRF token.
    expect(admin, "POST", "/logout", 302, csrf=True)
    expect(admin, "GET", "/api/employees", 401)
    state_path.parent.mkdir(parents=True, exist_ok=True)
    state_path.write_text(json.dumps({"employee_id": employee_id, "reader_id": reader_id,
                                      "name": employee["name"], "email": employee["email"]}))
    print("PASS: real sessions, CSRF, ADMIN/USER boundary, CRUD, HR validation and logout")


def persisted(admin, state):
    record = json.loads(expect(admin, "GET", f"/api/employees/{state['employee_id']}", 200))["data"]
    require(record["name"] == state["name"] and record["email"] == state["email"], "record identity after restart")
    require(record["employmentStatus"] == "RETIRED" and record["retirementDate"] == "2026-10-01",
            "HR fields persisted after restart")
    print("PASS: persisted employee and LOCAL administrator login")


def cleanup(admin, state, state_path):
    expect(admin, "DELETE", f"/api/employees/{state['employee_id']}", 200, csrf=True)
    expect(admin, "GET", f"/api/employees/{state['employee_id']}", 404)
    results = json.loads(expect(admin, "GET", "/api/employees?" + urlencode({"name": state["name"]}), 200))
    require(results["data"]["totalElements"] == 0, "soft-deleted employee excluded from search")
    expect(admin, "POST", f"/users/delete/{state['reader_id']}", 302, csrf=True, form={})
    state_path.unlink()
    print("PASS: soft deletion, search exclusion and test-account cleanup")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("phase", choices=["flow", "persisted", "cleanup"])
    parser.add_argument("--state", type=Path, default=Path("target/smoke-state.json"))
    args = parser.parse_args()
    base = os.environ.get("SMOKE_BASE_URL", "http://127.0.0.1:8080").rstrip("/")
    parsed = urlparse(base)
    require(parsed.scheme == "http" and parsed.hostname in {"127.0.0.1", "localhost", "::1"}
            and not parsed.username and not parsed.password and not parsed.path and not parsed.query
            and not parsed.fragment, "smoke test is restricted to a local isolated demo")
    username = os.environ.get("SMOKE_ADMIN_USERNAME", "admin")
    password = os.environ.get("SMOKE_ADMIN_PASSWORD")
    require(bool(password), "set SMOKE_ADMIN_PASSWORD for the test administrator")
    admin = Client(base)
    wait_ready(admin, username, password)
    if args.phase == "flow":
        require(not args.state.exists(), "finish the previous run or use a fresh isolated database")
        flow(admin, base, args.state)
    else:
        state = json.loads(args.state.read_text())
        if args.phase == "persisted":
            persisted(admin, state)
        else:
            cleanup(admin, state, args.state)


if __name__ == "__main__":
    try:
        main()
    except (AssertionError, OSError, ValueError) as error:
        print(f"FAIL: {error}", file=sys.stderr)
        sys.exit(1)
