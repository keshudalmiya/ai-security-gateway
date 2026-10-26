import React, { useState } from 'react';
import type { AnalyzeRequestPayload, AnalyzeResponse } from '../types';
import { gatewayApi } from '../api/gatewayApi';
import { DecisionBadge } from '../components/DecisionBadge';
import { RiskBadge } from '../components/RiskBadge';
import {
  Send,
  Zap,
  CheckCircle,
  Cpu
} from 'lucide-react';

interface Props {
  onEventCreated: () => void;
}

export const TesterView: React.FC<Props> = ({ onEventCreated }) => {
  const [method, setMethod] = useState('GET');
  const [path, setPath] = useState('/api/products');
  const [query, setQuery] = useState('category=electronics&page=1');
  const [body, setBody] = useState('');
  const [rate, setRate] = useState(1.0);

  const [isLoading, setIsLoading] = useState(false);
  const [result, setResult] = useState<AnalyzeResponse | null>(null);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);

  const presets = [
    {
      label: 'Benign API Query (Allow)',
      method: 'GET',
      path: '/api/products',
      query: 'category=books&limit=10&sort=asc',
      body: '',
      rate: 1.0
    },
    {
      label: 'SQL Injection Attack (Block)',
      method: 'GET',
      path: '/api/users',
      query: "id=1' UNION SELECT username, password FROM users--",
      body: '',
      rate: 2.0
    },
    {
      label: 'Cross-Site Scripting Attack (Block)',
      method: 'POST',
      path: '/api/comments',
      query: '',
      body: '{"author": "victim", "comment": "<script>alert(\'XSS\')</script>"}',
      rate: 1.0
    },
    {
      label: 'Path Traversal Indicator (Flag)',
      method: 'GET',
      path: '/api/files/download',
      query: 'file=../../../../etc/passwd',
      body: '',
      rate: 1.0
    },
    {
      label: 'Suspicious Rate Spike (Flag)',
      method: 'GET',
      path: '/api/status',
      query: '',
      body: '',
      rate: 140.0
    }
  ];

  const handleApplyPreset = (p: typeof presets[0]) => {
    setMethod(p.method);
    setPath(p.path);
    setQuery(p.query);
    setBody(p.body);
    setRate(p.rate);
    setResult(null);
    setErrorMsg(null);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    setErrorMsg(null);

    const payload: AnalyzeRequestPayload = {
      method,
      path: path.startsWith('/') ? path : `/${path}`,
      query,
      headers: {
        'Accept': 'application/json',
        'User-Agent': 'Security-Tester/1.0'
      },
      body
    };

    try {
      const res = await gatewayApi.analyze(payload, rate);
      setResult(res);
      onEventCreated();
    } catch (err: any) {
      setErrorMsg(err.message || 'Analysis request failed.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div style={{ display: 'grid', gridTemplateColumns: '1.2fr 1fr', gap: '24px' }}>
      {/* Request Configuration Form */}
      <div className="card">
        <div className="card-title">
          <span>Request Inspector & Attack Simulator</span>
        </div>

        {/* Preset Selector */}
        <div style={{ marginBottom: '18px' }}>
          <div style={{ fontSize: '11px', fontWeight: 600, color: 'var(--text-muted)', marginBottom: '8px', textTransform: 'uppercase' }}>
            Quick Load Scenario
          </div>
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: '6px' }}>
            {presets.map((p, idx) => (
              <button
                key={idx}
                type="button"
                className="btn btn-secondary btn-sm"
                onClick={() => handleApplyPreset(p)}
              >
                <Zap size={11} style={{ color: 'var(--primary)' }} />
                {p.label}
              </button>
            ))}
          </div>
        </div>

        <form onSubmit={handleSubmit}>
          <div style={{ display: 'grid', gridTemplateColumns: '120px 1fr', gap: '12px' }}>
            <div className="form-group">
              <label className="form-label">HTTP Method</label>
              <select className="form-select" value={method} onChange={(e) => setMethod(e.target.value)}>
                <option value="GET">GET</option>
                <option value="POST">POST</option>
                <option value="PUT">PUT</option>
                <option value="PATCH">PATCH</option>
                <option value="DELETE">DELETE</option>
                <option value="OPTIONS">OPTIONS</option>
              </select>
            </div>

            <div className="form-group">
              <label className="form-label">Path (starts with /)</label>
              <input
                type="text"
                className="form-input"
                value={path}
                onChange={(e) => setPath(e.target.value)}
                placeholder="/api/products"
                required
              />
            </div>
          </div>

          <div className="form-group">
            <label className="form-label">Query String Parameters</label>
            <input
              type="text"
              className="form-input"
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              placeholder="id=1&name=test"
            />
          </div>

          <div className="form-group">
            <label className="form-label">
              Request Payload Body <span style={{ fontSize: '11px', color: 'var(--text-faint)' }}>(JSON / Text)</span>
            </label>
            <textarea
              className="form-textarea"
              rows={4}
              value={body}
              onChange={(e) => setBody(e.target.value)}
              placeholder="Sample request body (never submit real credentials)"
            />
          </div>

          <div className="form-group">
            <label className="form-label">Client Request Rate (req/min)</label>
            <input
              type="number"
              step="1"
              min="1"
              max="1000"
              className="form-input"
              value={rate}
              onChange={(e) => setRate(parseFloat(e.target.value) || 1)}
            />
          </div>

          {errorMsg && (
            <div
              style={{
                padding: '10px 14px',
                borderRadius: '6px',
                background: 'rgba(239, 68, 68, 0.15)',
                color: 'var(--block-red)',
                border: '1px solid rgba(239, 68, 68, 0.3)',
                fontSize: '13px',
                marginBottom: '16px'
              }}
            >
              {errorMsg}
            </div>
          )}

          <button type="submit" className="btn btn-primary" style={{ width: '100%' }} disabled={isLoading}>
            <Send size={15} />
            {isLoading ? 'Analyzing Signals...' : 'Analyze Request'}
          </button>
        </form>
      </div>

      {/* Real-time Analysis Decision Result */}
      <div className="card">
        <div className="card-title">
          <span>Inspection Outcome</span>
          {result && <span style={{ fontSize: '11px', color: 'var(--text-faint)' }}>{result.latencyMs} ms total</span>}
        </div>

        {result ? (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
            {/* Top Decision Banner */}
            <div
              style={{
                padding: '16px',
                borderRadius: '8px',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                background:
                  result.decision === 'ALLOW'
                    ? 'rgba(16, 185, 129, 0.12)'
                    : result.decision === 'FLAG'
                    ? 'rgba(245, 158, 11, 0.12)'
                    : 'rgba(239, 68, 68, 0.12)',
                border: `1px solid ${
                  result.decision === 'ALLOW'
                    ? 'var(--allow-border)'
                    : result.decision === 'FLAG'
                    ? 'var(--flag-border)'
                    : 'var(--block-border)'
                }`
              }}
            >
              <div>
                <div style={{ fontSize: '11px', fontWeight: 600, color: 'var(--text-faint)', textTransform: 'uppercase' }}>
                  Gateway Action
                </div>
                <div style={{ marginTop: '4px' }}>
                  <DecisionBadge decision={result.decision} />
                </div>
              </div>

              <div style={{ textAlign: 'right' }}>
                <div style={{ fontSize: '11px', fontWeight: 600, color: 'var(--text-faint)', textTransform: 'uppercase' }}>
                  Hybrid Risk Score
                </div>
                <div style={{ fontSize: '24px', fontWeight: 800 }}>
                  <RiskBadge riskLevel={result.riskLevel} score={result.riskScore} />
                </div>
              </div>
            </div>

            {/* Risk Gauge Progress */}
            <div>
              <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '12px', marginBottom: '6px' }}>
                <span style={{ color: 'var(--text-muted)' }}>Risk Assessment: {result.riskLevel}</span>
                <span style={{ fontWeight: 700 }}>{result.riskScore} / 100</span>
              </div>
              <div style={{ height: '8px', background: '#1e293b', borderRadius: '4px', overflow: 'hidden' }}>
                <div
                  style={{
                    width: `${Math.min(100, result.riskScore)}%`,
                    height: '100%',
                    background:
                      result.riskScore < 30
                        ? 'var(--allow-green)'
                        : result.riskScore < 70
                        ? 'var(--flag-amber)'
                        : 'var(--block-red)',
                    transition: 'width 0.4s ease'
                  }}
                />
              </div>
            </div>

            {/* Decision Reasons */}
            <div>
              <div style={{ fontSize: '12px', fontWeight: 600, color: 'var(--text-muted)', marginBottom: '6px' }}>
                Policy Reasons
              </div>
              <ul style={{ listStyle: 'none', display: 'flex', flexDirection: 'column', gap: '4px' }}>
                {result.reasons?.map((r, i) => (
                  <li key={i} style={{ fontSize: '12.5px', color: 'var(--text-main)', display: 'flex', gap: '6px' }}>
                    <span style={{ color: 'var(--primary)' }}>&bull;</span>
                    <span>{r}</span>
                  </li>
                ))}
              </ul>
            </div>

            {/* Rule Matches */}
            <div>
              <div style={{ fontSize: '12px', fontWeight: 600, color: 'var(--text-muted)', marginBottom: '6px' }}>
                Matched Rule Signatures
              </div>
              {result.matchedRules && result.matchedRules.length > 0 ? (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
                  {result.matchedRules.map((m, idx) => (
                    <div
                      key={idx}
                      style={{
                        padding: '8px 12px',
                        borderRadius: '6px',
                        background: 'rgba(239, 68, 68, 0.08)',
                        border: '1px solid rgba(239, 68, 68, 0.25)',
                        fontSize: '12px'
                      }}
                    >
                      <div style={{ fontWeight: 600, color: 'var(--block-red)' }}>
                        [{m.ruleId}] {m.name} ({m.severity})
                      </div>
                      <div style={{ color: 'var(--text-muted)', fontSize: '11.5px', marginTop: '2px' }}>{m.reason}</div>
                    </div>
                  ))}
                </div>
              ) : (
                <div style={{ fontSize: '12.5px', color: 'var(--allow-green)', display: 'flex', alignItems: 'center', gap: '6px' }}>
                  <CheckCircle size={14} /> No rule patterns triggered.
                </div>
              )}
            </div>

            {/* ML Detector Diagnostic */}
            <div style={{ padding: '12px', background: '#0b1326', borderRadius: '8px' }}>
              <div style={{ fontSize: '11px', fontWeight: 600, color: 'var(--text-faint)', marginBottom: '8px', textTransform: 'uppercase' }}>
                Machine Learning Diagnostics
              </div>
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '10px', fontSize: '12px' }}>
                <div>
                  <span style={{ color: 'var(--text-faint)' }}>ML Status:</span>{' '}
                  <strong style={{ color: 'var(--primary)' }}>{result.ml?.status}</strong>
                </div>
                <div>
                  <span style={{ color: 'var(--text-faint)' }}>Anomaly:</span>{' '}
                  <strong>{result.ml?.isAnomaly ? 'YES' : 'NO'}</strong>
                </div>
                <div>
                  <span style={{ color: 'var(--text-faint)' }}>Score:</span>{' '}
                  <strong>{result.ml?.anomalyScore?.toFixed(3)}</strong>
                </div>
              </div>
            </div>

            <div style={{ fontSize: '11px', color: 'var(--text-faint)', fontFamily: 'var(--font-mono)' }}>
              Request ID: {result.requestId}
            </div>
          </div>
        ) : (
          <div style={{ textAlign: 'center', padding: '60px 20px', color: 'var(--text-muted)' }}>
            <Cpu size={36} style={{ color: 'var(--text-faint)', marginBottom: '12px' }} />
            <div style={{ fontSize: '14px', fontWeight: 500 }}>Awaiting Request Submission</div>
            <div style={{ fontSize: '12px', color: 'var(--text-faint)', marginTop: '4px' }}>
              Select a scenario preset on the left or enter a custom HTTP request to run hybrid inspection.
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
