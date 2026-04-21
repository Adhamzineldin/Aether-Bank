import { Link } from 'react-router-dom';
import { Sparkles, ArrowRight, ShieldCheck, Globe2, TrendingUp, CreditCard } from 'lucide-react';
import { Button } from '@shared/ui/Button';
import { ROUTES } from '@app/routes';

const features = [
  { icon: ShieldCheck, title: 'Bank-grade security', text: 'End-to-end encryption with audit-ready trails on every action.' },
  { icon: Globe2, title: 'Global by default', text: 'Hold and convert 8+ currencies with live FX rates.' },
  { icon: TrendingUp, title: 'Investing built-in', text: 'Trade stocks, ETFs and bonds — same app, zero friction.' },
  { icon: CreditCard, title: 'Cards & payments', text: 'Issue virtual cards, pay merchants and bills in seconds.' },
];

export default function LandingPage() {
  return (
    <div className="min-h-full bg-bg">
      <header className="container flex h-16 items-center justify-between">
        <Link to="/" className="inline-flex items-center gap-2 text-lg font-bold">
          <span className="grid h-8 w-8 place-items-center rounded-lg gradient-primary text-white">
            <Sparkles className="h-4 w-4" />
          </span>
          Aether Bank
        </Link>
        <nav className="flex gap-2">
          <Link to={ROUTES.login}><Button variant="ghost">Sign in</Button></Link>
          <Link to={ROUTES.register}><Button>Get started</Button></Link>
        </nav>
      </header>

      <section className="container grid gap-12 py-20 md:grid-cols-2 md:items-center">
        <div className="space-y-6">
          <span className="inline-flex items-center gap-2 rounded-full bg-primary/10 px-3 py-1 text-xs font-medium text-primary">
            ✨ New: Built-in investments
          </span>
          <h1 className="text-5xl font-bold leading-[1.05] tracking-tight md:text-6xl">
            Banking, redefined for the modern world.
          </h1>
          <p className="max-w-lg text-lg text-muted-fg">
            Open accounts in seconds, send money globally, invest in markets, and manage your financial life — all from one elegant app.
          </p>
          <div className="flex gap-3">
            <Link to={ROUTES.register}>
              <Button size="lg" rightIcon={<ArrowRight className="h-4 w-4" />}>Open free account</Button>
            </Link>
            <Link to={ROUTES.login}>
              <Button size="lg" variant="outline">Sign in</Button>
            </Link>
          </div>
        </div>
        <div className="relative">
          <div className="aspect-[4/3] rounded-3xl gradient-primary p-1 shadow-soft">
            <div className="grid h-full place-items-center rounded-[1.4rem] bg-card text-fg">
              <div className="text-center p-8">
                <Sparkles className="mx-auto h-10 w-10 text-primary" />
                <p className="mt-3 text-sm uppercase tracking-widest text-muted-fg">Trusted by 10k+ users</p>
                <p className="mt-1 text-2xl font-bold">$1B+ moved this year</p>
              </div>
            </div>
          </div>
        </div>
      </section>

      <section className="container grid gap-6 pb-24 md:grid-cols-2 lg:grid-cols-4">
        {features.map(({ icon: Icon, title, text }) => (
          <div key={title} className="rounded-2xl border border-border bg-card p-5">
            <span className="grid h-10 w-10 place-items-center rounded-xl bg-primary/10 text-primary">
              <Icon className="h-5 w-5" />
            </span>
            <h3 className="mt-3 font-semibold">{title}</h3>
            <p className="mt-1 text-sm text-muted-fg">{text}</p>
          </div>
        ))}
      </section>

      <footer className="border-t border-border">
        <div className="container flex h-16 items-center justify-between text-xs text-muted-fg">
          <p>© {new Date().getFullYear()} Aether Bank. All rights reserved.</p>
          <p>Made with ❤️ for modern finance.</p>
        </div>
      </footer>
    </div>
  );
}

