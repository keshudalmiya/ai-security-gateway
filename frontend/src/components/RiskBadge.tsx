import React from 'react';
import type { RiskLevel } from '../types';

interface Props {
  riskLevel: RiskLevel;
  score?: number;
}

export const RiskBadge: React.FC<Props> = ({ riskLevel, score }) => {
  let badgeClass = 'badge-allow';
  if (riskLevel === 'MEDIUM') badgeClass = 'badge-flag';
  else if (riskLevel === 'HIGH') badgeClass = 'badge-block';

  return (
    <span className={`badge ${badgeClass}`}>
      {riskLevel} {score !== undefined ? `(${score})` : ''}
    </span>
  );
};
