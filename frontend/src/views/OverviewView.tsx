import React from 'react';
import type { MetricsSummary, RequestEvent } from '../types';
import { DecisionBadge } from '../components/DecisionBadge';
import { RiskBadge } from '../components/RiskBadge';
import {
  ShieldCheck,
  AlertTriangle,
  ShieldAlert,
  Activity,
  Layers,
  ArrowRight
} from 'lucide-react';
import type { TabId } from '../components/Sidebar';

interface Props {
  metrics: MetricsSummary | null;
  recentEvents: RequestEvent[];
  onSelectEvent: (event: RequestEvent) => void;
  onNavigate: (tab: TabId) => void;
}

export const OverviewView: React.FC<Props> = ({
  metrics,
  recentEvents,
  onSelectEvent,
  onNavigate
}) => {
  const total = metrics?.totalRequests || 0;
  const allowed = metrics?.allowed || 0;
  const flagged = metrics?.flagged || 0;
  const blocked = metrics?.blocked || 0;

  const allowPct = total > 0 ? Math.round((allowed / total) * 100) : 0;
  const flagPct = total > 0 ? Math.round((flagged / total) * 100) : 0;
  const blockPct = total > 0 ? Math.round((blocked / total) * 100) : 0;

  // Filter high-risk or flagged events for the recent alert table
  const highRiskEvents = recentEvents.filter(
    (e) => e.decision === 'BLOCK' || e.decision === 'FLAG' || e.riskScore >= 30
  ).slice(0, 6);

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
      {/* KPI Cards Row */}
      <div className="kpi-grid">
        <div className="kpi-card" style={{ '--card-accent': 'var(--primary)' } as React.CSSProperties}>
          <div className="kpi-label">
            <Layers size={14} /> Total Analyzed
          </div>
          <div className="kpi-value">{total.toLocaleString()}</div>
          <div className="kpi-meta">Inspected by Hybrid Gateway</div>
        </div>

        <div className="kpi-card" style={{ '--card-accent': 'var(--allow-green)' } as React.CSSProperties}>
          <div className="kpi-label">
            <ShieldCheck size={14} style={{ color: 'var(--allow-green)' }} /> Allowed
          </div>
          <div className="kpi-value" style={{ color: 'var(--allow-green)' }}>{allowed.toLocaleString()}</div>
          <div className="kpi-meta">{allowPct}% of total volume</div>
        </div>

        <div className="kpi-card" style={{ '--card-accent': 'var(--flag-amber)' } as React.CSSProperties}>
          <div className="kpi-label">
            <AlertTriangle size={14} style={{ color: 'var(--flag-amber)' }} /> Flagged
          </div>
          <div className="kpi-value" style={{ color: 'var(--flag-amber)' }}>{flagged.toLocaleString()}</div>
          <div className="kpi-meta">{flagPct}% requires review</div>
        </div>

        <div className="kpi-card" style={{ '--card-accent': 'var(--block-red)' } as React.CSSProperties}>
          <div className="kpi-label">
            <ShieldAlert size={14} style={{ color: 'var(--block-red)' }} /> Blocked
          </div>
          <div className="kpi-value" style={{ color: 'var(--block-red)' }}>{blocked.toLocaleString()}</div>
          <div className="kpi-meta">{blockPct}% rejected by policy</div>
        </div>

        <div className="kpi-card" style={{ '--card-accent': '#38bdf8' } as React.CSSProperties}>
          <div className="kpi-label">
            <Activity size={14} style={{ color: '#38bdf8' }} /> ML Detector
          </div>
          <div className="kpi-value" style={{ fontSize: '20px', marginTop: '4px' }}>
            {metrics?.mlServiceStatus === 'OK' ? 'ACTIVE' : 'DEGRADED'}
          </div>
          <div className="kpi-meta">Isolation Forest v1</div>
        </div>
      </div>

      {/* Grid: Volume Trends & Decision Distribution */}
      <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: '20px' }}>
        {/* Trend Bar Chart */}
        <div className="card">
          <div className="card-title">
            <span>Traffic Volume Trend</span>
            <span style={{ fontSize: '11px', color: 'var(--text-faint)' }}>10-Minute Windows</span>
          </div>

          {metrics?.timeSeries && metrics.timeSeries.length > 0 ? (
            <div style={{ display: 'flex', alignItems: 'flex-end', gap: '16px', height: '170px', paddingTop: '20px' }}>
              {metrics.timeSeries.map((pt, idx) => {
                const maxVal = Math.max(...metrics.timeSeries.map((p) => p.total), 10);
                const hAllow = Math.max(4, Math.round((pt.allowed / maxVal) * 140));
                const hBlock = Math.round((pt.blocked / maxVal) * 140);
                const hFlag = Math.round((pt.flagged / maxVal) * 140);

                return (
                  <div key={idx} style={{ flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '8px' }}>
                    <div style={{ width: '100%', display: 'flex', alignItems: 'flex-end', justifyContent: 'center', gap: '3px', height: '140px' }}>
                      <div
                        style={{
                          width: '10px',
                          height: `${hAllow}px`,
                          backgroundColor: 'var(--allow-green)',
                          borderRadius: '3px 3px 0 0'
                        }}
                        title={`Allowed: ${pt.allowed}`}
                      />
                      {hFlag > 0 && (
                        <div
                          style={{
                            width: '10px',
                            height: `${hFlag}px`,
                            backgroundColor: 'var(--flag-amber)',
                            borderRadius: '3px 3px 0 0'
                          }}
                          title={`Flagged: ${pt.flagged}`}
                        />
                      )}
                      {hBlock > 0 && (
                        <div
                          style={{
                            width: '10px',
                            height: `${hBlock}px`,
                            backgroundColor: 'var(--block-red)',
                            borderRadius: '3px 3px 0 0'
                          }}
                          title={`Blocked: ${pt.blocked}`}
                        />
                      )}
                    </div>
                    <span style={{ fontSize: '11px', color: 'var(--text-faint)', fontFamily: 'var(--font-mono)' }}>
                      {pt.timestamp}
                    </span>
                  </div>
                );
              })}
            </div>
          ) : (
            <div style={{ height: '170px', display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'var(--text-muted)' }}>
              Awaiting request traffic data...
            </div>
          )}
        </div>

        {/* Decision Ratio Card */}
        <div className="card">
          <div className="card-title">
            <span>Decision Distribution</span>
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '14px', marginTop: '10px' }}>
            <div>
              <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '12px', marginBottom: '6px' }}>
                <span style={{ color: 'var(--allow-green)', fontWeight: 600 }}>ALLOW ({allowed})</span>
                <span>{allowPct}%</span>
              </div>
              <div style={{ height: '8px', background: '#1e293b', borderRadius: '4px', overflow: 'hidden' }}>
                <div style={{ width: `${allowPct}%`, height: '100%', background: 'var(--allow-green)' }} />
              </div>
            </div>

            <div>
              <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '12px', marginBottom: '6px' }}>
                <span style={{ color: 'var(--flag-amber)', fontWeight: 600 }}>FLAG ({flagged})</span>
                <span>{flagPct}%</span>
              </div>
              <div style={{ height: '8px', background: '#1e293b', borderRadius: '4px', overflow: 'hidden' }}>
                <div style={{ width: `${flagPct}%`, height: '100%', background: 'var(--flag-amber)' }} />
              </div>
            </div>

            <div>
              <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '12px', marginBottom: '6px' }}>
                <span style={{ color: 'var(--block-red)', fontWeight: 600 }}>BLOCK ({blocked})</span>
                <span>{blockPct}%</span>
              </div>
              <div style={{ height: '8px', background: '#1e293b', borderRadius: '4px', overflow: 'hidden' }}>
                <div style={{ width: `${blockPct}%`, height: '100%', background: 'var(--block-red)' }} />
              </div>
            </div>

            <div style={{ marginTop: '10px', padding: '10px', background: '#0b1326', borderRadius: '6px', fontSize: '11.5px', color: 'var(--text-muted)' }}>
              Risk Thresholds: <strong>Low: 0-29</strong> &bull; <strong>Med: 30-69</strong> &bull; <strong>High: 70-100</strong>
            </div>
          </div>
        </div>
      </div>

      {/* Recent Flagged & Blocked Security Events */}
      <div className="card">
        <div className="card-title">
          <span>Recent High-Risk & Suspicious Events</span>
          <button className="btn btn-secondary btn-sm" onClick={() => onNavigate('logs')}>
            View All Logs <ArrowRight size={13} />
          </button>
        </div>

        {highRiskEvents.length > 0 ? (
          <div className="data-table-container">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Timestamp</th>
                  <th>Method</th>
                  <th>Path</th>
                  <th>Risk Score</th>
                  <th>Decision</th>
                  <th>Primary Reason</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                {highRiskEvents.map((evt) => (
                  <tr key={evt.requestId}>
                    <td style={{ fontSize: '11px', color: 'var(--text-faint)', fontFamily: 'var(--font-mono)' }}>
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
                    <td style={{ maxWidth: '300px', overflow: 'hidden', textOverflow: 'ellipsis', color: 'var(--text-muted)' }}>
                      {evt.reasons && evt.reasons.length > 0 ? evt.reasons[0] : 'Threshold reached'}
                    </td>
                    <td>
                      <button className="btn btn-secondary btn-sm" onClick={() => onSelectEvent(evt)}>
                        Inspect
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <div style={{ textAlign: 'center', padding: '32px', color: 'var(--text-muted)' }}>
            No high-risk events detected yet. Submit a test attack in the Request Tester to observe real-time detection.
          </div>
        )}
      </div>
    </div>
  );
};
