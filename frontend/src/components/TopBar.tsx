import React from 'react';
import { RotateCw } from 'lucide-react';
import type { SystemHealth } from '../types';

interface Props {
  title: string;
  health: SystemHealth | null;
  onRefresh: () => void;
  isLoading: boolean;
}

export const TopBar: React.FC<Props> = ({ title, health, onRefresh, isLoading }) => {
  const gwUp = health?.gateway?.status === 'UP';
  const mlUp = health?.mlService?.status === 'OK';
  const dbUp = health?.database?.status === 'UP';

  return (
    <header className="topbar">
      <div className="topbar-left">
        <h1 className="page-title">{title}</h1>
        <span className="env-pill">LOCAL DEMO</span>
      </div>

      <div className="topbar-right">
        <div className="status-group">
          <div className="status-badge" title="Spring Boot Gateway">
            <span className={`status-dot ${gwUp ? 'up' : 'down'}`} />
            <span>Gateway: {gwUp ? '8080' : 'Offline'}</span>
          </div>

          <div className="status-badge" title="FastAPI Isolation Forest Model">
            <span className={`status-dot ${mlUp ? 'up' : 'degraded'}`} />
            <span>ML: {mlUp ? '8000' : 'Degraded'}</span>
          </div>

          <div className="status-badge" title="MongoDB Connection">
            <span className={`status-dot ${dbUp ? 'up' : 'degraded'}`} />
            <span>DB: {dbUp ? 'MongoDB' : 'Memory Ring'}</span>
          </div>
        </div>

        <button
          className="refresh-btn"
          onClick={onRefresh}
          disabled={isLoading}
          title="Refresh All Real-time Data"
        >
          <RotateCw size={15} className={isLoading ? 'spin' : ''} />
        </button>
      </div>
    </header>
  );
};
