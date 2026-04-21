import { Component, type ErrorInfo, type ReactNode } from 'react';
import { AlertTriangle } from 'lucide-react';
import { Button } from './Button';

interface Props { children: ReactNode }
interface State { error: Error | null }

export class ErrorBoundary extends Component<Props, State> {
  state: State = { error: null };
  static getDerivedStateFromError(error: Error): State { return { error }; }
  componentDidCatch(err: Error, info: ErrorInfo) { console.error('UI error:', err, info); }
  reset = () => this.setState({ error: null });

  render() {
    if (!this.state.error) return this.props.children;
    return (
      <div className="grid min-h-[60vh] place-items-center p-8">
        <div className="max-w-md text-center space-y-4">
          <div className="mx-auto grid h-12 w-12 place-items-center rounded-full bg-danger/10 text-danger">
            <AlertTriangle className="h-6 w-6" />
          </div>
          <h1 className="text-xl font-semibold">Something went wrong</h1>
          <p className="text-sm text-muted-fg">{this.state.error.message}</p>
          <Button onClick={this.reset}>Try again</Button>
        </div>
      </div>
    );
  }
}

