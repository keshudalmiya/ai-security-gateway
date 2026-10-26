import React, { useState } from 'react';
import type { SystemHealth } from '../types';
import { Activity, Server, Database, Cpu, Settings, Copy, Check } from 'lucide-react';

interface Props {
  health: SystemHealth | null;
  onRefresh: () => void;
  isLoading: boolean;
}

export const HealthView: React.FC<Props> = ({ health, onRefresh, isLoading }) => {
  const [copied, setCopied] = useState(false);

  const handleCopyDiagnostics = () => {
    if (!health) return;
    navigator.clipboard.writeText(JSON.stringify(health, null, 2));
    setCopied(true);
    setTimeout(() => setCopied(false), 2500);
  };

  const gwUp = health?.gateway?.status === 'UP';
  const mlUp = health?.mlService?.status === 'OK';
  const dbUp = health?.database?.status === 'UP';

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
      {/* Top Banner */}
      <div className="card" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <Activity size={20} style={{ color: 'var(--primary)' }} />
            <h2 style={{ fontSize: '16px', fontWeight: 700 }}>System Health & Diagnostics</h2>
          </div>
          <p style={{ fontSize: '13px', color: 'var(--text-muted)', marginTop: '4px' }}>
            Live status of distributed microservices, machine learning model pipeline, and persistence layer.
          </p>
        </div>

        <div style={{ display: 'flex', gap: '10px' }}>
          <button className="btn btn-secondary btn-sm" onClick={handleCopyDiagnostics}>
            {copied ? <Check size={14} style={{ color: 'var(--allow-green)' }} /> : <Copy size={14} />}
            {copied ? 'Copied Diagnostics' : 'Copy Diagnostics JSON'}
          </button>
          <button className="btn btn-primary btn-sm" onClick={onRefresh} disabled={isLoading}>
            Refresh Health Checks
          </button>
        </div>
      </div>

      {/* Component Status Cards Grid */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '20px' }}>
        {/* Gateway Card */}
        <div className="card">
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '16px' }}>
            <Server size={20} style={{ color: 'var(--primary)' }} />
            <div>
              <div style={{ fontSize: '14px', fontWeight: 700 }}>Spring Boot Gateway</div>
              <div style={{ fontSize: '11px', color: 'var(--text-faint)' }}>Port: 8080 &bull; REST API</div>
            </div>
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '8px', fontSize: '12.5px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between' }}>
              <span style={{ color: 'var(--text-muted)' }}>Status:</span>
              <strong style={{ color: gwUp ? 'var(--allow-green)' : 'var(--block-red)' }}>
                {gwUp ? 'OPERATIONAL' : 'DOWN'}
              </strong>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between' }}>
              <span style={{ color: 'var(--text-muted)' }}>Uptime:</span>
              <span>{health?.gateway?.uptimeSeconds || 0} seconds</span>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between' }}>
              <span style={{ color: 'var(--text-muted)' }}>Framework:</span>
              <span>Spring Boot 3.3.5 / Java 21</span>
            </div>
          </div>
        </div>

        {/* ML Inference Service Card */}
        <div className="card">
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '16px' }}>
            <Cpu size={20} style={{ color: '#a855f7' }} />
            <div>
              <div style={{ fontSize: '14px', fontWeight: 700 }}>FastAPI ML Service</div>
              <div style={{ fontSize: '11px', color: 'var(--text-faint)' }}>Port: 8000 &bull; Uvicorn</div>
            </div>
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '8px', fontSize: '12.5px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between' }}>
              <span style={{ color: 'var(--text-muted)' }}>Status:</span>
              <strong style={{ color: mlUp ? 'var(--allow-green)' : 'var(--flag-amber)' }}>
                {mlUp ? 'OPERATIONAL' : 'DEGRADED / OFFLINE'}
              </strong>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between' }}>
              <span style={{ color: 'var(--text-muted)' }}>Model Artifact:</span>
              <span>{health?.mlService?.modelVersion || 'iforest-csic-v1'}</span>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between' }}>
              <span style={{ color: 'var(--text-muted)' }}>Algorithm:</span>
              <span>Scikit-learn Isolation Forest</span>
            </div>
          </div>
        </div>

        {/* Database Storage Card */}
        <div className="card">
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '16px' }}>
            <Database size={20} style={{ color: '#10b981' }} />
            <div>
              <div style={{ fontSize: '14px', fontWeight: 700 }}>Persistence Layer</div>
              <div style={{ fontSize: '11px', color: 'var(--text-faint)' }}>Port: 27017 / In-Memory Fallback</div>
            </div>
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '8px', fontSize: '12.5px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between' }}>
              <span style={{ color: 'var(--text-muted)' }}>Status:</span>
              <strong style={{ color: dbUp ? 'var(--allow-green)' : 'var(--flag-amber)' }}>
                {dbUp ? 'MONGODB CONNECTED' : 'IN-MEMORY BUFFER'}
              </strong>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between' }}>
              <span style={{ color: 'var(--text-muted)' }}>Mode:</span>
              <span>{health?.database?.fallbackActive ? 'Resilient Circular Ring' : 'MongoDB Document Store'}</span>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between' }}>
              <span style={{ color: 'var(--text-muted)' }}>Data Loss Protection:</span>
              <span style={{ color: 'var(--allow-green)' }}>ACTIVE</span>
            </div>
          </div>
        </div>
      </div>

      {/* Security & Risk Thresholds Config */}
      <div className="card">
        <div className="card-title">
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <Settings size={18} style={{ color: 'var(--primary)' }} />
            <span>Active Security Gateway Policies</span>
          </div>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '16px', fontSize: '13px' }}>
          <div style={{ padding: '14px', background: '#0b1326', borderRadius: '8px' }}>
            <div style={{ fontWeight: 600, color: 'var(--text-main)', marginBottom: '4px' }}>Hybrid Risk Decision Policy</div>
            <div style={{ color: 'var(--text-muted)', fontSize: '12.5px' }}>
              &bull; <strong>0 - 29 (LOW):</strong> Request permitted (ALLOW)<br />
              &bull; <strong>30 - 69 (MEDIUM):</strong> Suspicious, logged for review (FLAG)<br />
              &bull; <strong>70 - 100 (HIGH):</strong> Rejected by policy (BLOCK)<br />
              &bull; <strong>CRITICAL Rules:</strong> Immediate mandatory BLOCK override
            </div>
          </div>

          <div style={{ padding: '14px', background: '#0b1326', borderRadius: '8px' }}>
            <div style={{ fontWeight: 600, color: 'var(--text-main)', marginBottom: '4px' }}>Fail-Safe Policies & Limits</div>
            <div style={{ color: 'var(--text-muted)', fontSize: '12.5px' }}>
              &bull; <strong>ML Timeout:</strong> {health?.config?.mlTimeoutMs || 1500} ms (fallback to FLAG)<br />
              &bull; <strong>Max Request Body:</strong> {health?.config?.maxRequestBodyBytes || 65536} bytes<br />
              &bull; <strong>Demo Upstream:</strong> {health?.config?.demoUpstreamUrl || 'Local Mock Upstream'}<br />
              &bull; <strong>Proxying Constraint:</strong> Arbitrary external host forwarding disabled
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
