import { useEffect, useMemo, useState } from "react";
import { librarianApi } from "../../api/client";
import { StatCard } from "../../components/StatCard";
import Barcode from "../../components/Barcode";

export function LibrarianRequestsPage({ workspace }) {
  const permissionsLoaded = Array.isArray(workspace?.permissions);
  const permissions = permissionsLoaded ? workspace.permissions : [];
  const canProcessRequests = !permissionsLoaded || permissions.includes("REQUEST_PROCESS");

  const [statusFilter, setStatusFilter] = useState("PENDING");
  const [requests, setRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [processingId, setProcessingId] = useState(null);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  async function loadRequests(filter = statusFilter) {
    if (!canProcessRequests) {
      setRequests([]);
      setLoading(false);
      return;
    }

    setLoading(true);
    setError("");
    try {
      const result = await librarianApi.listBorrowRequests(workspace?.token, filter);
      setRequests(result || []);
    } catch (requestError) {
      setError(requestError.message || "Failed to load borrow requests");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadRequests("PENDING");
  }, [workspace?.token, canProcessRequests]);

  async function handleSearch() {
    await loadRequests(statusFilter);
  }

  async function handleProcess(requestId, action) {
    if (!canProcessRequests) {
      setError("Current librarian role cannot process borrow requests.");
      return;
    }

    setProcessingId(requestId);
    setMessage("");
    setError("");

    try {
      let payload;
      if (action === "APPROVE") {
        const copyBarcode = window.prompt("Optional: enter a specific copy barcode. Leave blank to auto-assign.", "");
        payload = { action: "APPROVE", processNote: "Approved by librarian", copyBarcode: copyBarcode || undefined };
      } else {
        payload = { action: "REJECT", rejectReason: "Rejected by librarian workspace" };
      }

      const result = await librarianApi.processBorrowRequest(workspace?.token, requestId, payload);
      setMessage(`Request #${result.requestId} ${action === "APPROVE" ? "approved" : "rejected"} successfully.`);
      await loadRequests(statusFilter);
    } catch (requestError) {
      setError(requestError.message || "Failed to process request");
    } finally {
      setProcessingId(null);
    }
  }

  const stats = useMemo(() => ({
    total: requests.length,
    pendingCount: requests.filter((item) => item.status === "PENDING").length,
    approvedCount: requests.filter((item) => item.status === "APPROVED").length,
    rejectedCount: requests.filter((item) => item.status === "REJECTED").length,
  }), [requests]);

  return (
    <div className="page-stack">
      <section className="page-card">
        <div className="section-head compact">
          <div>
            <span className="eyebrow">Librarian</span>
            <h2 className="section-title">Borrow Requests</h2>
          </div>
        </div>

        <div className="stats-grid">
          <StatCard label="Loaded Requests" value={stats.total} />
          <StatCard label="Pending" value={stats.pendingCount} />
          <StatCard label="Approved" value={stats.approvedCount} />
        </div>
      </section>

      <section className="page-card">
        {!canProcessRequests ? (
          <p className="page-note">No request access.</p>
        ) : (
          <>
            <div className="toolbar">
              <select value={statusFilter} onChange={(event) => setStatusFilter(event.target.value)}>
                <option value="PENDING">PENDING</option>
                <option value="APPROVED">APPROVED</option>
                <option value="REJECTED">REJECTED</option>
              </select>
              <button className="primary-button" type="button" disabled={loading} onClick={handleSearch}>
                {loading ? "Loading..." : "Refresh"}
              </button>
            </div>
            {message ? <p className="page-note">{message}</p> : null}
            {error ? <p className="page-note">{error}</p> : null}
            <div className="table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Reader</th>
                    <th>Book</th>
                    <th>Copy Barcode</th>
                    <th>Status</th>
                    <th>Requested At</th>
                    <th>Remaining</th>
                    <th>Action</th>
                  </tr>
                </thead>
                <tbody>
                  {requests.map((item) => (
                    <tr key={item.requestId}>
                      <td>{item.requestId}</td>
                      <td>{item.readerUsername}</td>
                      <td>{item.bookTitle}</td>
                      <td><Barcode value={item.copyBarcode} height={25} fontSize={9} displayValue={true} /></td>
                      <td>{item.status}</td>
                      <td>{item.requestedAt || "-"}</td>
                      <td>{item.remainingCopies ?? 0}</td>
                      <td>
                        {item.status === "PENDING" ? (
                          <div className="table-actions">
                            <button
                              className="primary-button"
                              type="button"
                              disabled={processingId === item.requestId}
                              onClick={() => handleProcess(item.requestId, "APPROVE")}
                            >
                              {processingId === item.requestId ? "Processing..." : "Approve"}
                            </button>
                            <button
                              className="secondary-button"
                              type="button"
                              disabled={processingId === item.requestId}
                              onClick={() => handleProcess(item.requestId, "REJECT")}
                            >
                              Reject
                            </button>
                          </div>
                        ) : (
                          item.message || "-"
                        )}
                      </td>
                    </tr>
                  ))}
                  {!loading && requests.length === 0 ? <tr><td colSpan="8">No requests.</td></tr> : null}
                </tbody>
              </table>
            </div>
          </>
        )}
      </section>
    </div>
  );
}
