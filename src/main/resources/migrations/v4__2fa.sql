CREATE TABLE IF NOT EXISTS pre_auth_token (
    id          SERIAL PRIMARY KEY,
    token       VARCHAR(96) NOT NULL UNIQUE,
    user_id     UUID NOT NULL,
    pin         VARCHAR(6) NOT NULL,
    used        BOOLEAN NOT NULL DEFAULT FALSE,
    expires_at  TIMESTAMPTZ NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT timezone('utc', now()),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT timezone('utc', now())
);