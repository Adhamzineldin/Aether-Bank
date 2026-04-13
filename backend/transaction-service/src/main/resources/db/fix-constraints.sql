-- ─────────────────────────────────────────────────────────────────────────────
-- Idempotent repair script for stale check constraints.
--
-- The transactions_status_check constraint was originally created before
-- TransactionStatus.SUCCESS was added to the enum.  Hibernate's ddl-auto=update
-- never drops/recreates existing check constraints, so we do it here on every
-- startup.  The DROP IF EXISTS + ADD pattern is fully idempotent.
-- ─────────────────────────────────────────────────────────────────────────────

ALTER TABLE transactions
    DROP CONSTRAINT IF EXISTS transactions_status_check;

ALTER TABLE transactions
    ADD CONSTRAINT transactions_status_check
    CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED', 'SUCCESS'));

