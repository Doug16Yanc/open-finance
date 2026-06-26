CREATE TABLE consents (
      id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
      consent_id          VARCHAR(255)    NOT NULL UNIQUE,
      client_id           VARCHAR(255)    NOT NULL,
      cpf                 VARCHAR(11)     NOT NULL,
      status              VARCHAR(50)     NOT NULL,
      permissions         TEXT[]          NOT NULL,
      expiration_date     TIMESTAMPTZ,
      transaction_from    TIMESTAMPTZ,
      transaction_to      TIMESTAMPTZ,
      rejection_reason    VARCHAR(255),
      created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
      updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
      version             BIGINT          NOT NULL DEFAULT 0
);

CREATE INDEX idx_consents_client_cpf    ON consents (client_id, cpf);
CREATE INDEX idx_consents_status        ON consents (status);
CREATE INDEX idx_consents_consent_id    ON consents (consent_id);

CREATE TABLE consent_status_history (
        id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
        consent_id      UUID        NOT NULL REFERENCES consents(id),
        from_status     VARCHAR(50),
        to_status       VARCHAR(50) NOT NULL,
        changed_by      VARCHAR(255),
        reason          TEXT,
        changed_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_consent_history_consent ON consent_status_history (consent_id);


CREATE TABLE outbox_events (
       id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
       aggregate_type  VARCHAR(100) NOT NULL,
       aggregate_id    VARCHAR(255) NOT NULL,
       event_type      VARCHAR(100) NOT NULL,
       payload         JSONB       NOT NULL,
       published       BOOLEAN     NOT NULL DEFAULT FALSE,
       created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_outbox_unpublished ON outbox_events (published) WHERE published = FALSE;