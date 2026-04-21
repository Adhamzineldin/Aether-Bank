-- Initialize all required databases for Aether Bank (PostgreSQL).
-- PostgreSQL has no `CREATE DATABASE IF NOT EXISTS`, so we use a DO block
-- with dynamic SQL guarded by a SELECT against pg_database.

DO $$
DECLARE
    db TEXT;
BEGIN
    FOREACH db IN ARRAY ARRAY['iam_db', 'account_db', 'transaction_db', 'card_db']
    LOOP
        IF NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = db) THEN
            EXECUTE format('CREATE DATABASE %I OWNER postgres', db);
            RAISE NOTICE 'Created database %', db;
        ELSE
            RAISE NOTICE 'Database % already exists', db;
        END IF;
    END LOOP;
END $$;

-- Grant privileges (idempotent)
GRANT ALL PRIVILEGES ON DATABASE iam_db         TO postgres;
GRANT ALL PRIVILEGES ON DATABASE account_db     TO postgres;
GRANT ALL PRIVILEGES ON DATABASE transaction_db TO postgres;
GRANT ALL PRIVILEGES ON DATABASE card_db        TO postgres;

DO $$
BEGIN
    RAISE NOTICE 'All databases ready: iam_db, account_db, transaction_db, card_db';
END $$;

