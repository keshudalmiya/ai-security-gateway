import React from 'react';
import type { Decision } from '../types';
import { ShieldCheck, AlertTriangle, ShieldAlert } from 'lucide-react';

interface Props {
  decision: Decision;
}

export const DecisionBadge: React.FC<Props> = ({ decision }) => {
  if (decision === 'ALLOW') {
    return (
      <span className="badge badge-allow">
        <ShieldCheck size={12} />
        ALLOW
      </span>
    );
  } else if (decision === 'FLAG') {
    return (
      <span className="badge badge-flag">
        <AlertTriangle size={12} />
        FLAG
      </span>
    );
  } else {
    return (
      <span className="badge badge-block">
        <ShieldAlert size={12} />
        BLOCK
      </span>
    );
  }
};
