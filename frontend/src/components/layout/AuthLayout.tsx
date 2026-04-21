import { Outlet, Link } from 'react-router-dom';
import { Sparkles } from 'lucide-react';

export function AuthLayout() {
  return (
    <div className="grid min-h-full lg:grid-cols-2">
      {/* Left: form */}
      <div className="flex flex-col justify-between p-8 lg:p-12">
        <Link to="/" className="inline-flex items-center gap-2 text-lg font-bold">
          <span className="grid h-8 w-8 place-items-center rounded-lg gradient-primary text-white">
            <Sparkles className="h-4 w-4" />
          </span>
          Aether Bank
        </Link>
        <div className="mx-auto w-full max-w-md py-12">
          <Outlet />
        </div>
        <p className="text-xs text-muted-fg">© {new Date().getFullYear()} Aether Bank. All rights reserved.</p>
      </div>
      {/* Right: hero */}
      <div className="relative hidden overflow-hidden lg:block">
        <div className="absolute inset-0 gradient-primary" />
        <div className="absolute inset-0 bg-[radial-gradient(circle_at_30%_20%,rgba(255,255,255,0.25),transparent_40%)]" />
        <div className="relative z-10 flex h-full flex-col justify-end p-12 text-white">
          <h2 className="max-w-md text-4xl font-bold leading-tight">Banking, redefined for the modern world.</h2>
          <p className="mt-4 max-w-md text-white/80">
            Open accounts in seconds, send money globally, invest in the markets, and manage your financial life — all in one
            elegant experience.
          </p>
        </div>
      </div>
    </div>
  );
}

