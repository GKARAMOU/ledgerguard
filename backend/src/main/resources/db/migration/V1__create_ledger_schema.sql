CREATE TABLE wallets (
    id UUID PRIMARY KEY,
    name VARCHAR(80) NOT NULL,
    account_number VARCHAR(24) NOT NULL UNIQUE,
    currency VARCHAR(3) NOT NULL,
    balance NUMERIC(19, 2) NOT NULL CHECK (balance >= 0),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE transfers (
    id UUID PRIMARY KEY,
    idempotency_key VARCHAR(72) NOT NULL UNIQUE,
    source_wallet_id UUID NOT NULL REFERENCES wallets(id),
    destination_wallet_id UUID NOT NULL REFERENCES wallets(id),
    amount NUMERIC(19, 2) NOT NULL CHECK (amount > 0),
    reference VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT different_wallets CHECK (source_wallet_id <> destination_wallet_id)
);

CREATE INDEX idx_transfers_source_created ON transfers(source_wallet_id, created_at DESC);
CREATE INDEX idx_transfers_destination_created ON transfers(destination_wallet_id, created_at DESC);
