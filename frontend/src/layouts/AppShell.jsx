import { Topbar } from "../components/Topbar";
import { SidebarNav } from "../components/SidebarNav";
import { roleDescriptions } from "../config/navigation";

export function AppShell({ role, username, menus, activeKey, onNavigate, onLogout, children }) {
  return (
    <div className="shell">
      <aside className="sidebar">
        <div className="brand">
          <span className="eyebrow">Library System</span>
          <h2>Workspace</h2>
          <p>{roleDescriptions[role]}</p>
        </div>
        <SidebarNav menus={menus} activeKey={activeKey} onNavigate={onNavigate} />
        <button className="ghost-button" onClick={onLogout}>
          Switch User
        </button>
      </aside>

      <main className="content">
        <Topbar role={role} username={username} />
        <div className="workspace">{children}</div>
      </main>
    </div>
  );
}
