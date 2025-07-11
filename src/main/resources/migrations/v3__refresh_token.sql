-- Tamanho do refresh token https://learn.microsoft.com/en-us/answers/questions/501381/what-is-length-of-the-refresh-token
CREATE TABLE refresh_token (
    id          SERIAL PRIMARY KEY,
    token       VARCHAR(1000) UNIQUE NOT NULL,
    user_id     UUID NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT timezone('utc', now()),
    expires_at  TIMESTAMP NOT NULL
);
