import React from 'react';
import type { RequestEvent } from '../types';
import { DecisionBadge } from './DecisionBadge';
import { RiskBadge } from './RiskBadge';
import { X, CheckCircle } from 'lucide-react';

interface Props {
  event: RequestEvent | null;
  onClose: () => void;
}

export const RequestDetailModal: React.FC<Props> = ({ event, onClose }) => {
  if (!event) return null;

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
              <h2 style={{ fontSize: '17px', fontWeight: 700 }}>Security Event Analysis</h2>
              <DecisionBadge decision={event.decision} />
              <RiskBadge riskLevel={event.riskLevel} score={event.riskScore} />
            </div>
            <div style={{ fontSize: '11px', color: 'var(--text-faint)', fontFamily: 'var(--font-mono)', marginTop: '4px' }}>
              ID: {event.requestId} &bull; {event.createdAt}
            </div>
          </div>
          <button className="refresh-btn" onClick={onClose}>
            <X size={16} />
          </button>
        </div>

        <div className="modal-body">
          {/* Decision & Explanations */}
          <div className="card" style={{ padding: '16px' }}>
            <h3 style={{ fontSize: '13px', color: 'var(--text-muted)', marginBottom: '8px', textTransform: 'uppercase' }}>
              Auditable Decision Explanation
            </h3>
            <ul style={{ listStyle: 'none', display: 'flex', flexDirection: 'column', gap: '6px' }}>
              {event.reasons && event.reasons.length > 0 ? (
                event.reasons.map((r, i) => (
                  <li key={i} style={{ fontSize: '13px', display: 'flex', alignItems: 'flex-start', gap: '8px' }}>
                    <span style={{ color: 'var(--primary)' }}>&bull;</span>
                    <span>{r}</span>
                  </li>
                ))
              ) : (
                <li style={{ fontSize: '13px', color: 'var(--text-muted)' }}>Request was inspected and passed all baseline thresholds.</li>
              )}
            </ul>
          </div>

          {/* Rule-Based Matches */}
          <div className="card" style={{ padding: '16px' }}>
            <h3 style={{ fontSize: '13px', color: 'var(--text-muted)', marginBottom: '10px', textTransform: 'uppercase' }}>
              Rule-Based Detection Findings
            </h3>
            {event.matchedRules && event.matchedRules.length > 0 ? (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                {event.matchedRules.map((rule, idx) => (
                  <div
                    key={idx}
                    style={{
                      padding: '10px 14px',
                      borderRadius: '6px',
                      background: 'rgba(239, 68, 68, 0.08)',
                      border: '1px solid rgba(239, 68, 68, 0.25)',
                      display: 'flex',
                      flexDirection: 'column',
                      gap: '4px'
                    }}
                  >
                    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                      <span style={{ fontWeight: 600, color: 'var(--block-red)', fontSize: '13px' }}>
                        [{rule.ruleId}] {rule.name}
                      </span>
                      <span className="badge badge-block" style={{ fontSize: '10px' }}>{rule.severity}</span>
                    </div>
                    <div style={{ fontSize: '12px', color: 'var(--text-muted)' }}>{rule.reason}</div>
                  </div>
                ))}
              </div>
            ) : (
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px', color: 'var(--allow-green)', fontSize: '13px' }}>
                <CheckCircle size={15} />
                <span>No deterministic security rule violations triggered.</span>
              </div>
            )}
          </div>

          {/* Machine Learning Model Diagnostics */}
          <div className="card" style={{ padding: '16px' }}>
            <h3 style={{ fontSize: '13px', color: 'var(--text-muted)', marginBottom: '10px', textTransform: 'uppercase' }}>
              Machine Learning (Isolation Forest) Anomaly Analysis
            </h3>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '12px', textAlign: 'center' }}>
              <div style={{ background: '#0b1326', padding: '10px', borderRadius: '6px' }}>
                <div style={{ fontSize: '11px', color: 'var(--text-faint)' }}>STATUS</div>
                <div style={{ fontSize: '14px', fontWeight: 700, color: 'var(--primary)' }}>{event.ml?.status || 'N/A'}</div>
              </div>
              <div style={{ background: '#0b1326', padding: '10px', borderRadius: '6px' }}>
                <div style={{ fontSize: '11px', color: 'var(--text-faint)' }}>ANOMALY</div>
                <div style={{ fontSize: '14px', fontWeight: 700, color: event.ml?.isAnomaly ? 'var(--block-red)' : 'var(--allow-green)' }}>
                  {event.ml?.isAnomaly ? 'DETECTED' : 'NORMAL'}
                </div>
              </div>
              <div style={{ background: '#0b1326', padding: '10px', borderRadius: '6px' }}>
                <div style={{ fontSize: '11px', color: 'var(--text-faint)' }}>SCORE (0-1)</div>
                <div style={{ fontSize: '14px', fontWeight: 700 }}>
                  {event.ml?.anomalyScore !== undefined ? event.ml.anomalyScore.toFixed(3) : '0.000'}
                </div>
              </div>
              <div style={{ background: '#0b1326', padding: '10px', borderRadius: '6px' }}>
                <div style={{ fontSize: '11px', color: 'var(--text-faint)' }}>ML LATENCY</div>
                <div style={{ fontSize: '14px', fontWeight: 700 }}>
                  {event.ml?.latencyMs ? `${event.ml.latencyMs.toFixed(1)} ms` : '< 1 ms'}
                </div>
              </div>
            </div>
          </div>

          {/* Extracted Feature Vector */}
          <div className="card" style={{ padding: '16px' }}>
            <h3 style={{ fontSize: '13px', color: 'var(--text-muted)', marginBottom: '10px', textTransform: 'uppercase' }}>
              Extracted 8-Dimensional Numerical Feature Vector
            </h3>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '8px', fontSize: '12px' }}>
              <div><span style={{ color: 'var(--text-faint)' }}>Request Length:</span> <strong>{event.requestLength} B</strong></div>
              <div><span style={{ color: 'var(--text-faint)' }}>URL Length:</span> <strong>{event.urlLength} chars</strong></div>
              <div><span style={{ color: 'var(--text-faint)' }}>Parameter Count:</span> <strong>{event.parameterCount}</strong></div>
              <div><span style={{ color: 'var(--text-faint)' }}>Special Chars:</span> <strong>{event.specialCharCount}</strong></div>
              <div><span style={{ color: 'var(--text-faint)' }}>Body Length:</span> <strong>{event.bodyLength} B</strong></div>
              <div><span style={{ color: 'var(--text-faint)' }}>Header Count:</span> <strong>{event.headerCount}</strong></div>
              <div><span style={{ color: 'var(--text-faint)' }}>Total Latency:</span> <strong>{event.totalLatencyMs?.toFixed(2)} ms</strong></div>
              <div><span style={{ color: 'var(--text-faint)' }}>Detector Mode:</span> <strong>{event.detectorMode}</strong></div>
            </div>
          </div>

          {/* Request Metadata Preview */}
          <div className="card" style={{ padding: '16px' }}>
            <h3 style={{ fontSize: '13px', color: 'var(--text-muted)', marginBottom: '8px', textTransform: 'uppercase' }}>
              Sanitized Target URI
            </h3>
            <div
              style={{
                fontFamily: 'var(--font-mono)',
                fontSize: '12px',
                background: '#080d1a',
                padding: '10px 14px',
                borderRadius: '6px',
                wordBreak: 'break-all',
                color: '#38bdf8'
              }}
            >
              {event.method} {event.path}{event.querySummary ? `?${event.querySummary}` : ''}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
