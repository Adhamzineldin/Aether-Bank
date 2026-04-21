import { Card, CardContent } from '@shared/ui/Card';
import { PageHeader } from '@shared/ui/PageHeader';
import { useTheme } from '@shared/hooks/useTheme';
import { Button } from '@shared/ui/Button';
import { ROUTES } from '@app/routes';
import { Moon, Sun } from 'lucide-react';

export default function PreferencesPage() {
  const { theme, setTheme } = useTheme();
  return (
    <div className="space-y-6">
      <PageHeader title="Preferences" description="Personalize how Aether Bank looks and feels." back={{ to: ROUTES.profile }} />
      <Card className="max-w-xl">
        <CardContent>
          <h3 className="text-sm font-semibold mb-3">Appearance</h3>
          <div className="flex gap-2">
            <Button variant={theme === 'light' ? 'primary' : 'outline'} leftIcon={<Sun className="h-4 w-4" />} onClick={() => setTheme('light')}>
              Light
            </Button>
            <Button variant={theme === 'dark' ? 'primary' : 'outline'} leftIcon={<Moon className="h-4 w-4" />} onClick={() => setTheme('dark')}>
              Dark
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}

