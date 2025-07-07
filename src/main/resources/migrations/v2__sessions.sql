CREATE TABLE IF NOT EXISTS sessions (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    token       VARCHAR(96) NOT NULL UNIQUE,
    user_id     UUID NOT NULL,
    expires_at  TIMESTAMPTZ NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT timezone('utc', now()),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT timezone('utc', now())
);