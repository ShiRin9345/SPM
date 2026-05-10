import { useState } from "react";
import { authApi } from "../../api/auth";

export function AuthPage({ onLogin, onGoRegister }) {
  const [username, setUsername] = useState("reader");
  const [password, setPassword] = useState("123456");
  const [role, setRole] = useState("READER");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  async function handleSubmit(event) {
    event.preventDefault();
    setLoading(true);
    setError("");

    try {
      const result = await authApi.login({ username, password, role });
      onLogin({ username: result.username, role: result.role, token: result.token });
    } catch (requestError) {
      setError(requestError.message || "Login failed");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="auth-page">
      <div className="auth-card">
        <section className="auth-hero">
          <span className="eyebrow">Library System</span>
          <h1>Sign In</h1>
          <p className="page-note">Select a role and continue to its workspace.</p>
        </section>

        <section className="auth-panel">
          <span className="eyebrow">Account</span>
          <form className="auth-form" onSubmit={handleSubmit}>
            <div className="field">
              <label>Username</label>
              <input value={username} onChange={(e) => setUsername(e.target.value)} placeholder="reader" />
            </div>

            <div className="field">
              <label>Password</label>
              <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} placeholder="123456" />
            </div>

            <div className="field">
              <label>Role</label>
              <select value={role} onChange={(e) => setRole(e.target.value)}>
                <option value="READER">Reader</option>
                <option value="LIBRARIAN">Librarian</option>
                <option value="ADMIN">Admin</option>
              </select>
            </div>

            <button className="primary-button" type="submit" disabled={loading}>
              {loading ? "Signing In..." : "Sign In"}
            </button>

            {role === "READER" ? (
              <button
                className="secondary-button"
                type="button"
                onClick={onGoRegister}
                style={{ marginTop: "12px", width: "100%" }}
              >
                Register
              </button>
            ) : null}
          </form>

          <p className="page-note">Demo: `reader / 123456`, `librarian / 123456`, `admin / 123456`</p>
          {error ? <p className="page-note">{error}</p> : null}
        </section>
      </div>
    </div>
  );
}
