import React, { useState, useEffect } from 'react';
import type { SecurityRule } from '../types';
import { gatewayApi } from '../api/gatewayApi';
import { Sliders, Check, RefreshCw } from 'lucide-react';

export const RulesView: React.FC = () => {
  const [rules, setRules] = useState<SecurityRule[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [statusMsg, setStatusMsg] = useState<string | null>(null);

  const fetchRules = async () => {
    setIsLoading(true);
    try {
      const data = await gatewayApi.getRules();
      setRules(data);
    } catch (e) {
      console.error('Failed to fetch rules:', e);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchRules();
  }, []);

  const handleToggle = async (ruleId: string, currentEnabled: boolean) => {
    try {
      const updated = await gatewayApi.updateRule(ruleId, !currentEnabled);
      setRules((prev) => prev.map((r) => (r.ruleId === ruleId ? updated : r)));
      setStatusMsg(`Rule [${ruleId}] has been ${!currentEnabled ? 'ENABLED' : 'DISABLED'}. Audit event recorded.`);
      setTimeout(() => setStatusMsg(null), 4000);
    } catch (e: any) {
      alert(`Failed to update rule: ${e.message}`);
    }
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
      <div className="card">
        <div className="card-title">
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <Sliders size={18} style={{ color: 'var(--primary)' }} />
            <span>Configurable Signature Rules</span>
          </div>
          <button className="btn btn-secondary btn-sm" onClick={fetchRules}>
            <RefreshCw size={13} className={isLoading ? 'spin' : ''} /> Refresh
          </button>
        </div>
        <p style={{ fontSize: '13px', color: 'var(--text-muted)' }}>
          Manage deterministic pattern inspection rules. Toggling a rule immediately influences the Hybrid Decision Engine and records an auditable event in the database.
        </p>

        {statusMsg && (
          <div
            style={{
              padding: '10px 14px',
              borderRadius: '6px',
              background: 'rgba(56, 189, 248, 0.12)',
              border: '1px solid rgba(56, 189, 248, 0.3)',
              color: 'var(--primary)',
              fontSize: '12.5px',
              marginTop: '14px',
              display: 'flex',
              alignItems: 'center',
              gap: '8px'
            }}
          >
            <Check size={14} /> {statusMsg}
          </div>
        )}
      </div>

      <div className="card" style={{ padding: 0, overflow: 'hidden' }}>
        <div className="data-table-container" style={{ border: 'none' }}>
          <table className="data-table">
            <thead>
              <tr>
                <th>Rule ID</th>
                <th>Rule Title</th>
                <th>Category</th>
                <th>Severity</th>
                <th>Description</th>
                <th>Status</th>
                <th>Toggle Action</th>
              </tr>
            </thead>
            <tbody>
              {rules.length > 0 ? (
                rules.map((rule) => (
                  <tr key={rule.ruleId}>
                    <td className="mono-cell" style={{ fontWeight: 700 }}>
                      {rule.ruleId}
                    </td>
                    <td style={{ fontWeight: 600 }}>{rule.name}</td>
                    <td>
                      <span className="badge badge-method">{rule.category}</span>
                    </td>
                    <td>
                      <span
                        className={`badge ${
                          rule.severity === 'CRITICAL' || rule.severity === 'HIGH'
                            ? 'badge-block'
                            : rule.severity === 'MEDIUM'
                            ? 'badge-flag'
                            : 'badge-allow'
                        }`}
                      >
                        {rule.severity}
                      </span>
                    </td>
                    <td style={{ color: 'var(--text-muted)', fontSize: '12px', maxWidth: '300px' }}>
                      {rule.description}
                    </td>
                    <td>
                      <span
                        style={{
                          fontSize: '12px',
                          fontWeight: 600,
                          color: rule.enabled ? 'var(--allow-green)' : 'var(--text-faint)'
                        }}
                      >
                        {rule.enabled ? 'ACTIVE' : 'DISABLED'}
                      </span>
                    </td>
                    <td>
                      <button
                        className={`btn btn-sm ${rule.enabled ? 'btn-secondary' : 'btn-primary'}`}
                        onClick={() => handleToggle(rule.ruleId, rule.enabled)}
                      >
                        {rule.enabled ? 'Disable' : 'Enable'}
                      </button>
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan={7} style={{ textAlign: 'center', padding: '36px', color: 'var(--text-muted)' }}>
                    Loading detection rules...
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};
