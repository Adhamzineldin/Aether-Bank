-- Initialize all required databases for Aether Bank

-- Create databases
CREATE DATABASE IF NOT EXISTS iam_db;
CREATE DATABASE IF NOT EXISTS account_db;
CREATE DATABASE IF NOT EXISTS transaction_db;
CREATE DATABASE IF NOT EXISTS card_db;

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE iam_db TO postgres;
GRANT ALL PRIVILEGES ON DATABASE account_db TO postgres;
GRANT ALL PRIVILEGES ON DATABASE transaction_db TO postgres;
GRANT ALL PRIVILEGES ON DATABASE card_db TO postgres;

-- Log completion
DO $$
BEGIN
  RAISE NOTICE 'All databases created successfully!';
  RAISE NOTICE 'Databases: iam_db, account_db, transaction_db, card_db';
END $$;

