document.addEventListener("DOMContentLoaded", async () => {
    await loadDepartments();
    await loadEmployees();
  });

  function showLoading(flag) {
    const loading = document.getElementById("loading");
    loading.style.display = flag ? "flex" : "none";
  }

  function showToast(message) {
    const toast = document.getElementById("toast");
    toast.textContent = message;
    toast.style.display = "block";
    setTimeout(() => toast.style.display = "none", 2000);
  }

  function clearErrors() {
    ["name", "email", "position", "departmentId"].forEach(field => {
      const element = document.getElementById(field + "Error");
      if (element) element.textContent = "";
    });

    document.getElementById("globalError").textContent = "";
  }

  function renderEmployees(employees) {
    const table = document.getElementById("employeeTable");
    table.replaceChildren();
    const canEdit = table.dataset.canEdit === "true";
    const row = table.createTHead().insertRow();
    ["ID", "名前", "メール", "役職", "部署", "在籍", "雇用区分", "入社日", "退職日", ...(canEdit ? ["操作"] : [])].forEach(label => {
      const th = document.createElement("th"); th.textContent = label; row.appendChild(th);
    });
    const body = table.createTBody();
    const statuses = {ACTIVE: "在職", ON_LEAVE: "休職", RETIRED: "退職"};
    const types = {PERMANENT: "正社員", CONTRACT: "契約社員", OUTSOURCED: "業務委託"};
    employees.forEach(emp => {
      const tr = body.insertRow();
      [emp.id, emp.name, emp.email, emp.position, emp.departmentName, statuses[emp.employmentStatus],
          types[emp.employmentType], emp.hireDate, emp.retirementDate].forEach(value => {
        tr.insertCell().textContent = value ?? "";
      });
      if (canEdit) {
        const actions = tr.insertCell();
        [["編集", "primary", editEmployee], ["削除", "danger", deleteEmployee]].forEach(([label, color, action]) => {
          const button = document.createElement("button"); button.type = "button";
          button.className = "btn btn-sm me-1 btn-outline-" + color; button.textContent = label;
          button.addEventListener("click", () => action(emp.id)); actions.appendChild(button);
        });
      }
    });
  }

  async function loadEmployees() {
    try {
      showLoading(true);

      const res = await fetch("/api/employees/search?page=0&size=10&sort=id,desc");
      const result = await res.json();

      renderEmployees(result.data.content || []);
      renderPagination(result.data);

    } catch (e) {
      showToast("一覧取得失敗");
    } finally {
      showLoading(false);
    }
  }

  async function searchEmployees(page = 0) {
    try {
      showLoading(true);

      const name = document.getElementById("searchName").value;

      const url =
        `/api/employees/search?name=${encodeURIComponent(name)}&page=${page}&size=10&sort=id,desc`;

      const res = await fetch(url);
      const result = await res.json();

      renderEmployees(result.data.content || []);
      renderPagination(result.data);

    } catch (e) {
      showToast("検索失敗");
    } finally {
      showLoading(false);
    }
  }

  function renderPagination(pageData) {
    const area = document.getElementById("pagination");
    area.replaceChildren();

    if (!pageData || pageData.totalPages <= 1) return;

    for (let i = Math.max(0, pageData.number - 2); i < Math.min(pageData.totalPages, pageData.number + 3); i++) {
      const button = document.createElement("button");

      button.textContent = i + 1;
      button.type = "button";
      button.className = "btn btn-sm me-1 " +
        (i === pageData.number ? "btn-primary" : "btn-outline-primary");

      button.onclick = () => searchEmployees(i);

      if (i === pageData.number) {
        button.disabled = true;
      }

      area.appendChild(button);
    }
  }

  function clearSearch() {
    document.getElementById("searchName").value = "";
    loadEmployees();
  }

  document.getElementById("employeeForm")?.addEventListener("submit", async function(e) {
    e.preventDefault();

    clearErrors();
    showLoading(true);

    const id = document.getElementById("id").value;

    const data = {
      employmentStatus: document.getElementById("employmentStatus").value,
      employmentType: document.getElementById("employmentType").value,
      hireDate: document.getElementById("hireDate").value || null,
      retirementDate: document.getElementById("retirementDate").value || null,
      name: document.getElementById("name").value,
      email: document.getElementById("email").value,
      position: document.getElementById("position").value,
      departmentId: document.getElementById("departmentId").value
        ? Number(document.getElementById("departmentId").value)
        : null
    };

    const url = id ? `/api/employees/${id}` : "/api/employees";
    const method = id ? "PUT" : "POST";

    try {
      const res = await fetch(url, {
        method: method,
        headers: csrfHeaders({ "Content-Type": "application/json" }),
        body: JSON.stringify(data)
      });

      const result = await res.json();

      if (res.ok && result.status === "success") {
        showToast(result.message || "保存成功");
        resetForm();
        reloadCurrentList();
        return;
      }

      if (result.status === "error") {
        showFieldErrors(result.data || {});
        document.getElementById("globalError").textContent = result.message || "保存失敗";
        return;
      }

      showToast("保存失敗");

    } catch (err) {
      showToast("通信エラー");
    } finally {
      showLoading(false);
    }
  });

  function showFieldErrors(errors) {
    Object.keys(errors).forEach(field => {
      const errorElement = document.getElementById(field + "Error");
      if (errorElement) {
        errorElement.textContent = errors[field];
      }
    });
  }

  async function editEmployee(id) {
    try {
      showLoading(true);

      const res = await fetch(`/api/employees/${id}`);
      const result = await res.json();

      const emp = result.data;

      if (!emp) {
        showToast("データが見つかりません");
        return;
      }

      document.getElementById("formTitle").textContent = "Edit Employee";

      document.getElementById("employmentStatus").value = emp.employmentStatus;
      document.getElementById("employmentType").value = emp.employmentType;
      document.getElementById("hireDate").value = emp.hireDate ?? "";
      document.getElementById("retirementDate").value = emp.retirementDate ?? "";
      document.getElementById("id").value = emp.id ?? "";
      document.getElementById("name").value = emp.name ?? "";
      document.getElementById("email").value = emp.email ?? "";
      document.getElementById("position").value = emp.position ?? "";
      document.getElementById("departmentId").value = emp.departmentId ?? "";

      clearErrors();

      window.scrollTo({ top: document.body.scrollHeight, behavior: "smooth" });

    } catch (e) {
      showToast("取得失敗");
    } finally {
      showLoading(false);
    }
  }

  async function deleteEmployee(id) {
    if (!confirm("削除しますか？")) return;

    try {
      showLoading(true);

      const res = await fetch(`/api/employees/${id}`, {
        method: "DELETE",
        headers: csrfHeaders()
      });

      if (!res.ok) {
        showToast("削除失敗");
        return;
      }

      showToast("削除成功");
      reloadCurrentList();

    } catch (e) {
      showToast("通信エラー");
    } finally {
      showLoading(false);
    }
  }

  function resetForm() {
    document.getElementById("formTitle").textContent = "New Employee";

    document.getElementById("employmentStatus").value = "ACTIVE";
    document.getElementById("employmentType").value = "PERMANENT";
    document.getElementById("hireDate").value = "";
    document.getElementById("retirementDate").value = "";
    document.getElementById("id").value = "";
    document.getElementById("name").value = "";
    document.getElementById("email").value = "";
    document.getElementById("position").value = "";
    document.getElementById("departmentId").value = "";

    clearErrors();
  }

  function reloadCurrentList() {
    const searchName = document.getElementById("searchName").value;

    if (searchName) {
      searchEmployees(0);
    } else {
      loadEmployees();
    }
  }

  async function loadDepartments() {
    const select = document.getElementById("departmentId");
    if (!select) return;
    try {
      let page = 0;
      let last = false;
      while (!last) {
        const res = await fetch("/api/departments?size=100&sort=name,asc&page=" + page++);
        if (!res.ok) throw new Error("部署一覧取得失敗");
        const result = await res.json();
        result.data.content.forEach(dept => {
          const option = document.createElement("option");
          option.value = dept.id; option.textContent = dept.name; select.appendChild(option);
        });
        last = result.data.last;
      }
    } catch (e) { showToast("部署一覧取得失敗"); }
  }
