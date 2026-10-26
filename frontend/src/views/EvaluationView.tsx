import React, { useState, useEffect } from 'react';
import type { EvaluationRun } from '../types';
import { gatewayApi } from '../api/gatewayApi';
import { Download, Award, Shield, Cpu, Activity, RefreshCw } from 'lucide-react';

export const EvaluationView: React.FC = () => {
  const [evaluation, setEvaluation] = useState<EvaluationRun | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  const fetchEvaluation = async () => {
    setIsLoading(true);
    try {
      const data = await gatewayApi.getLatestEvaluation();
      setEvaluation(data);
    } catch (e) {
      console.error('Failed to load evaluation:', e);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchEvaluation();
  }, []);

  const handleExportCsv = () => {
    if (!evaluation) return;
    const m = evaluation.metrics;
    const rows = [
      ['Approach', 'Precision', 'Recall', 'F1_Score', 'False_Positive_Count', 'False_Positive_Rate', 'Avg_Latency_ms', 'P95_Latency_ms'],
      ['Rule-Based', m.ruleBased.precision, m.ruleBased.recall, m.ruleBased.f1, m.ruleBased.falsePositiveCount, m.ruleBased.falsePositiveRate, m.ruleBased.avgLatencyMs, m.ruleBased.p95LatencyMs],
      ['ML-Based (Isolation Forest)', m.mlBased.precision, m.mlBased.recall, m.mlBased.f1, m.mlBased.falsePositiveCount, m.mlBased.falsePositiveRate, m.mlBased.avgLatencyMs, m.mlBased.p95LatencyMs],
      ['Hybrid Framework', m.hybrid.precision, m.hybrid.recall, m.hybrid.f1, m.hybrid.falsePositiveCount, m.hybrid.falsePositiveRate, m.hybrid.avgLatencyMs, m.hybrid.p95LatencyMs]
    ];
    const csvContent = 'data:text/csv;charset=utf-8,' + rows.map((r) => r.join(',')).join('\n');
    const encoded = encodeURI(csvContent);
    const link = document.createElement('a');
    link.setAttribute('href', encoded);
    link.setAttribute('download', `csic2010_benchmark_evaluation.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  if (!evaluation) {
    return (
      <div className="card" style={{ textAlign: 'center', padding: '60px 20px' }}>
        <Award size={40} style={{ color: 'var(--text-faint)', marginBottom: '16px' }} />
        <h3 style={{ fontSize: '16px', fontWeight: 600 }}>No Evaluation Results Loaded</h3>
        <p style={{ color: 'var(--text-muted)', fontSize: '13px', marginTop: '6px' }}>
          Execute <code>python ml/scripts/evaluate.py</code> to run the offline benchmark against the CSIC 2010 held-out test set.
        </p>
        <button className="btn btn-secondary" style={{ marginTop: '16px' }} onClick={fetchEvaluation}>
          <RefreshCw size={14} className={isLoading ? 'spin' : ''} /> Reload Evaluation Data
        </button>
      </div>
    );
  }

  const { metrics } = evaluation;

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
      {/* Header Info Banner */}
      <div className="card" style={{ background: 'linear-gradient(135deg, #0d1933 0%, #0f172a 100%)', border: '1px solid #1e293b' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '16px' }}>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
              <Award size={22} style={{ color: 'var(--primary)' }} />
              <h2 style={{ fontSize: '18px', fontWeight: 700 }}>Academic Benchmark: CSIC 2010 Held-out Test Set</h2>
            </div>
            <p style={{ color: 'var(--text-muted)', fontSize: '13px', marginTop: '6px' }}>
              Reproducible multi-detector evaluation comparing Rule-Based, ML-Based (Isolation Forest), and Adaptive Hybrid Framework.
            </p>
            <div style={{ display: 'flex', gap: '16px', marginTop: '12px', fontSize: '12px', color: 'var(--text-faint)' }}>
              <span>Total Test Set: <strong>{evaluation.testSampleCount?.toLocaleString()} samples</strong></span>
              <span>Normal Traffic: <strong>{evaluation.normalSampleCount?.toLocaleString()}</strong></span>
              <span>Anomalous Traffic: <strong>{evaluation.anomalousSampleCount?.toLocaleString()}</strong></span>
              <span>Model: <strong>{evaluation.modelVersion}</strong></span>
            </div>
          </div>

          <button className="btn btn-secondary btn-sm" onClick={handleExportCsv}>
            <Download size={14} /> Export Benchmark CSV
          </button>
        </div>
      </div>

      {/* Head-to-Head Comparison Table */}
      <div className="card">
        <div className="card-title">
          <span>Multi-Detector Performance Comparison</span>
        </div>

        <div className="data-table-container">
          <table className="data-table">
            <thead>
              <tr>
                <th>Detection Approach</th>
                <th>Precision</th>
                <th>Recall</th>
                <th>F1-Score</th>
                <th>FP Count</th>
                <th>False Positive Rate</th>
                <th>Avg Latency</th>
                <th>P95 Latency</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td style={{ fontWeight: 600, display: 'flex', alignItems: 'center', gap: '8px' }}>
                  <Shield size={16} style={{ color: 'var(--primary)' }} /> Rule-Based Engine
                </td>
                <td style={{ color: 'var(--allow-green)', fontWeight: 700 }}>{(metrics.ruleBased.precision * 100).toFixed(1)}%</td>
                <td>{(metrics.ruleBased.recall * 100).toFixed(1)}%</td>
                <td>{(metrics.ruleBased.f1 * 100).toFixed(1)}%</td>
                <td>{metrics.ruleBased.falsePositiveCount}</td>
                <td>{(metrics.ruleBased.falsePositiveRate * 100).toFixed(2)}%</td>
                <td className="mono-cell">{metrics.ruleBased.avgLatencyMs.toFixed(2)} ms</td>
                <td className="mono-cell">{metrics.ruleBased.p95LatencyMs.toFixed(2)} ms</td>
              </tr>

              <tr>
                <td style={{ fontWeight: 600, display: 'flex', alignItems: 'center', gap: '8px' }}>
                  <Cpu size={16} style={{ color: '#a855f7' }} /> ML-Based (Isolation Forest)
                </td>
                <td>{(metrics.mlBased.precision * 100).toFixed(1)}%</td>
                <td style={{ color: 'var(--allow-green)', fontWeight: 700 }}>{(metrics.mlBased.recall * 100).toFixed(1)}%</td>
                <td style={{ color: 'var(--allow-green)', fontWeight: 700 }}>{(metrics.mlBased.f1 * 100).toFixed(1)}%</td>
                <td style={{ color: 'var(--flag-amber)' }}>{metrics.mlBased.falsePositiveCount}</td>
                <td style={{ color: 'var(--flag-amber)' }}>{(metrics.mlBased.falsePositiveRate * 100).toFixed(2)}%</td>
                <td className="mono-cell">{metrics.mlBased.avgLatencyMs.toFixed(2)} ms</td>
                <td className="mono-cell">{metrics.mlBased.p95LatencyMs.toFixed(2)} ms</td>
              </tr>

              <tr style={{ background: 'rgba(56, 189, 248, 0.05)' }}>
                <td style={{ fontWeight: 700, color: 'var(--primary)', display: 'flex', alignItems: 'center', gap: '8px' }}>
                  <Activity size={16} style={{ color: 'var(--primary)' }} /> Hybrid Framework (Our Approach)
                </td>
                <td style={{ color: 'var(--allow-green)', fontWeight: 700 }}>{(metrics.hybrid.precision * 100).toFixed(1)}%</td>
                <td>{(metrics.hybrid.recall * 100).toFixed(1)}%</td>
                <td style={{ fontWeight: 700 }}>{(metrics.hybrid.f1 * 100).toFixed(1)}%</td>
                <td style={{ color: 'var(--allow-green)', fontWeight: 700 }}>{metrics.hybrid.falsePositiveCount}</td>
                <td style={{ color: 'var(--allow-green)', fontWeight: 700 }}>{(metrics.hybrid.falsePositiveRate * 100).toFixed(2)}%</td>
                <td className="mono-cell" style={{ fontWeight: 700 }}>{metrics.hybrid.avgLatencyMs.toFixed(2)} ms</td>
                <td className="mono-cell">{metrics.hybrid.p95LatencyMs.toFixed(2)} ms</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      {/* Visual Comparison Charts */}
      <div style={{ display: 'grid', gridTemplateColumns: '1.2fr 1fr', gap: '20px' }}>
        {/* Metric Comparison Bars */}
        <div className="card">
          <div className="card-title">
            <span>Accuracy Metrics Comparison (Precision &bull; Recall &bull; F1)</span>
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '16px', marginTop: '10px' }}>
            {/* Precision Group */}
            <div>
              <div style={{ fontSize: '12px', fontWeight: 600, color: 'var(--text-muted)', marginBottom: '6px' }}>Precision</div>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '11px' }}>
                  <span style={{ width: '80px', color: 'var(--text-faint)' }}>Rule-Based:</span>
                  <div style={{ flex: 1, height: '10px', background: '#1e293b', borderRadius: '3px', overflow: 'hidden' }}>
                    <div style={{ width: `${metrics.ruleBased.precision * 100}%`, height: '100%', background: '#38bdf8' }} />
                  </div>
                  <span style={{ width: '45px', textAlign: 'right', fontWeight: 600 }}>{(metrics.ruleBased.precision * 100).toFixed(1)}%</span>
                </div>

                <div style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '11px' }}>
                  <span style={{ width: '80px', color: 'var(--text-faint)' }}>ML-Based:</span>
                  <div style={{ flex: 1, height: '10px', background: '#1e293b', borderRadius: '3px', overflow: 'hidden' }}>
                    <div style={{ width: `${metrics.mlBased.precision * 100}%`, height: '100%', background: '#a855f7' }} />
                  </div>
                  <span style={{ width: '45px', textAlign: 'right', fontWeight: 600 }}>{(metrics.mlBased.precision * 100).toFixed(1)}%</span>
                </div>

                <div style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '11px' }}>
                  <span style={{ width: '80px', color: 'var(--primary)', fontWeight: 600 }}>Hybrid:</span>
                  <div style={{ flex: 1, height: '10px', background: '#1e293b', borderRadius: '3px', overflow: 'hidden' }}>
                    <div style={{ width: `${metrics.hybrid.precision * 100}%`, height: '100%', background: 'var(--allow-green)' }} />
                  </div>
                  <span style={{ width: '45px', textAlign: 'right', fontWeight: 700, color: 'var(--allow-green)' }}>{(metrics.hybrid.precision * 100).toFixed(1)}%</span>
                </div>
              </div>
            </div>

            {/* False Positive Rate Group */}
            <div>
              <div style={{ fontSize: '12px', fontWeight: 600, color: 'var(--text-muted)', marginBottom: '6px' }}>
                False Positive Rate (Lower is Better)
              </div>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '11px' }}>
                  <span style={{ width: '80px', color: 'var(--text-faint)' }}>Rule-Based:</span>
                  <div style={{ flex: 1, height: '10px', background: '#1e293b', borderRadius: '3px', overflow: 'hidden' }}>
                    <div style={{ width: `${metrics.ruleBased.falsePositiveRate * 100}%`, height: '100%', background: '#38bdf8' }} />
                  </div>
                  <span style={{ width: '45px', textAlign: 'right', fontWeight: 600 }}>{(metrics.ruleBased.falsePositiveRate * 100).toFixed(1)}%</span>
                </div>

                <div style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '11px' }}>
                  <span style={{ width: '80px', color: 'var(--text-faint)' }}>ML-Based:</span>
                  <div style={{ flex: 1, height: '10px', background: '#1e293b', borderRadius: '3px', overflow: 'hidden' }}>
                    <div style={{ width: `${metrics.mlBased.falsePositiveRate * 100}%`, height: '100%', background: 'var(--flag-amber)' }} />
                  </div>
                  <span style={{ width: '45px', textAlign: 'right', fontWeight: 600, color: 'var(--flag-amber)' }}>{(metrics.mlBased.falsePositiveRate * 100).toFixed(1)}%</span>
                </div>

                <div style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '11px' }}>
                  <span style={{ width: '80px', color: 'var(--primary)', fontWeight: 600 }}>Hybrid:</span>
                  <div style={{ flex: 1, height: '10px', background: '#1e293b', borderRadius: '3px', overflow: 'hidden' }}>
                    <div style={{ width: `${metrics.hybrid.falsePositiveRate * 100}%`, height: '100%', background: 'var(--allow-green)' }} />
                  </div>
                  <span style={{ width: '45px', textAlign: 'right', fontWeight: 700, color: 'var(--allow-green)' }}>{(metrics.hybrid.falsePositiveRate * 100).toFixed(1)}%</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Latency Comparison Card */}
        <div className="card">
          <div className="card-title">
            <span>Average Processing Latency (ms)</span>
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '16px', marginTop: '16px' }}>
            <div style={{ padding: '12px', background: '#0b1326', borderRadius: '6px' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '12px', marginBottom: '4px' }}>
                <span style={{ color: 'var(--text-muted)' }}>Rule-Based Engine</span>
                <span className="mono-cell" style={{ fontWeight: 700 }}>{metrics.ruleBased.avgLatencyMs.toFixed(2)} ms</span>
              </div>
              <div style={{ fontSize: '11px', color: 'var(--text-faint)' }}>Pure regex matching in JVM memory</div>
            </div>

            <div style={{ padding: '12px', background: '#0b1326', borderRadius: '6px' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '12px', marginBottom: '4px' }}>
                <span style={{ color: 'var(--text-muted)' }}>ML-Based Inference</span>
                <span className="mono-cell" style={{ fontWeight: 700 }}>{metrics.mlBased.avgLatencyMs.toFixed(2)} ms</span>
              </div>
              <div style={{ fontSize: '11px', color: 'var(--text-faint)' }}>FastAPI feature scaling + Isolation Forest tree traversal</div>
            </div>

            <div style={{ padding: '12px', background: 'rgba(56, 189, 248, 0.08)', border: '1px solid rgba(56, 189, 248, 0.25)', borderRadius: '6px' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '12px', marginBottom: '4px' }}>
                <span style={{ color: 'var(--primary)', fontWeight: 600 }}>Hybrid Framework (Total Pipeline)</span>
                <span className="mono-cell" style={{ fontWeight: 800 }}>{metrics.hybrid.avgLatencyMs.toFixed(2)} ms</span>
              </div>
              <div style={{ fontSize: '11px', color: 'var(--text-faint)' }}>Feature extraction + Rules + ML + Risk Decision Engine</div>
            </div>
          </div>
        </div>
      </div>

      {/* Research Insights */}
      <div className="card">
        <div className="card-title">
          <span>Research Findings & Academic Insights</span>
        </div>
        <div style={{ display: 'flex', flexDirection: 'column', gap: '10px', fontSize: '13px', color: 'var(--text-muted)', lineHeight: '1.6' }}>
          {evaluation.notes?.map((note, idx) => (
            <div key={idx} style={{ display: 'flex', alignItems: 'flex-start', gap: '8px' }}>
              <span style={{ color: 'var(--primary)', fontWeight: 700 }}>&bull;</span>
              <span>{note}</span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};
