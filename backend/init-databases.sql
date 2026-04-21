-- Initialize all required databases for Aether Bank (PostgreSQL).
-- PostgreSQL does NOT allow CREATE DATABASE inside DO/PL-pgSQL blocks,
-- so we generate the statements via a SELECT and execute them with \gexec.

SELECT 'CREATE DATABASE iam_db OWNER postgres'
WHERE NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = 'iam_db')\gexec

SELECT 'CREATE DATABASE account_db OWNER postgres'
WHERE NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = 'account_db')\gexec

SELECT 'CREATE DATABASE transaction_db OWNER postgres'
WHERE NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = 'transaction_db')\gexec

SELECT 'CREATE DATABASE card_db OWNER postgres'
WHERE NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = 'card_db')\gexec

SELECT 'CREATE DATABASE workflow_db OWNER postgres'
WHERE NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = 'workflow_db')\gexec

GRANT ALL PRIVILEGES ON DATABASE iam_db         TO postgres;
GRANT ALL PRIVILEGES ON DATABASE account_db     TO postgres;
GRANT ALL PRIVILEGES ON DATABASE transaction_db TO postgres;
GRANT ALL PRIVILEGES ON DATABASE card_db        TO postgres;
GRANT ALL PRIVILEGES ON DATABASE workflow_db    TO postgres;
