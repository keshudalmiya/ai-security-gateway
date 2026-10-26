import React from 'react';
import {
  Shield,
  LayoutDashboard,
  Terminal,
  FileText,
  BarChart2,
  Sliders,
  Activity
} from 'lucide-react';

export type TabId = 'overview' | 'tester' | 'logs' | 'evaluation' | 'rules' | 'health';

interface Props {
  activeTab: TabId;
  onSelectTab: (tab: TabId) => void;
}

export const Sidebar: React.FC<Props> = ({ activeTab, onSelectTab }) => {
  const navItems: { id: TabId; label: string; icon: React.ReactNode }[] = [
    { id: 'overview', label: 'Overview', icon: <LayoutDashboard size={18} /> },
    { id: 'tester', label: 'Request Tester', icon: <Terminal size={18} /> },
    { id: 'logs', label: 'Request Logs', icon: <FileText size={18} /> },
    { id: 'evaluation', label: 'Evaluation & ML', icon: <BarChart2 size={18} /> },
    { id: 'rules', label: 'Rules Engine', icon: <Sliders size={18} /> },
    { id: 'health', label: 'System Health', icon: <Activity size={18} /> }
  ];

  return (
    <aside className="sidebar">
      <div className="sidebar-header">
        <div className="brand-icon">
          <Shield size={20} />
        </div>
        <div>
          <div className="brand-title">AI Security Gateway</div>
          <span className="brand-badge">HYBRID WAF</span>
        </div>
      </div>

      <ul className="nav-list">
        {navItems.map((item) => (
          <li
            key={item.id}
            className={`nav-item ${activeTab === item.id ? 'active' : ''}`}
            onClick={() => onSelectTab(item.id)}
          >
            {item.icon}
            <span>{item.label}</span>
          </li>
        ))}
      </ul>

      <div className="sidebar-footer">
        <div>Adaptive Hybrid Framework</div>
        <div>Version 1.0.0 (CSIC 2010 Baseline)</div>
      </div>
    </aside>
  );
};
