-- This extension is needed to use gen_random_uuid()
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE IF NOT EXISTS users (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username    VARCHAR(30) NOT NULL UNIQUE,
    email       VARCHAR(254) NOT NULL UNIQUE,
    password    VARCHAR(60) NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT timezone('utc', now()),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT timezone('utc', now())
);