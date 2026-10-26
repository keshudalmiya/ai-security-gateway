export type Decision = 'ALLOW' | 'FLAG' | 'BLOCK';
export type RiskLevel = 'LOW' | 'MEDIUM' | 'HIGH';
export type Severity = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';

export interface MatchedRule {
  ruleId: string;
  name: string;
  severity: Severity;
  matched: boolean;
  reason: string;
}

export interface MlResult {
  status: string;
  isAnomaly: boolean;
  anomalyScore: number;
  modelVersion: string;
  latencyMs: number;
}

export interface RequestEvent {
  id?: string;
  requestId: string;
  createdAt: string;
  method: string;
  path: string;
  querySummary?: string;
  requestLength: number;
  urlLength: number;
  parameterCount: number;
  specialCharCount: number;
  headerCount: number;
  bodyLength: number;
  decision: Decision;
  riskLevel: RiskLevel;
  riskScore: number;
  reasons: string[];
  matchedRules: MatchedRule[];
  ml: MlResult;
  detectorMode: string;
  totalLatencyMs: number;
  featureSchemaVersion: string;
}

export interface AnalyzeRequestPayload {
  method: string;
  path: string;
  query: string;
  headers?: Record<string, string>;
  body: string;
}

export interface AnalyzeResponse {
  requestId: string;
  decision: Decision;
  riskLevel: RiskLevel;
  riskScore: number;
  reasons: string[];
  matchedRules: MatchedRule[];
  ml: MlResult;
  latencyMs: number;
  createdAt: string;
}

export interface TimeSeriesPoint {
  timestamp: string;
  total: number;
  allowed: number;
  flagged: number;
  blocked: number;
}

export interface MetricsSummary {
  totalRequests: number;
  allowed: number;
  flagged: number;
  blocked: number;
  mlServiceStatus: string;
  riskDistribution: Record<string, number>;
  timeSeries: TimeSeriesPoint[];
}

export interface SecurityRule {
  id?: string;
  ruleId: string;
  name: string;
  category: string;
  description: string;
  severity: Severity;
  enabled: boolean;
  version: number;
  updatedAt: string;
  updatedBy: string;
}

export interface DetectorMetrics {
  precision: number;
  recall: number;
  f1: number;
  truePositiveCount: number;
  falsePositiveCount: number;
  trueNegativeCount: number;
  falseNegativeCount: number;
  falsePositiveRate: number;
  avgLatencyMs: number;
  p95LatencyMs: number;
}

export interface EvaluationRun {
  runId: string;
  createdAt: string;
  datasetName: string;
  datasetVersion: string;
  splitStrategy: string;
  seed: number;
  testSampleCount: number;
  normalSampleCount: number;
  anomalousSampleCount: number;
  modelVersion: string;
  thresholdConfig: Record<string, any>;
  metrics: {
    ruleBased: DetectorMetrics;
    mlBased: DetectorMetrics;
    hybrid: DetectorMetrics;
  };
  notes: string[];
}

export interface SystemHealth {
  status: string;
  timestamp: string;
  gateway: {
    status: string;
    service: string;
    version: string;
    uptimeSeconds: number;
  };
  mlService: {
    status: string;
    service?: string;
    modelLoaded?: boolean;
    modelVersion?: string;
    uptimeSeconds?: number;
    error?: string;
  };
  database: {
    status: string;
    type: string;
    fallbackActive: boolean;
  };
  config: {
    riskLowMax: number;
    riskMediumMax: number;
    maxRequestBodyBytes: number;
    mlTimeoutMs: number;
    demoUpstreamUrl: string;
  };
}
