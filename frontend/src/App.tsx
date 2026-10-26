import React, { useState, useEffect } from 'react';
import { Sidebar } from './components/Sidebar';
import type { TabId } from './components/Sidebar';
import { TopBar } from './components/TopBar';
import { OverviewView } from './views/OverviewView';
import { TesterView } from './views/TesterView';
import { LogsView } from './views/LogsView';
import { EvaluationView } from './views/EvaluationView';
import { RulesView } from './views/RulesView';
import { HealthView } from './views/HealthView';
import { RequestDetailModal } from './components/RequestDetailModal';
import { gatewayApi } from './api/gatewayApi';
import type { MetricsSummary, RequestEvent, SystemHealth } from './types';

export const App: React.FC = () => {
  const [activeTab, setActiveTab] = useState<TabId>('overview');
  const [health, setHealth] = useState<SystemHealth | null>(null);
  const [metrics, setMetrics] = useState<MetricsSummary | null>(null);
  const [recentEvents, setRecentEvents] = useState<RequestEvent[]>([]);
  const [selectedEvent, setSelectedEvent] = useState<RequestEvent | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  const refreshAllData = async () => {
    setIsLoading(true);
    try {
      const [h, m, logs] = await Promise.allSettled([
        gatewayApi.getHealth(),
        gatewayApi.getMetricsSummary(),
        gatewayApi.getRequests({ page: 0, size: 20 })
      ]);

      if (h.status === 'fulfilled') setHealth(h.value);
      if (m.status === 'fulfilled') setMetrics(m.value);
      if (logs.status === 'fulfilled') setRecentEvents(logs.value.content || []);
    } catch (e) {
      console.error('Error refreshing gateway data:', e);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    refreshAllData();
    const interval = setInterval(refreshAllData, 15000);
    return () => clearInterval(interval);
  }, []);

  const getPageTitle = (tab: TabId): string => {
    switch (tab) {
      case 'overview':
        return 'Security Operations Overview';
      case 'tester':
        return 'Request Inspector & Attack Simulator';
      case 'logs':
        return 'Real-Time Request Activity Logs';
      case 'evaluation':
        return 'Research Benchmark: Multi-Detector Comparison';
      case 'rules':
        return 'Signature Detection Rules Configuration';
      case 'health':
        return 'Gateway Infrastructure Diagnostics';
    }
  };

  return (
    <div className="app-container">
      <Sidebar activeTab={activeTab} onSelectTab={(t) => setActiveTab(t)} />

      <div className="main-wrapper">
        <TopBar
          title={getPageTitle(activeTab)}
          health={health}
          onRefresh={refreshAllData}
          isLoading={isLoading}
        />

        <main className="content-area">
          {activeTab === 'overview' && (
            <OverviewView
              metrics={metrics}
              recentEvents={recentEvents}
              onSelectEvent={(evt) => setSelectedEvent(evt)}
              onNavigate={(tab) => setActiveTab(tab)}
            />
          )}

          {activeTab === 'tester' && (
            <TesterView
              onEventCreated={refreshAllData}
            />
          )}

          {activeTab === 'logs' && (
            <LogsView
              onSelectEvent={(evt) => setSelectedEvent(evt)}
            />
          )}

          {activeTab === 'evaluation' && (
            <EvaluationView />
          )}

          {activeTab === 'rules' && (
            <RulesView />
          )}

          {activeTab === 'health' && (
            <HealthView
              health={health}
              onRefresh={refreshAllData}
              isLoading={isLoading}
            />
          )}
        </main>
      </div>

      <RequestDetailModal
        event={selectedEvent}
        onClose={() => setSelectedEvent(null)}
      />
    </div>
  );
};

export default App;
