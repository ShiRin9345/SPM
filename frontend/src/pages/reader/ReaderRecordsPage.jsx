import { useEffect, useMemo, useState } from "react";
import { readerApi } from "../../api/client";
import { StatCard } from "../../components/StatCard";

export function ReaderRecordsPage({ workspace }) {
  const permissionsLoaded = Array.isArray(workspace?.permissions);
  const permissions = permissionsLoaded ? workspace.permissions : [];
  const canViewRecords =
    !permissionsLoaded
    || permissions.includes("RETURN_REQUEST")
    || permissions.includes("BORROW_REQUEST")
    || permissions.includes("BOOK_VIEW")
    || permissions.includes("BOOK_SEARCH");
  const canManageReturns = !permissionsLoaded || permissions.includes("RETURN_REQUEST");

  const [records, setRecords] = useState([]);
  const [loading, setLoading] = useState(true);
  const [submittingId, setSubmittingId] = useState(null);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  async function loadRecords() {
    if (!canViewRecords) {
      setRecords([]);
      setLoading(false);
      return;
    }

    setLoading(true);
    setError("");
    try {
      const result = await readerApi.listBorrowRecords(workspace?.token);
      setRecords(result || []);
    } catch (requestError) {
      setError(requestError.message || "Failed to load borrow records");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadRecords();
  }, [workspace?.token, canViewRecords]);

  async function handleReturnRequest(recordId) {
    if (!canManageReturns) {
      setError("Current reader role cannot submit return requests.");
      return;
    }

    setSubmittingId(recordId);
    setMessage("");
    setError("");
    try {
      const result = await readerApi.submitReturnRequest(workspace?.token, recordId);
      setMessage(result.message || `Return request #${recordId} submitted.`);
      await loadRecords();
    } catch (requestError) {
      setError(requestError.message || "Failed to submit return request");
    } finally {
      setSubmittingId(null);
    }
  }

  const currentRecords = useMemo(
    () => records.filter((item) => item.status !== "RETURNED"),
    [records]
  );

  const historyRecords = useMemo(
    () => records.filter((item) => item.status === "RETURNED"),
    [records]
  );

  const stats = useMemo(
    () => ({
      activeLoans: records.filter((item) => item.status === "BORROWED" || item.status === "OVERDUE").length,
      returnPending: records.filter((item) => item.status === "RETURN_PENDING").length,
      historyCount: historyRecords.length,
    }),
    [records, historyRecords.length]
  );

  return (
    <div className="page-stack">
      <section className="page-card">
        <div className="section-head compact">
          <div>
            <span className="eyebrow">Reader</span>
            <h2 className="section-title">Borrow And Return Records</h2>
          </div>
        </div>

        <div className="stats-grid">
          <StatCard label="Current Borrowings" value={stats.activeLoans} />
          <StatCard label="Return Pending" value={stats.returnPending} />
          <StatCard label="Past History" value={stats.historyCount} />
        </div>
      </section>

      <section className="page-card">
        {!canViewRecords ? (
          <p className="page-note">Current reader role does not have permission to view borrow records.</p>
        ) : (
          <>
            <div className="inline-actions">
              <button className="secondary-button" type="button" onClick={loadRecords} disabled={loading}>
                {loading ? "Loading..." : "Refresh Records"}
              </button>
            </div>
            {message ? <p className="page-note">{message}</p> : null}
            {error ? <p className="page-note">{error}</p> : null}

            <h3 className="section-title">Current Borrowing Records</h3>
            <div className="table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Book</th>
                    <th>Copy Barcode</th>
                    <th>Status</th>
                    <th>Borrow Date</th>
                    <th>Due Date</th>
                    <th>Fine</th>
                    <th>Action</th>
                  </tr>
                </thead>
                <tbody>
                  {currentRecords.map((record) => (
                    <tr key={record.recordId}>
                      <td>{record.recordId}</td>
                      <td>{record.bookTitle}</td>
                      <td>{record.copyBarcode || "-"}</td>
                      <td>{record.status}</td>
                      <td>{record.borrowDate || "-"}</td>
                      <td>{record.dueDate || "-"}</td>
                      <td>${Number(record.fineAmount || 0).toFixed(2)}</td>
                      <td>
                        {record.canRequestReturn && canManageReturns ? (
                          <button
                            className="primary-button"
                            type="button"
                            disabled={submittingId === record.recordId}
                            onClick={() => handleReturnRequest(record.recordId)}
                          >
                            {submittingId === record.recordId ? "Submitting..." : "Create Return Request"}
                          </button>
                        ) : (
                          record.message || "-"
                        )}
                      </td>
                    </tr>
                  ))}
                  {!loading && currentRecords.length === 0 ? (
                    <tr>
                      <td colSpan="8">No current borrowing records.</td>
                    </tr>
                  ) : null}
                </tbody>
              </table>
            </div>
          </>
        )}
      </section>

      {canViewRecords ? (
        <section className="page-card">
          <h3 className="section-title">Past Borrowing History</h3>
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Book</th>
                  <th>Copy Barcode</th>
                  <th>Borrow Date</th>
                  <th>Due Date</th>
                  <th>Return Date</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {historyRecords.map((record) => (
                  <tr key={record.recordId}>
                    <td>{record.recordId}</td>
                    <td>{record.bookTitle}</td>
                    <td>{record.copyBarcode || "-"}</td>
                    <td>{record.borrowDate || "-"}</td>
                    <td>{record.dueDate || "-"}</td>
                    <td>{record.returnDate || "-"}</td>
                    <td>{record.status}</td>
                  </tr>
                ))}
                {!loading && historyRecords.length === 0 ? (
                  <tr>
                    <td colSpan="7">No borrowing history yet.</td>
                  </tr>
                ) : null}
              </tbody>
            </table>
          </div>
        </section>
      ) : null}
    </div>
  );
}
