CREATE TABLE permission_groups (
       id                  UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
       group_name          TEXT        NOT NULL UNIQUE,
       description         TEXT        NOT NULL,
       permissions         TEXT[]      NOT NULL,
       required_permissions TEXT[],
       active              BOOLEAN     NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_permission_groups_group_name ON permission_groups (group_name);
CREATE INDEX idx_permission_groups_active     ON permission_groups (active);

INSERT INTO permission_groups (group_name, description, permissions, required_permissions) VALUES
       (
           'RESOURCES',
           'Permissão base obrigatória para acesso a recursos',
           ARRAY['RESOURCES_READ'],
           NULL
       ),
       (
           'ACCOUNTS',
           'Leitura de dados de contas correntes e poupança',
           ARRAY['ACCOUNTS_READ', 'ACCOUNTS_BALANCES_READ', 'ACCOUNTS_TRANSACTIONS_READ', 'ACCOUNTS_OVERDRAFT_LIMITS_READ'],
           ARRAY['RESOURCES_READ']
       ),
       (
           'CREDIT_CARDS',
           'Leitura de dados de cartões de crédito',
           ARRAY[
               'CREDIT_CARDS_ACCOUNTS_READ',
           'CREDIT_CARDS_ACCOUNTS_BILLS_READ',
           'CREDIT_CARDS_ACCOUNTS_BILLS_TRANSACTIONS_READ',
           'CREDIT_CARDS_ACCOUNTS_LIMITS_READ',
           'CREDIT_CARDS_ACCOUNTS_TRANSACTIONS_READ'
               ],
           ARRAY['RESOURCES_READ']
       ),
       (
           'LOANS',
           'Leitura de operações de crédito — empréstimos',
           ARRAY['LOANS_READ', 'LOANS_WARRANTIES_READ', 'LOANS_SCHEDULED_INSTALMENTS_READ', 'LOANS_PAYMENTS_READ'],
           ARRAY['RESOURCES_READ']
       ),
       (
           'FINANCINGS',
           'Leitura de operações de crédito — financiamentos',
           ARRAY['FINANCINGS_READ', 'FINANCINGS_WARRANTIES_READ', 'FINANCINGS_SCHEDULED_INSTALMENTS_READ', 'FINANCINGS_PAYMENTS_READ'],
           ARRAY['RESOURCES_READ']
       ),
       (
           'CUSTOMERS_PERSONAL',
           'Dados cadastrais de pessoa física',
           ARRAY['CUSTOMERS_PERSONAL_IDENTIFICATIONS_READ', 'CUSTOMERS_PERSONAL_ADITTIONALINFO_READ'],
           ARRAY['RESOURCES_READ']
       ),
       (
           'CUSTOMERS_BUSINESS',
           'Dados cadastrais de pessoa jurídica',
           ARRAY['CUSTOMERS_BUSINESS_IDENTIFICATIONS_READ', 'CUSTOMERS_BUSINESS_ADITTIONALINFO_READ'],
           ARRAY['RESOURCES_READ']
       );

CREATE TABLE consent_permission_validations (
        id                   UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
        consent_id           TEXT        NOT NULL UNIQUE,
        requested_permissions TEXT[]     NOT NULL,
        missing_dependencies  TEXT[],
        invalid_permissions   TEXT[],
        resolved_permissions  TEXT[],
        result               TEXT        NOT NULL,
        rejection_reason     TEXT,
        validated_at         TIMESTAMPTZ NOT NULL DEFAULT now(),

        CONSTRAINT fk_cpv_consent
            FOREIGN KEY (consent_id)
                REFERENCES consents (consent_id)
                ON DELETE CASCADE,

        CONSTRAINT chk_cpv_result
            CHECK (result IN ('VALID', 'AUTO_CORRECTED', 'REJECTED'))
);

CREATE INDEX idx_cpv_consent_id  ON consent_permission_validations (consent_id);
CREATE INDEX idx_cpv_result      ON consent_permission_validations (result);
CREATE INDEX idx_cpv_validated_at ON consent_permission_validations (validated_at DESC);