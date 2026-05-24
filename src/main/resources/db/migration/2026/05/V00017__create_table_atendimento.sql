CREATE TABLE table_atendimento (
    id                       BIGINT AUTO_INCREMENT PRIMARY KEY,
    data_emissao             DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    usuario_id               BIGINT         NOT NULL,
    data_consulta_exame      DATE           NOT NULL,
    cliente_id               BIGINT UNSIGNED NOT NULL,
    clinica_id               BIGINT         NOT NULL,
    tipo_pagamento           VARCHAR(20)    NOT NULL,
    parcelas                 INT            NOT NULL DEFAULT 1,
    status                   VARCHAR(20)    NOT NULL DEFAULT 'ABERTO',
    total_transfer_value     DECIMAL(10,2)  NOT NULL DEFAULT 0.00,
    total_price              DECIMAL(10,2)  NOT NULL DEFAULT 0.00,
    total_transfer_value_card DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    total_price_card         DECIMAL(10,2)  NOT NULL DEFAULT 0.00,
    data_criacao             DATETIME       DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao         DATETIME       ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_atendimento_usuario  FOREIGN KEY (usuario_id)  REFERENCES table_usuario(id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_atendimento_cliente  FOREIGN KEY (cliente_id)  REFERENCES table_cliente(id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_atendimento_clinica  FOREIGN KEY (clinica_id)  REFERENCES table_clinica(id)
        ON UPDATE CASCADE ON DELETE RESTRICT
);

CREATE TABLE table_atendimento_consulta_exame (
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    atendimento_id       BIGINT        NOT NULL,
    doctor_id            BIGINT        NULL,
    medical_procedure_id BIGINT        NOT NULL,
    transfer_value       DECIMAL(10,2) NULL,
    price                DECIMAL(10,2) NOT NULL,
    transfer_value_card  DECIMAL(10,2) NULL,
    price_card           DECIMAL(10,2) NOT NULL,

    CONSTRAINT fk_ace_atendimento FOREIGN KEY (atendimento_id) REFERENCES table_atendimento(id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_ace_doctor      FOREIGN KEY (doctor_id)      REFERENCES table_doctor(id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_ace_procedure   FOREIGN KEY (medical_procedure_id) REFERENCES table_medical_procedure(id)
        ON UPDATE CASCADE ON DELETE RESTRICT
);
