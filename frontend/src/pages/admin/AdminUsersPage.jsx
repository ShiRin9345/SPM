import { useEffect, useState } from "react";
import { adminApi } from "../../api/admin";
import { StatCard } from "../../components/StatCard";

const emptyUserForm = {
  username: "",
  password: "",
  fullName: "",
  studentNo: "",
  phone: "",
  role: "READER",
};

const emptyCategoryForm = {
  code: "",
  name: "",
  enabled: true,
};

const emptyStatusCodeForm = {
  codeType: "",
  codeValue: "",
  displayName: "",
  description: "",
  enabled: true,
};

const PERMISSION_OPTIONS = {
  READER: ["BOOK_SEARCH", "BOOK_VIEW", "BORROW_REQUEST", "RETURN_REQUEST", "RESERVATION"],
  LIBRARIAN: ["BOOK_MANAGE", "INVENTORY_MANAGE", "REQUEST_PROCESS", "RESERVATION_PROCESS", "FINE_MANAGE"],
  ADMIN: ["USER_MANAGE", "ROLE_MANAGE", "SYSTEM_CONFIG", "LOG_VIEW", "BACKUP_RESTORE", "REPORT_VIEW"],
};

export function AdminUsersPage({ workspace }) {
  const token = workspace?.token;
  const [users, setUsers] = useState([]);
  const [permissions, setPermissions] = useState([]);
  const [configs, setConfigs] = useState([]);
  const [categories, setCategories] = useState([]);
  const [statusCodes, setStatusCodes] = useState([]);
  const [userForm, setUserForm] = useState(emptyUserForm);
  const [categoryForm, setCategoryForm] = useState(emptyCategoryForm);
  const [statusCodeForm, setStatusCodeForm] = useState(emptyStatusCodeForm);
  const [editingUserId, setEditingUserId] = useState(null);
  const [editingCategoryId, setEditingCategoryId] = useState(null);
  const [editingStatusCodeId, setEditingStatusCodeId] = useState(null);
  const [roleDrafts, setRoleDrafts] = useState({});
  const [configDrafts, setConfigDrafts] = useState({});
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState("");
  const [messageType, setMessageType] = useState("success");

  useEffect(() => {
    if (token) {
      loadAdminData();
    }
  }, [token]);

  async function loadAdminData() {
    try {
      setLoading(true);
      setMessage("");
      const [userList, permissionList, configList, categoryList, statusCodeList] = await Promise.all([
        adminApi.listUsers(token),
        adminApi.listRolePermissions(token),
        adminApi.listSystemConfigs(token),
        adminApi.listCategoryCodes(token),
        adminApi.listStatusCodes(token),
      ]);
      setUsers(userList || []);
      setPermissions(permissionList || []);
      setConfigs(configList || []);
      setCategories(categoryList || []);
      setStatusCodes(statusCodeList || []);
      setRoleDrafts(Object.fromEntries((permissionList || []).map((item) => [item.role, splitPermissionScope(item.permissionScope)])));
      setConfigDrafts(
        Object.fromEntries((configList || []).map((item) => [item.configKey, { configValue: item.configValue || "", description: item.description || "" }]))
      );
    } catch (error) {
      showMessage("error", error?.message || "Failed to load admin data.");
    } finally {
      setLoading(false);
    }
  }

  function showMessage(type, text) {
    setMessageType(type);
    setMessage(text);
  }

  function resetUserForm() {
    setEditingUserId(null);
    setUserForm(emptyUserForm);
  }

  function resetCategoryForm() {
    setEditingCategoryId(null);
    setCategoryForm(emptyCategoryForm);
  }

  function resetStatusCodeForm() {
    setEditingStatusCodeId(null);
    setStatusCodeForm(emptyStatusCodeForm);
  }

  async function startEditUser(user) {
    try {
      setLoading(true);
      const fullUser = await adminApi.getUser(user.userId, token);
      setEditingUserId(fullUser.userId);
      setUserForm({
        username: fullUser.username || "",
        password: fullUser.password || "",
        fullName: fullUser.fullName || "",
        studentNo: fullUser.studentNo || "",
        phone: fullUser.phone || "",
        role: fullUser.role || "READER",
      });
      showMessage("success", `Editing user ${fullUser.username}.`);
    } catch (error) {
      showMessage("error", error?.message || "Failed to load user details.");
    } finally {
      setLoading(false);
    }
  }

  async function handleUserSubmit(event) {
    event.preventDefault();
    try {
      setLoading(true);
      const payload = { ...userForm };
      if (editingUserId) {
        await adminApi.updateUser(editingUserId, payload, token);
        showMessage("success", "User information updated successfully.");
      } else {
        await adminApi.createUser(payload, token);
        showMessage("success", "User created successfully.");
      }
      resetUserForm();
      await loadAdminData();
    } catch (error) {
      showMessage("error", error?.message || "Failed to save user.");
      setLoading(false);
    }
  }

  async function handleToggleUser(user) {
    try {
      setLoading(true);
      await adminApi.updateUserEnabled(user.userId, !user.enabled, token);
      showMessage("success", `${user.username} is now ${user.enabled ? "disabled" : "enabled"}.`);
      await loadAdminData();
    } catch (error) {
      showMessage("error", error?.message || "Failed to update user status.");
      setLoading(false);
    }
  }

  function handleRoleDraftChange(role, permissionCode, checked) {
    setRoleDrafts((prev) => {
      const current = new Set(prev[role] || []);
      if (checked) {
        current.add(permissionCode);
      } else {
        current.delete(permissionCode);
      }
      return { ...prev, [role]: Array.from(current) };
    });
  }

  async function handleSaveRolePermission(role) {
    try {
      setLoading(true);
      await adminApi.updateRolePermission(role, (roleDrafts[role] || []).join(","), token);
      showMessage("success", `${role} permission scope updated successfully.`);
      await loadAdminData();
    } catch (error) {
      showMessage("error", error?.message || "Failed to update permission scope.");
      setLoading(false);
    }
  }

  function handleConfigDraftChange(configKey, field, value) {
    setConfigDrafts((prev) => ({ ...prev, [configKey]: { ...(prev[configKey] || {}), [field]: value } }));
  }

  async function handleSaveConfig(configKey) {
    try {
      setLoading(true);
      await adminApi.updateSystemConfig(configKey, configDrafts[configKey], token);
      showMessage("success", `${configKey} updated successfully.`);
      await loadAdminData();
    } catch (error) {
      showMessage("error", error?.message || "Failed to update system parameter.");
      setLoading(false);
    }
  }

  async function handleSaveCategory(event) {
    event.preventDefault();
    try {
      setLoading(true);
      if (editingCategoryId) {
        await adminApi.updateCategoryCode(editingCategoryId, { name: categoryForm.name, enabled: categoryForm.enabled }, token);
        showMessage("success", "Category code updated successfully.");
      } else {
        await adminApi.createCategoryCode(categoryForm, token);
        showMessage("success", "Category code created successfully.");
      }
      resetCategoryForm();
      await loadAdminData();
    } catch (error) {
      showMessage("error", error?.message || "Failed to save category code.");
      setLoading(false);
    }
  }

  async function handleDeleteCategory(categoryId) {
    try {
      setLoading(true);
      await adminApi.deleteCategoryCode(categoryId, true, token);
      showMessage("success", "Category code deleted successfully.");
      if (editingCategoryId === categoryId) {
        resetCategoryForm();
      }
      await loadAdminData();
    } catch (error) {
      showMessage("error", error?.message || "Failed to delete category code.");
      setLoading(false);
    }
  }

  async function handleSaveStatusCode(event) {
    event.preventDefault();
    try {
      setLoading(true);
      if (editingStatusCodeId) {
        await adminApi.updateStatusCode(editingStatusCodeId, {
          displayName: statusCodeForm.displayName,
          description: statusCodeForm.description,
          enabled: statusCodeForm.enabled,
        }, token);
        showMessage("success", "Status code updated successfully.");
      } else {
        await adminApi.createStatusCode(statusCodeForm, token);
        showMessage("success", "Status code created successfully.");
      }
      resetStatusCodeForm();
      await loadAdminData();
    } catch (error) {
      showMessage("error", error?.message || "Failed to save status code.");
      setLoading(false);
    }
  }

  return (
    <div className="page-stack">
      <section className="page-card">
        <div className="section-head compact">
          <div>
            <span className="eyebrow">Admin</span>
            <h2 className="section-title">User Management And Rules</h2>
          </div>
        </div>
        <div className="stats-grid">
          <StatCard label="Users" value={users.length} />
          <StatCard label="Role Scopes" value={permissions.length} />
          <StatCard label="Business Params" value={configs.length} />
          <StatCard label="Status Codes" value={statusCodes.length} />
        </div>
        {message ? <p className="page-note" style={{ color: messageType === "success" ? "#67c23a" : "#f56c6c" }}>{message}</p> : null}
      </section>

      <section className="page-card split-grid">
        <div>
          <h3 className="section-title">{editingUserId ? "Edit User" : "Add User"}</h3>
          <form className="auth-form" onSubmit={handleUserSubmit}>
            <input name="username" value={userForm.username} onChange={(e) => setUserForm((prev) => ({ ...prev, username: e.target.value }))} disabled={Boolean(editingUserId)} placeholder="Username" />
            <input type="password" name="password" value={userForm.password} onChange={(e) => setUserForm((prev) => ({ ...prev, password: e.target.value }))} placeholder="Password" />
            <input name="fullName" value={userForm.fullName} onChange={(e) => setUserForm((prev) => ({ ...prev, fullName: e.target.value }))} placeholder="Full Name" />
            <input name="studentNo" value={userForm.studentNo} onChange={(e) => setUserForm((prev) => ({ ...prev, studentNo: e.target.value }))} placeholder="Student / Staff No" />
            <input name="phone" value={userForm.phone} onChange={(e) => setUserForm((prev) => ({ ...prev, phone: e.target.value }))} placeholder="Phone" />
            <select name="role" value={userForm.role} onChange={(e) => setUserForm((prev) => ({ ...prev, role: e.target.value }))}>
              <option value="READER">Reader</option>
              <option value="LIBRARIAN">Librarian</option>
              <option value="ADMIN">Admin</option>
            </select>
            <div className="inline-actions">
              <button className="primary-button" type="submit" disabled={loading}>{editingUserId ? "Save User" : "Add User"}</button>
              {editingUserId ? <button className="secondary-button" type="button" onClick={resetUserForm}>Cancel Edit</button> : null}
            </div>
          </form>
        </div>
        <div>
          <h3 className="section-title">User Accounts</h3>
          <div className="table-wrap">
            <table>
              <thead><tr><th>ID</th><th>Username</th><th>Role</th><th>Enabled</th><th>Actions</th></tr></thead>
              <tbody>
                {users.map((item) => (
                  <tr key={item.userId}>
                    <td>{item.userId}</td>
                    <td>{item.username}</td>
                    <td>{item.role}</td>
                    <td>{item.enabled ? "Yes" : "No"}</td>
                    <td>
                      <div className="table-actions">
                        <button className="secondary-button" type="button" onClick={() => startEditUser(item)}>Edit</button>
                        <button className="secondary-button" type="button" onClick={() => handleToggleUser(item)}>{item.enabled ? "Disable" : "Enable"}</button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </section>

      <section className="page-card split-grid">
        <div>
          <h3 className="section-title">Maintain Category Codes</h3>
          <form className="auth-form" onSubmit={handleSaveCategory}>
            <input placeholder="Category Code" value={categoryForm.code} disabled={Boolean(editingCategoryId)} onChange={(e) => setCategoryForm((prev) => ({ ...prev, code: e.target.value }))} />
            <input placeholder="Category Name" value={categoryForm.name} onChange={(e) => setCategoryForm((prev) => ({ ...prev, name: e.target.value }))} />
            <select value={String(categoryForm.enabled)} onChange={(e) => setCategoryForm((prev) => ({ ...prev, enabled: e.target.value === "true" }))}>
              <option value="true">Enabled</option>
              <option value="false">Disabled</option>
            </select>
            <div className="inline-actions">
              <button className="primary-button" type="submit" disabled={loading}>{editingCategoryId ? "Save Category Code" : "Add Category Code"}</button>
              {editingCategoryId ? <button className="secondary-button" type="button" onClick={resetCategoryForm}>Cancel Edit</button> : null}
            </div>
          </form>
          <div className="table-wrap">
            <table>
              <thead><tr><th>Code</th><th>Name</th><th>Enabled</th><th>Actions</th></tr></thead>
              <tbody>
                {categories.map((item) => (
                  <tr key={item.categoryId}>
                    <td>{item.code}</td>
                    <td>{item.name}</td>
                    <td>{item.enabled ? "Yes" : "No"}</td>
                    <td>
                      <div className="table-actions">
                        <button className="secondary-button" type="button" onClick={() => { setEditingCategoryId(item.categoryId); setCategoryForm({ code: item.code, name: item.name, enabled: item.enabled }); }}>Edit</button>
                        <button className="secondary-button" type="button" onClick={() => handleDeleteCategory(item.categoryId)}>Delete</button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>

        <div>
          <h3 className="section-title">Maintain Status Codes</h3>
          <form className="auth-form" onSubmit={handleSaveStatusCode}>
            <input placeholder="Code Type" value={statusCodeForm.codeType} disabled={Boolean(editingStatusCodeId)} onChange={(e) => setStatusCodeForm((prev) => ({ ...prev, codeType: e.target.value }))} />
            <input placeholder="Code Value" value={statusCodeForm.codeValue} disabled={Boolean(editingStatusCodeId)} onChange={(e) => setStatusCodeForm((prev) => ({ ...prev, codeValue: e.target.value }))} />
            <input placeholder="Display Name" value={statusCodeForm.displayName} onChange={(e) => setStatusCodeForm((prev) => ({ ...prev, displayName: e.target.value }))} />
            <input placeholder="Description" value={statusCodeForm.description} onChange={(e) => setStatusCodeForm((prev) => ({ ...prev, description: e.target.value }))} />
            <select value={String(statusCodeForm.enabled)} onChange={(e) => setStatusCodeForm((prev) => ({ ...prev, enabled: e.target.value === "true" }))}>
              <option value="true">Enabled</option>
              <option value="false">Disabled</option>
            </select>
            <div className="inline-actions">
              <button className="primary-button" type="submit" disabled={loading}>{editingStatusCodeId ? "Save Status Code" : "Add Status Code"}</button>
              {editingStatusCodeId ? <button className="secondary-button" type="button" onClick={resetStatusCodeForm}>Cancel Edit</button> : null}
            </div>
          </form>
          <div className="table-wrap">
            <table>
              <thead><tr><th>Type</th><th>Value</th><th>Name</th><th>Enabled</th><th>Actions</th></tr></thead>
              <tbody>
                {statusCodes.map((item) => (
                  <tr key={item.statusCodeId}>
                    <td>{item.codeType}</td>
                    <td>{item.codeValue}</td>
                    <td>{item.displayName}</td>
                    <td>{item.enabled ? "Yes" : "No"}</td>
                    <td>
                      <button className="secondary-button" type="button" onClick={() => {
                        setEditingStatusCodeId(item.statusCodeId);
                        setStatusCodeForm({
                          codeType: item.codeType,
                          codeValue: item.codeValue,
                          displayName: item.displayName,
                          description: item.description || "",
                          enabled: item.enabled,
                        });
                      }}>Edit</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </section>

      <section className="page-card split-grid">
        <div>
          <h3 className="section-title">Roles And Permissions</h3>
          <div style={{ display: "grid", gap: "16px" }}>
            {permissions.map((item) => (
              <div key={item.role} className="monitor-card" style={{ alignItems: "stretch" }}>
                <strong>{item.role}</strong>
                <div className="permission-grid">
                  {(PERMISSION_OPTIONS[item.role] || []).map((permissionCode) => {
                    const checked = (roleDrafts[item.role] || []).includes(permissionCode);
                    return (
                      <label key={permissionCode} className={`permission-pill${checked ? " checked" : ""}`}>
                        <input type="checkbox" checked={checked} onChange={(event) => handleRoleDraftChange(item.role, permissionCode, event.target.checked)} />
                        <span>{permissionCode}</span>
                      </label>
                    );
                  })}
                </div>
                <button className="primary-button" type="button" onClick={() => handleSaveRolePermission(item.role)}>Save Permission Scope</button>
              </div>
            ))}
          </div>
        </div>

        <div>
          <h3 className="section-title">Maintain Business Parameters</h3>
          <div style={{ display: "grid", gap: "16px" }}>
            {configs.map((item) => {
              const draft = configDrafts[item.configKey] || { configValue: "", description: "" };
              return (
                <div key={item.configKey} className="monitor-card" style={{ alignItems: "stretch" }}>
                  <strong>{item.configKey}</strong>
                  <input value={draft.configValue} onChange={(event) => handleConfigDraftChange(item.configKey, "configValue", event.target.value)} placeholder="Value" />
                  <input value={draft.description} onChange={(event) => handleConfigDraftChange(item.configKey, "description", event.target.value)} placeholder="Description" />
                  <button className="primary-button" type="button" onClick={() => handleSaveConfig(item.configKey)}>Save Parameter</button>
                </div>
              );
            })}
          </div>
        </div>
      </section>
    </div>
  );
}

function splitPermissionScope(permissionScope) {
  if (!permissionScope) {
    return [];
  }
  return permissionScope.split(",").map((item) => item.trim()).filter(Boolean);
}
