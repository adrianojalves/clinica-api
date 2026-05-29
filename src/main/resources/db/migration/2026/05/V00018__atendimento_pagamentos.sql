ALTER TABLE table_atendimento
    DROP COLUMN tipo_pagamento,
    ADD COLUMN valor_desconto  DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    ADD COLUMN valor_acrescimo DECIMAL(10,2) NOT NULL DEFAULT 0.00;

CREATE TABLE table_atendimento_pagamentos (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    atendimento_id BIGINT        NOT NULL,
    tipo_pagamento VARCHAR(20)   NOT NULL,
    valor          DECIMAL(10,2) NOT NULL,
    valor_desconto DECIMAL(10,2) NOT NULL DEFAULT 0.00,

    CONSTRAINT fk_ap_atendimento FOREIGN KEY (atendimento_id) REFERENCES table_atendimento(id)
        ON UPDATE CASCADE ON DELETE CASCADE
);
