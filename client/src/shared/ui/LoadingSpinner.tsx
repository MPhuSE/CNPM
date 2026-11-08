import React from 'react';
import { cn } from '../utils/cn';

export interface LoadingSpinnerProps {
  size?: 'sm' | 'md' | 'lg';
  className?: string;
  label?: string;
}

export const LoadingSpinner: React.FC<LoadingSpinnerProps> = ({
  size = 'md',
  className,
  label = 'Đang tải...',
}) => {
  const sizes = {
    sm: 'w-4 h-4 border-2',
    md: 'w-8 h-8 border-3',
    lg: 'w-12 h-12 border-4',
  };

  return (
    <div className={cn('flex flex-col items-center justify-center p-6 gap-2', className)}>
      <div
        className={cn(
          'rounded-full border-indigo-200 border-t-indigo-600 animate-spin',
          sizes[size]
        )}
      />
      {label && <span className="text-xs text-gray-500">{label}</span>}
    </div>
  );
};
