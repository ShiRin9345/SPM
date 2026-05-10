import { useEffect, useState } from "react";
import { librarianApi } from "../../api/client";
import { StatCard } from "../../components/StatCard";

export function LibrarianOperationsPage({ workspace }) {
  const permissionsLoaded = Array.isArray(workspace?.permissions);
  const permissions = permissionsLoaded ? workspace.permissions : [];
  const canProcessReturns = !permissionsLoaded || permissions.includes("REQUEST_PROCESS");
  const canProcessReservations = !permissionsLoaded || permissions.includes("RESERVATION_PROCESS");
  const canManageFines = !permissionsLoaded || permissions.includes("FINE_MANAGE");
  const hasOperationsAccess = canProcessReturns || canProcessReservations || canManageFines;

  const [stats, setStats] = useState(null);
  const [details, setDetails] = useState(null);
  const [currentBorrowings, setCurrentBorrowings] = useState([]);
  const [overdueBorrowings, setOverdueBorrowings] = useState([]);
  const [returns, setReturns] = useState([]);
  const [reservations, setReservations] = useState([]);
  const [fines, setFines] = useState([]);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  async function loadData() {
    if (!hasOperationsAccess) {
      setStats(null);
      setDetails(null);
      setCurrentBorrowings([]);
      setOverdueBorrowings([]);
      setReturns([]);
      setReservations([]);
      setFines([]);
      setLoading(false);
      return;
    }

    setLoading(true);
    setError("");
    try {
      const [statistics, statisticsDetail, currentRows, overdueRows, returnRows, reservationRows, fineRows] = await Promise.all([
        librarianApi.getStatistics(workspace?.token),
        librarianApi.getDetailedStatistics(workspace?.token, "month"),
        canProcessReturns ? librarianApi.listCurrentBorrowings(workspace?.token) : Promise.resolve([]),
        canManageFines ? librarianApi.listOverdueBorrowings(workspace?.token) : Promise.resolve([]),
        canProcessReturns ? librarianApi.listReturnRequests(workspace?.token) : Promise.resolve([]),
        canProcessReservations ? librarianApi.listReservations(workspace?.token) : Promise.resolve([]),
        canManageFines ? librarianApi.listFines(workspace?.token) : Promise.resolve([]),
      ]);
      setStats(statistics || {});
      setDetails(statisticsDetail || null);
      setCurrentBorrowings(currentRows || []);
      setOverdueBorrowings(overdueRows || []);
      setReturns(returnRows || []);
      setReservations(reservationRows || []);
      setFines(fineRows || []);
    } catch (requestError) {
      setError(requestError.message || "Failed to load operations data");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadData();
  }, [workspace?.token, hasOperationsAccess, canProcessReturns, canProcessReservations, canManageFines]);

  async function handleProcessReturn(recordId, approve) {
    if (!canProcessReturns) {
      setError("Current librarian role cannot process return requests.");
      return;
    }
    setMessage("");
    setError("");
    try {
      await librarianApi.processReturnRequest(workspace?.token, recordId, {
        approve,
        fineAmount: approve ? 0 : undefined,
        rejectReason: approve ? undefined : "return request rejected by librarian",
      });
      setMessage(`Return #${recordId} ${approve ? "approved" : "rejected"}.`);
      await loadData();
    } catch (requestError) {
      setError(requestError.message || "Failed to process return");
    }
  }

  async function handleProcessReservation(reservationId, action) {
    if (!canProcessReservations) {
      setError("Current librarian role cannot process reservations.");
      return;
    }
    setMessage("");
    setError("");
    try {
      await librarianApi.processReservation(workspace?.token, reservationId, { action });
      setMessage(`Reservation #${reservationId} ${action.toLowerCase()} completed.`);
      await loadData();
    } catch (requestError) {
      setError(requestError.message || "Failed to process reservation");
    }
  }

  async function handleFineStatus(fineId, status) {
    if (!canManageFines) {
      setError("Current librarian role cannot manage fines.");
      return;
    }
    setMessage("");
    setError("");
    try {
      await librarianApi.updateFineStatus(workspace?.token, fineId, { status });
      setMessage(`Fine #${fineId} marked as ${status}.`);
      await loadData();
    } catch (requestError) {
      setError(requestError.message || "Failed to update fine");
    }
  }

  return (
    <div className="page-stack">
      <section className="page-card">
        <div className="section-head compact">
          <div>
            <span className="eyebrow">Librarian</span>
            <h2 className="section-title">Operations</h2>
          </div>
        </div>

        <div className="stats-grid">
          <StatCard label="Current Borrowings" value={stats?.activeBorrows ?? 0} />
          <StatCard label="Pending Reservations" value={stats?.pendingReservations ?? 0} />
          <StatCard label="Unpaid Fines" value={stats?.unpaidFines ?? 0} />
        </div>
      </section>

      {!hasOperationsAccess ? (
        <section className="page-card">
          <p className="page-note">No operations access.</p>
        </section>
      ) : (
        <>
          <section className="page-card">
            {message ? <p className="page-note">{message}</p> : null}
            {error ? <p className="page-note">{error}</p> : null}
            <div className="inline-actions">
              <button className="secondary-button" type="button" onClick={loadData} disabled={loading}>
                {loading ? "Loading..." : "Refresh"}
              </button>
            </div>
            <div className="monitor-grid">
              <div className="monitor-card">
                <small>Total Books</small>
                <strong>{stats?.totalBooks ?? 0}</strong>
              </div>
              <div className="monitor-card">
                <small>Pending Returns</small>
                <strong>{stats?.pendingReturnRequests ?? 0}</strong>
              </div>
              <div className="monitor-card">
                <small>Overdue Records</small>
                <strong>{overdueBorrowings.length}</strong>
              </div>
            </div>
          </section>

          {canProcessReturns ? (
            <section className="page-card">
              <h3 className="section-title">Current Borrowing Records</h3>
              <div className="table-wrap">
                <table>
                  <thead>
                    <tr>
                      <th>ID</th>
                      <th>Reader</th>
                      <th>Book</th>
                      <th>Copy Barcode</th>
                      <th>Status</th>
                      <th>Borrow Date</th>
                      <th>Due Date</th>
                    </tr>
                  </thead>
                  <tbody>
                    {currentBorrowings.map((item) => (
                      <tr key={item.recordId}>
                        <td>{item.recordId}</td>
                        <td>{item.readerUsername}</td>
                        <td>{item.bookTitle}</td>
                        <td>{item.copyBarcode || "-"}</td>
                        <td>{item.status}</td>
                        <td>{item.borrowDate || "-"}</td>
                        <td>{item.dueDate || "-"}</td>
                      </tr>
                    ))}
                    {!loading && currentBorrowings.length === 0 ? <tr><td colSpan="7">No active borrowing records.</td></tr> : null}
                  </tbody>
                </table>
              </div>
            </section>
          ) : null}

          {canManageFines ? (
            <section className="page-card">
              <h3 className="section-title">Overdue Borrowing Records</h3>
              <div className="table-wrap">
                <table>
                  <thead>
                    <tr>
                      <th>ID</th>
                      <th>Reader</th>
                      <th>Book</th>
                      <th>Copy Barcode</th>
                      <th>Due Date</th>
                      <th>Overdue Days</th>
                      <th>Fine</th>
                      <th>Fine Status</th>
                      <th>Reminder</th>
                    </tr>
                  </thead>
                  <tbody>
                    {overdueBorrowings.map((item) => (
                      <tr key={item.recordId}>
                        <td>{item.recordId}</td>
                        <td>{item.readerUsername}</td>
                        <td>{item.bookTitle}</td>
                        <td>{item.copyBarcode || "-"}</td>
                        <td>{item.dueDate || "-"}</td>
                        <td>{item.overdueDays ?? 0}</td>
                        <td>{item.fineAmount ?? 0}</td>
                        <td>{item.fineStatus || "-"}</td>
                        <td>{item.reminderInfo || "-"}</td>
                      </tr>
                    ))}
                    {!loading && overdueBorrowings.length === 0 ? <tr><td colSpan="9">No overdue borrowing records.</td></tr> : null}
                  </tbody>
                </table>
              </div>
            </section>
          ) : null}

          {canProcessReturns ? (
            <section className="page-card">
              <h3 className="section-title">Return Requests</h3>
              <div className="table-wrap">
                <table>
                  <thead>
                    <tr>
                      <th>ID</th>
                      <th>Reader</th>
                      <th>Book</th>
                      <th>Copy Barcode</th>
                      <th>Status</th>
                      <th>Due Date</th>
                      <th>Action</th>
                    </tr>
                  </thead>
                  <tbody>
                    {returns.map((item) => (
                      <tr key={item.recordId}>
                        <td>{item.recordId}</td>
                        <td>{item.readerUsername}</td>
                        <td>{item.bookTitle}</td>
                        <td>{item.copyBarcode || "-"}</td>
                        <td>{item.status}</td>
                        <td>{item.dueDate || "-"}</td>
                        <td>
                          <div className="table-actions">
                            <button className="primary-button" type="button" onClick={() => handleProcessReturn(item.recordId, true)}>
                              Approve
                            </button>
                            <button className="secondary-button" type="button" onClick={() => handleProcessReturn(item.recordId, false)}>
                              Reject
                            </button>
                          </div>
                        </td>
                      </tr>
                    ))}
                    {!loading && returns.length === 0 ? <tr><td colSpan="7">No return requests.</td></tr> : null}
                  </tbody>
                </table>
              </div>
            </section>
          ) : null}

          {canProcessReservations ? (
            <section className="page-card">
              <h3 className="section-title">Reservations</h3>
              <div className="table-wrap">
                <table>
                  <thead>
                    <tr>
                      <th>ID</th>
                      <th>Reader</th>
                      <th>Book</th>
                      <th>Status</th>
                      <th>Queue No</th>
                      <th>Action</th>
                    </tr>
                  </thead>
                  <tbody>
                    {reservations.map((item) => (
                      <tr key={item.reservationId}>
                        <td>{item.reservationId}</td>
                        <td>{item.readerUsername}</td>
                        <td>{item.bookTitle}</td>
                        <td>{item.status}</td>
                        <td>{item.queueNo}</td>
                        <td>
                          {item.status === "PENDING" ? (
                            <div className="table-actions">
                              <button className="primary-button" type="button" onClick={() => handleProcessReservation(item.reservationId, "FULFILL")}>
                                Fulfill
                              </button>
                              <button className="secondary-button" type="button" onClick={() => handleProcessReservation(item.reservationId, "CANCEL")}>
                                Cancel
                              </button>
                            </div>
                          ) : (
                            item.message || "-"
                          )}
                        </td>
                      </tr>
                    ))}
                    {!loading && reservations.length === 0 ? <tr><td colSpan="6">No reservations.</td></tr> : null}
                  </tbody>
                </table>
              </div>
            </section>
          ) : null}

          {canManageFines ? (
            <section className="page-card">
              <h3 className="section-title">Fines</h3>
              <div className="table-wrap">
                <table>
                  <thead>
                    <tr>
                      <th>ID</th>
                      <th>Book</th>
                      <th>Copy Barcode</th>
                      <th>Reader</th>
                      <th>Amount</th>
                      <th>Status</th>
                      <th>Action</th>
                    </tr>
                  </thead>
                  <tbody>
                    {fines.map((item) => (
                      <tr key={item.fineId}>
                        <td>{item.fineId}</td>
                        <td>{item.bookTitle || "-"}</td>
                        <td>{item.copyBarcode || "-"}</td>
                        <td>{item.readerUsername}</td>
                        <td>{item.amount}</td>
                        <td>{item.status}</td>
                        <td>
                          <div className="table-actions">
                            <button className="primary-button" type="button" onClick={() => handleFineStatus(item.fineId, "PAID")}>
                              Mark Paid
                            </button>
                            <button className="secondary-button" type="button" onClick={() => handleFineStatus(item.fineId, "UNPAID")}>
                              Mark Unpaid
                            </button>
                          </div>
                        </td>
                      </tr>
                    ))}
                    {!loading && fines.length === 0 ? <tr><td colSpan="7">No fines.</td></tr> : null}
                  </tbody>
                </table>
              </div>
            </section>
          ) : null}

          <section className="page-card">
            <h3 className="section-title">Borrowing Statistics</h3>
            <div className="split-grid">
              <div>
                <h4 className="section-title">Popular Books</h4>
                <div className="table-wrap">
                  <table>
                    <thead>
                      <tr>
                        <th>Book</th>
                        <th>Author</th>
                        <th>Category</th>
                        <th>Borrow Count</th>
                      </tr>
                    </thead>
                    <tbody>
                      {(details?.popularBooks || []).map((item) => (
                        <tr key={item.bookId}>
                          <td>{item.title}</td>
                          <td>{item.author}</td>
                          <td>{item.categoryName || "-"}</td>
                          <td>{item.borrowCount}</td>
                        </tr>
                      ))}
                      {!loading && (!details?.popularBooks || details.popularBooks.length === 0) ? <tr><td colSpan="4">No popular book data.</td></tr> : null}
                    </tbody>
                  </table>
                </div>
              </div>
              <div>
                <h4 className="section-title">Borrowing Trend</h4>
                <div className="table-wrap">
                  <table>
                    <thead>
                      <tr>
                        <th>Period</th>
                        <th>Borrow Count</th>
                        <th>Return Count</th>
                      </tr>
                    </thead>
                    <tbody>
                      {(details?.borrowTrend || []).map((item) => (
                        <tr key={item.period}>
                          <td>{item.period}</td>
                          <td>{item.borrowCount}</td>
                          <td>{item.returnCount}</td>
                        </tr>
                      ))}
                      {!loading && (!details?.borrowTrend || details.borrowTrend.length === 0) ? <tr><td colSpan="3">No borrowing trend data.</td></tr> : null}
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          </section>
        </>
      )}
    </div>
  );
}
