import React from 'react';
import { clsx } from 'clsx';

interface CardProps {
  children: React.ReactNode;
  className?: string;
  hover?: boolean;
  onClick?: () => void;
}

export const Card: React.FC<CardProps> = ({
  children,
  className,
  hover = false,
  onClick,
}) => {
  return (
    <div
      className={clsx(
        'bg-white rounded-xl shadow-sm p-6',
        hover && 'transition-shadow hover:shadow-md cursor-pointer',
        className
      )}
      onClick={onClick}
    >
      {children}
    </div>
  );
};

