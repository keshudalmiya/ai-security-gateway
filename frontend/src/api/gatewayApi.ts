import type {
  AnalyzeRequestPayload,
  AnalyzeResponse,
  EvaluationRun,
  MetricsSummary,
  RequestEvent,
  SecurityRule,
  SystemHealth
} from '../types';

const API_BASE = import.meta.env.VITE_GATEWAY_API_URL || '/api/v1';

async function fetchJson<T>(url: string, options?: RequestInit): Promise<T> {
  const res = await fetch(url, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      Accept: 'application/json',
      ...options?.headers
    }
  });

  if (!res.ok) {
    let errorMsg = `HTTP Error ${res.status}`;
    try {
      const errBody = await res.json();
      if (errBody?.error?.message) {
        errorMsg = errBody.error.message;
      }
    } catch (_) {}
    throw new Error(errorMsg);
  }

  return res.json() as Promise<T>;
}

export const gatewayApi = {
  getHealth: async (): Promise<SystemHealth> => {
    return fetchJson<SystemHealth>(`${API_BASE}/health`);
  },

  analyze: async (payload: AnalyzeRequestPayload, rate: number = 1.0): Promise<AnalyzeResponse> => {
    return fetchJson<AnalyzeResponse>(`${API_BASE}/analyze?rate=${rate}`, {
      method: 'POST',
      body: JSON.stringify(payload)
    });
  },

  getRequests: async (params?: {
    decision?: string;
    riskLevel?: string;
    method?: string;
    query?: string;
    page?: number;
    size?: number;
  }): Promise<{ content: RequestEvent[]; totalElements: number; totalPages: number }> => {
    const query = new URLSearchParams();
    if (params?.decision && params.decision !== 'ALL') query.set('decision', params.decision);
    if (params?.riskLevel && params.riskLevel !== 'ALL') query.set('riskLevel', params.riskLevel);
    if (params?.method && params.method !== 'ALL') query.set('method', params.method);
    if (params?.query) query.set('query', params.query);
    query.set('page', String(params?.page || 0));
    query.set('size', String(params?.size || 20));

    return fetchJson<{ content: RequestEvent[]; totalElements: number; totalPages: number }>(
      `${API_BASE}/requests?${query.toString()}`
    );
  },

  getRequestDetail: async (requestId: string): Promise<RequestEvent> => {
    return fetchJson<RequestEvent>(`${API_BASE}/requests/${encodeURIComponent(requestId)}`);
  },

  getMetricsSummary: async (): Promise<MetricsSummary> => {
    return fetchJson<MetricsSummary>(`${API_BASE}/metrics/summary`);
  },

  getLatestEvaluation: async (): Promise<EvaluationRun | null> => {
    const data = await fetchJson<any>(`${API_BASE}/evaluation/latest`);
    if (data?.status === 'NOT_EVALUATED') return null;
    return data as EvaluationRun;
  },

  getRules: async (): Promise<SecurityRule[]> => {
    return fetchJson<SecurityRule[]>(`${API_BASE}/rules`);
  },

  updateRule: async (ruleId: string, enabled: boolean): Promise<SecurityRule> => {
    return fetchJson<SecurityRule>(`${API_BASE}/rules/${encodeURIComponent(ruleId)}`, {
      method: 'PATCH',
      body: JSON.stringify({ enabled })
    });
  }
};
