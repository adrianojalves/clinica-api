ALTER TABLE table_atendimento
    MODIFY COLUMN data_consulta_exame DATE NULL,
    ADD COLUMN turno      VARCHAR(20) NULL AFTER data_consulta_exame,
    ADD COLUMN observacao TEXT        NULL AFTER turno;
