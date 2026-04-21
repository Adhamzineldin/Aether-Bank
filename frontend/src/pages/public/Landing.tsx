import React from 'react';
import { Link } from 'react-router-dom';
import { Button } from '../../components/common/Button';

const Landing: React.FC = () => {
  return (
    <div className="min-h-screen bg-gradient-to-br from-primary-500 to-primary-700">
      {/* Header */}
      <header className="absolute top-0 left-0 right-0 z-10">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
          <div className="flex justify-between items-center">
            <div className="flex items-center gap-2">
              <span className="text-3xl">🏦</span>
              <span className="text-2xl font-bold text-white">Aether Bank</span>
            </div>
            <div className="flex items-center gap-4">
              <Link to="/login">
                <Button variant="ghost" className="text-white hover:bg-white/10">
                  Sign In
                </Button>
              </Link>
              <Link to="/register">
                <Button className="bg-white text-primary-600 hover:bg-gray-100">
                  Get Started
                </Button>
              </Link>
            </div>
          </div>
        </div>
      </header>

      {/* Hero Section */}
      <div className="min-h-screen flex items-center justify-center px-4">
        <div className="text-center text-white max-w-4xl">
          <h1 className="text-6xl font-bold mb-6">
            Banking Made Simple
          </h1>
          <p className="text-2xl mb-8 text-primary-100">
            Experience modern banking with instant transfers, smart investments, and 24/7 support
          </p>
          <div className="flex gap-4 justify-center">
            <Link to="/register">
              <Button size="lg" className="bg-white text-primary-600 hover:bg-gray-100">
                Open Account
              </Button>
            </Link>
            <Link to="/login">
              <Button size="lg" variant="ghost" className="text-white border-2 border-white hover:bg-white/10">
                Sign In
              </Button>
            </Link>
          </div>
        </div>
      </div>

      {/* Features */}
      <div className="bg-white py-20">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <h2 className="text-4xl font-bold text-center mb-12">Why Choose Aether Bank?</h2>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            <div className="text-center">
              <div className="text-5xl mb-4">⚡</div>
              <h3 className="text-xl font-semibold mb-2">Instant Transfers</h3>
              <p className="text-gray-600">Send money instantly to anyone, anywhere in the world</p>
            </div>
            <div className="text-center">
              <div className="text-5xl mb-4">🔒</div>
              <h3 className="text-xl font-semibold mb-2">Secure & Safe</h3>
              <p className="text-gray-600">Bank-level security with 256-bit encryption</p>
            </div>
            <div className="text-center">
              <div className="text-5xl mb-4">📱</div>
              <h3 className="text-xl font-semibold mb-2">Mobile Ready</h3>
              <p className="text-gray-600">Access your accounts anytime, anywhere</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Landing;

