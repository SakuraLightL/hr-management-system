"""Offline checks for the smoke client's HTTP mechanics; these do not test Spring."""
import importlib.util
from pathlib import Path
import threading
import unittest
from unittest.mock import patch
from http.server import BaseHTTPRequestHandler, HTTPServer
from urllib.parse import parse_qs

spec = importlib.util.spec_from_file_location("smoke", Path(__file__).with_name("smoke-test.py"))
smoke = importlib.util.module_from_spec(spec)
spec.loader.exec_module(smoke)


class ClientTest(unittest.TestCase):
    def test_real_http_login_retains_rotated_cookie_and_reads_new_csrf(self):
        observed = {}

        class Handler(BaseHTTPRequestHandler):
            def log_message(self, *args):
                pass

            def do_GET(self):
                if self.path == "/login":
                    self.send_response(200)
                    self.send_header("Set-Cookie", "SESSION=before; Path=/")
                    self.end_headers()
                    self.wfile.write(b'<input value="login-token" name="_csrf" type="hidden">')
                elif self.path == "/employees" and "SESSION=after" in self.headers.get("Cookie", ""):
                    self.send_response(200)
                    self.end_headers()
                    self.wfile.write(b'<meta content="session-token" name="_csrf"><meta content="X-CSRF-TOKEN" name="_csrf_header">')
                else:
                    self.send_response(403)
                    self.end_headers()

            def do_POST(self):
                observed["form"] = parse_qs(self.rfile.read(int(self.headers["Content-Length"])).decode())
                observed["cookie"] = self.headers.get("Cookie")
                self.send_response(302)
                self.send_header("Location", "/dashboard")
                self.send_header("Set-Cookie", "SESSION=after; Path=/")
                self.end_headers()

        server = HTTPServer(("127.0.0.1", 0), Handler)
        worker = threading.Thread(target=server.serve_forever, daemon=True)
        worker.start()
        try:
            client = smoke.Client(f"http://127.0.0.1:{server.server_port}")
            client.login("test-user", "test-password")
            self.assertEqual(observed["cookie"], "SESSION=before")
            self.assertEqual(observed["form"]["_csrf"], ["login-token"])
            self.assertEqual(client.csrf.token, "session-token")
            self.assertEqual(client.csrf.header, "X-CSRF-TOKEN")
        finally:
            server.shutdown()
            server.server_close()
            worker.join()

    def test_readiness_retries_connection_reset_and_bootstrap_login_race(self):
        class StartingClient:
            requests = 0
            logins = 0

            def request(self, method, path):
                self.requests += 1
                if self.requests == 1:
                    raise ConnectionResetError("starting")
                return 200, {}, ""

            def login(self, username, password):
                self.logins += 1
                if self.logins == 1:
                    raise AssertionError("administrator is not created yet")

        client = StartingClient()
        with patch.object(smoke.time, "sleep") as sleep:
            smoke.wait_ready(client, "test-admin", "test-password")
        self.assertEqual(client.requests, 3)
        self.assertEqual(client.logins, 2)
        self.assertEqual(sleep.call_count, 2)


if __name__ == "__main__":
    unittest.main()
