import { useEffect, useState } from "react";
import { adminApi } from "../../api/admin";
import { StatCard } from "../../components/StatCard";

export function AdminMonitoringPage({ workspace }) {
  const token = workspace?.token;
  const [overview, setOverview] = useState(null);
  const [logs, setLogs] = useState([]);
  const [abnormalBehaviors, setAbnormalBehaviors] = useState([]);
  const [backups, setBackups] = useState([]);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  async function loadMonitoringData() {
    if (!token) {
      return;
    }
    setLoading(true);
    setError("");
    try {
      const [overviewResult, logRows, abnormalRows, backupRows] = await Promise.all([
        adminApi.getMonitoringOverview(token),
        adminApi.listOperationLogs(token),
        adminApi.listAbnormalBehaviors(token),
        adminApi.listBackupRecords(token),
      ]);
      setOverview(overviewResult || {});
      setLogs(logRows || []);
      setAbnormalBehaviors(abnormalRows || []);
      setBackups(backupRows || []);
    } catch (requestError) {
      setError(requestError.message || "Failed to load monitoring data.");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadMonitoringData();
  }, [token]);

  async function handleCreateBackup() {
    try {
      setLoading(true);
      setMessage("");
      setError("");
      const result = await adminApi.executeBackup(token);
      setMessage(`Backup ${result.backupName} created successfully.`);
      await loadMonitoringData();
    } catch (requestError) {
      setError(requestError.message || "Failed to create backup.");
      setLoading(false);
    }
  }

  return (
    <div className="page-stack">
      <section className="page-card">
        <div className="section-head compact">
          <div>
            <span className="eyebrow">Admin</span>
            <h2 className="section-title">Monitoring</h2>
          </div>
        </div>

        <div className="stats-grid">
          <StatCard label="Total Users" value={overview?.totalUsers ?? 0} />
          <StatCard label="Total Books" value={overview?.totalBooks ?? 0} />
          <StatCard label="Active Borrows" value={overview?.activeBorrows ?? 0} />
          <StatCard label="Latest Backup" value={overview?.latestBackup || "No backup yet"} />
        </div>
      </section>

      <section className="page-card">
        <div className="inline-actions">
          <button className="primary-button" type="button" onClick={handleCreateBackup} disabled={loading}>
            {loading ? "Processing..." : "Create Backup"}
          </button>
          <button className="secondary-button" type="button" onClick={loadMonitoringData} disabled={loading}>
            Refresh
          </button>
        </div>
        {message ? <p className="page-note">{message}</p> : null}
        {error ? <p className="page-note">{error}</p> : null}
        <div className="monitor-grid">
          <div className="monitor-card">
            <small>Pending Reservations</small>
            <strong>{overview?.pendingReservations ?? 0}</strong>
          </div>
          <div className="monitor-card">
            <small>Unpaid Fines</small>
            <strong>{overview?.unpaidFines ?? 0}</strong>
          </div>
          <div className="monitor-card">
            <small>System Status</small>
            <strong>{overview?.systemStatus || "Unknown"}</strong>
          </div>
        </div>
      </section>

      <section className="page-card split-grid">
        <div>
          <h3 className="section-title">Key Operation Logs</h3>
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Module</th>
                  <th>Action</th>
                  <th>Operator</th>
                  <th>Result</th>
                  <th>Created At</th>
                </tr>
              </thead>
              <tbody>
                {logs.map((item) => (
                  <tr key={item.logId}>
                    <td>{item.logId}</td>
                    <td>{item.moduleName}</td>
                    <td>{item.actionName}</td>
                    <td>{item.operatorName}</td>
                    <td>{item.resultMessage || "-"}</td>
                    <td>{item.createdAt || "-"}</td>
                  </tr>
                ))}
                {!loading && logs.length === 0 ? <tr><td colSpan="6">No operation logs.</td></tr> : null}
              </tbody>
            </table>
          </div>
        </div>

        <div>
          <h3 className="section-title">Abnormal Behaviors</h3>
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Module</th>
                  <th>Action</th>
                  <th>Operator</th>
                  <th>Detail</th>
                </tr>
              </thead>
              <tbody>
                {abnormalBehaviors.map((item) => (
                  <tr key={item.logId}>
                    <td>{item.logId}</td>
                    <td>{item.moduleName}</td>
                    <td>{item.actionName}</td>
                    <td>{item.operatorName}</td>
                    <td>{item.resultMessage || "-"}</td>
                  </tr>
                ))}
                {!loading && abnormalBehaviors.length === 0 ? <tr><td colSpan="5">No abnormal behaviors.</td></tr> : null}
              </tbody>
            </table>
          </div>
        </div>
      </section>

      <section className="page-card">
        <h3 className="section-title">Backup Records</h3>
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>Backup Name</th>
                <th>Status</th>
                <th>Summary</th>
                <th>File Path</th>
                <th>Created At</th>
              </tr>
            </thead>
            <tbody>
              {backups.map((item) => (
                <tr key={item.backupId}>
                  <td>{item.backupId}</td>
                  <td>{item.backupName}</td>
                  <td>{item.status}</td>
                  <td>{item.summary || "-"}</td>
                  <td>{item.filePath}</td>
                  <td>{item.createdAt || "-"}</td>
                </tr>
              ))}
              {!loading && backups.length === 0 ? <tr><td colSpan="6">No backup records.</td></tr> : null}
            </tbody>
          </table>
        </div>
      </section>
    </div>
  );
}
