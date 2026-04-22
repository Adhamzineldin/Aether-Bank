import type { Role } from '@stores/authStore';

/**
 * Mirrors the backend `WorkflowCallerRoles.mayActOnStep` rule:
 * ADMIN and SUPERADMIN can always act; otherwise the caller must hold
 * the role the workflow step requires.
 */
export function canActOnWorkflowStep(stepRole: string | undefined | null, userRoles: Role[] | undefined | null): boolean {
  if (!userRoles?.length) return false;
  if (userRoles.includes('ADMIN') || userRoles.includes('SUPERADMIN')) return true;
  if (!stepRole) return false;
  return userRoles.includes(stepRole);
}

