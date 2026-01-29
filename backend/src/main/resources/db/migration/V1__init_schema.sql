CREATE TABLE app_users (
    id UUID PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255), -- Nullable for OAuth users
    avatar_url VARCHAR(500),

    -- Social Login Fields
    provider VARCHAR(20) NOT NULL DEFAULT 'LOCAL',
    provider_id VARCHAR(255),

    -- User Status
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    account_non_locked BOOLEAN NOT NULL DEFAULT TRUE,

    -- Audit Fields
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ,
    created_by UUID,
    updated_by UUID,
    is_deleted BOOLEAN DEFAULT FALSE,
    version BIGINT,

    -- Unique Constraints
    CONSTRAINT uk_user_username UNIQUE (username),
    CONSTRAINT uk_user_email UNIQUE (email),
    CONSTRAINT uk_user_provider_identity UNIQUE (provider, provider_id)
);

-- Indexes for app_users
CREATE INDEX idx_users_email ON app_users (email);
CREATE INDEX idx_users_provider ON app_users (provider, provider_id);


CREATE TABLE categories (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,  -- Enum TransactionType save as String
    type VARCHAR(50) NOT NULL,
    icon_code VARCHAR(50),
    color_code VARCHAR(7),

    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ,
    created_by UUID,
    updated_by UUID,
    is_deleted BOOLEAN DEFAULT FALSE,
    version BIGINT,

    -- Foreign Key
    CONSTRAINT fk_category_user
        FOREIGN KEY (user_id)
            REFERENCES app_users (id)
);

-- Partial Index to handle Unique Soft Delete
CREATE UNIQUE INDEX idx_categories_name_user_active
    ON categories (name, user_id)
    WHERE (is_deleted = false);


CREATE TABLE transactions (
    id UUID PRIMARY KEY,
    category_id UUID NOT NULL,
    user_id UUID NOT NULL,
    transaction_date TIMESTAMPTZ NOT NULL,
    amount NUMERIC(19, 4) NOT NULL,
    note TEXT,

    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ,
    created_by UUID,
    updated_by UUID,
    is_deleted BOOLEAN DEFAULT FALSE,
    version BIGINT,

    CONSTRAINT fk_transaction_category
      FOREIGN KEY (category_id)
          REFERENCES categories (id)
);

CREATE INDEX idx_transactions_user_id ON transactions (user_id);
CREATE INDEX idx_transactions_date ON transactions (transaction_date);