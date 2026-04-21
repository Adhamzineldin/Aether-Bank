import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuthStore } from '../../stores/authStore';
import { Button } from '../common/Button';

export const Header: React.FC = () => {
  const { user, isAuthenticated, logout } = useAuthStore();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <header className="bg-white shadow-sm border-b border-gray-200">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-16">
          <Link to="/" className="flex items-center gap-2">
            <span className="text-2xl">🏦</span>
            <span className="text-xl font-bold text-primary-500">Aether Bank</span>
          </Link>

          <nav className="flex items-center gap-6">
            {isAuthenticated ? (
              <>
                <Link to="/dashboard" className="text-gray-700 hover:text-primary-500">
                  Dashboard
                </Link>
                <Link to="/accounts" className="text-gray-700 hover:text-primary-500">
                  Accounts
                </Link>
                <Link to="/transfer" className="text-gray-700 hover:text-primary-500">
                  Transfer
                </Link>
                <div className="flex items-center gap-4 ml-4 pl-4 border-l border-gray-300">
                  <span className="text-gray-700">Hello, {user?.name}</span>
                  <Button onClick={handleLogout} variant="ghost" size="sm">
                    Logout
                  </Button>
                </div>
              </>
            ) : (
              <>
                <Link to="/login">
                  <Button variant="ghost" size="sm">Login</Button>
                </Link>
                <Link to="/register">
                  <Button size="sm">Get Started</Button>
                </Link>
              </>
            )}
          </nav>
        </div>
      </div>
    </header>
  );
};

