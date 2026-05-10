export function Topbar({ role, username }) {
  return (
    <header className="topbar">
      <div className="topbar-copy">
        <h1>Library Management System</h1>
        <p>{role}</p>
      </div>
      <div className={`role-chip ${role.toLowerCase()}`}>
        <span>Current User</span>
        <strong>{username}</strong>
      </div>
    </header>
  );
}
