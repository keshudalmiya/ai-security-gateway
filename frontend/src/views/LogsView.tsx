import React, { useState, useEffect } from 'react';
import type { RequestEvent } from '../types';
import { gatewayApi } from '../api/gatewayApi';
import { DecisionBadge } from '../components/DecisionBadge';
import { RiskBadge } from '../components/RiskBadge';
import { Search, Download, ChevronLeft, ChevronRight, Filter } from 'lucide-react';

interface Props {
  onSelectEvent: (event: RequestEvent) => void;
}

export const LogsView: React.FC<Props> = ({ onSelectEvent }) => {
  const [events, setEvents] = useState<RequestEvent[]>([]);
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [page, setPage] = useState(0);

  const [decisionFilter, setDecisionFilter] = useState('ALL');
  const [riskFilter, setRiskFilter] = useState('ALL');
  const [methodFilter, setMethodFilter] = useState('ALL');
  const [searchQuery, setSearchQuery] = useState('');

  const [isLoading, setIsLoading] = useState(false);

  const fetchLogs = async () => {
    setIsLoading(true);
    try {
      const data = await gatewayApi.getRequests({
        decision: decisionFilter !== 'ALL' ? decisionFilter : undefined,
        riskLevel: riskFilter !== 'ALL' ? riskFilter : undefined,
        method: methodFilter !== 'ALL' ? methodFilter : undefined,
        query: searchQuery || undefined,
        page,
        size: 15
      });
      setEvents(data.content || []);
      setTotalElements(data.totalElements || 0);
      setTotalPages(data.totalPages || 0);
    } catch (e) {
      console.error('Failed to fetch request logs:', e);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchLogs();
  }, [decisionFilter, riskFilter, methodFilter, page]);

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    setPage(0);
    fetchLogs();
  };

  const handleExportCsv = () => {
    if (events.length === 0) return;
    const headers = ['RequestID', 'Timestamp', 'Method', 'Path', 'RiskScore', 'RiskLevel', 'Decision', 'LatencyMs', 'Reasons'];
    const rows = events.map((e) => [
      e.requestId,
      e.createdAt,
      e.method,
      `"${e.path.replace(/"/g, '""')}"`,
      e.riskScore,
      e.riskLevel,
      e.decision,
      e.totalLatencyMs,
      `"${(e.reasons || []).join('; ').replace(/"/g, '""')}"`
    ]);

    const csvContent = 'data:text/csv;charset=utf-8,' + [headers.join(','), ...rows.map((r) => r.join(','))].join('\n');
    const encodedUri = encodeURI(csvContent);
    const link = document.createElement('a');
    link.setAttribute('href', encodedUri);
    link.setAttribute('download', `security_logs_${new Date().toISOString().substring(0, 10)}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
      {/* Filter and Search Bar */}
      <div className="card" style={{ padding: '16px 20px' }}>
        <form onSubmit={handleSearch} style={{ display: 'flex', flexWrap: 'wrap', alignItems: 'center', gap: '14px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '6px', fontSize: '13px', color: 'var(--text-muted)' }}>
            <Filter size={15} /> Filters:
          </div>

          <select
            className="form-select"
            style={{ width: '130px' }}
            value={decisionFilter}
            onChange={(e) => {
              setDecisionFilter(e.target.value);
              setPage(0);
            }}
          >
            <option value="ALL">Decision: All</option>
            <option value="ALLOW">ALLOW</option>
            <option value="FLAG">FLAG</option>
            <option value="BLOCK">BLOCK</option>
          </select>

          <select
            className="form-select"
            style={{ width: '130px' }}
            value={riskFilter}
            onChange={(e) => {
              setRiskFilter(e.target.value);
              setPage(0);
            }}
          >
            <option value="ALL">Risk: All</option>
            <option value="LOW">LOW</option>
            <option value="MEDIUM">MEDIUM</option>
            <option value="HIGH">HIGH</option>
          </select>

          <select
            className="form-select"
            style={{ width: '130px' }}
            value={methodFilter}
            onChange={(e) => {
              setMethodFilter(e.target.value);
              setPage(0);
            }}
          >
            <option value="ALL">Method: All</option>
            <option value="GET">GET</option>
            <option value="POST">POST</option>
            <option value="PUT">PUT</option>
            <option value="PATCH">PATCH</option>
            <option value="DELETE">DELETE</option>
          </select>

          <div style={{ flex: 1, minWidth: '200px', display: 'flex', gap: '8px' }}>
            <input
              type="text"
              className="form-input"
              style={{ width: '100%' }}
              placeholder="Search by path or Request ID..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
            />
            <button type="submit" className="btn btn-secondary">
              <Search size={14} />
            </button>
          </div>

          <button type="button" className="btn btn-secondary" onClick={handleExportCsv} disabled={events.length === 0}>
            <Download size={14} /> Export CSV
          </button>
        </form>
      </div>

      {/* Logs Data Table */}
      <div className="card" style={{ padding: '0', overflow: 'hidden' }}>
        <div style={{ padding: '16px 20px', borderBottom: '1px solid var(--border-subtle)', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <span style={{ fontSize: '14px', fontWeight: 600 }}>Analyzed Requests ({totalElements.toLocaleString()} records)</span>
          {isLoading && <span style={{ fontSize: '12px', color: 'var(--text-faint)' }}>Loading logs...</span>}
        </div>

        <div className="data-table-container" style={{ border: 'none', borderRadius: '0' }}>
          <table className="data-table">
            <thead>
              <tr>
                <th>Time</th>
                <th>Method</th>
                <th>Request Path</th>
                <th>Risk Score</th>
                <th>Decision</th>
                <th>Primary Reason</th>
                <th>Latency</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {events.length > 0 ? (
                events.map((evt) => (
                  <tr key={evt.requestId}>
                    <td style={{ fontSize: '11.5px', color: 'var(--text-faint)', fontFamily: 'var(--font-mono)' }}>
                      {evt.createdAt ? evt.createdAt.substring(11, 19) : 'now'}
                    </td>
                    <td>
                      <span className="badge badge-method">{evt.method}</span>
                    </td>
                    <td className="mono-cell" style={{ maxWidth: '240px', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                      {evt.path}
                    </td>
                    <td>
                      <RiskBadge riskLevel={evt.riskLevel} score={evt.riskScore} />
                    </td>
                    <td>
                      <DecisionBadge decision={evt.decision} />
                    </td>
                    <td style={{ maxWidth: '280px', overflow: 'hidden', textOverflow: 'ellipsis', color: 'var(--text-muted)' }}>
                      {evt.reasons && evt.reasons.length > 0 ? evt.reasons[0] : 'Baseline rule check'}
                    </td>
                    <td style={{ fontSize: '12px', color: 'var(--text-faint)' }}>
                      {evt.totalLatencyMs?.toFixed(1)} ms
                    </td>
                    <td>
                      <button className="btn btn-secondary btn-sm" onClick={() => onSelectEvent(evt)}>
                        Inspect
                      </button>
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan={8} style={{ textAlign: 'center', padding: '36px', color: 'var(--text-muted)' }}>
                    No security events found matching current criteria.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>

        {/* Pagination Bar */}
        <div style={{ padding: '14px 20px', borderTop: '1px solid var(--border-subtle)', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <span style={{ fontSize: '12px', color: 'var(--text-faint)' }}>
            Page {page + 1} of {Math.max(1, totalPages)}
          </span>
          <div style={{ display: 'flex', gap: '8px' }}>
            <button
              className="btn btn-secondary btn-sm"
              disabled={page === 0}
              onClick={() => setPage((p) => Math.max(0, p - 1))}
            >
              <ChevronLeft size={14} /> Previous
            </button>
            <button
              className="btn btn-secondary btn-sm"
              disabled={page >= totalPages - 1}
              onClick={() => setPage((p) => p + 1)}
            >
              Next <ChevronRight size={14} />
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
