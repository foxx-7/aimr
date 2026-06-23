-- ============================================================
-- V2__add_advised_indexes.sql
-- Aimr Notify — Indexing & column name fixes
-- ============================================================

-- STEP 1: Fix refresh_token hyphenated names (rename if live data exists)
-- Skip these ALTER statements if the table was just created with correct names.

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_name = 'refresh-token'
    ) THEN
        ALTER TABLE "refresh-token" RENAME TO refresh_token;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'refresh_token' AND column_name = 'user-id'
    ) THEN
        ALTER TABLE refresh_token RENAME COLUMN "user-id"            TO user_id;
        ALTER TABLE refresh_token RENAME COLUMN "token-hash"         TO token_hash;
        ALTER TABLE refresh_token RENAME COLUMN "replacement-token"  TO replaced_by;
        ALTER TABLE refresh_token RENAME COLUMN "is-revoked"         TO is_revoked;
    END IF;
END $$;

-- STEP 2: refresh_token indexes
CREATE INDEX IF NOT EXISTS idx_refresh_token_user_id
    ON refresh_token(user_id);

CREATE UNIQUE INDEX IF NOT EXISTS idx_refresh_token_hash
    ON refresh_token(token_hash);

-- STEP 3: tenant_membership — standalone tenant_id index
-- (composite unique on user_id,tenant_id already exists but won't cover tenant-only queries)
CREATE INDEX IF NOT EXISTS idx_membership_tenant_id
    ON tenant_membership(tenant_id);

-- STEP 4: tenants — composite (id, owner_id) for findTenantByIdAndOwnerId
CREATE INDEX IF NOT EXISTS idx_tenant_id_owner
    ON tenants(id, owner_id);

-- STEP 5: api_keys — tenant_id for listing/bulk revocation
CREATE INDEX IF NOT EXISTS idx_api_key_tenant_id
    ON api_keys(tenant_id);

-- STEP 6: invitations — standalone tenant_id for listing per-tenant invites
CREATE INDEX IF NOT EXISTS idx_invitation_tenant_id
    ON invitations(tenant_id);
