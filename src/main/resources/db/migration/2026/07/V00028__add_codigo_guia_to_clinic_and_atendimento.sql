ALTER TABLE table_clinica
    ADD COLUMN codigo_guia BIGINT NULL;

ALTER TABLE table_atendimento
    ADD COLUMN codigo_guia BIGINT NULL;

ALTER TABLE table_atendimento
    ADD CONSTRAINT uk_atendimento_clinica_guia UNIQUE (clinica_id, codigo_guia);
