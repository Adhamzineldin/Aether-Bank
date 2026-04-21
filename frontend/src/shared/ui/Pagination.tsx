import { ChevronLeft, ChevronRight } from 'lucide-react';
import { Button } from './Button';

interface Props {
  page: number;
  totalPages: number;
  onChange: (page: number) => void;
}

export function Pagination({ page, totalPages, onChange }: Props) {
  if (totalPages <= 1) return null;
  return (
    <div className="flex items-center justify-between gap-2 pt-3">
      <p className="text-xs text-muted-fg">Page {page + 1} of {totalPages}</p>
      <div className="flex gap-1">
        <Button variant="outline" size="sm" disabled={page === 0} onClick={() => onChange(page - 1)} leftIcon={<ChevronLeft className="h-4 w-4" />}>
          Prev
        </Button>
        <Button variant="outline" size="sm" disabled={page + 1 >= totalPages} onClick={() => onChange(page + 1)} rightIcon={<ChevronRight className="h-4 w-4" />}>
          Next
        </Button>
      </div>
    </div>
  );
}

