CREATE TABLE table_clinic_doctor_procedure (
       id BIGINT AUTO_INCREMENT PRIMARY KEY,
       clinic_id BIGINT NOT NULL,
       doctor_id BIGINT NOT NULL,
       medical_procedure_id BIGINT NOT NULL,
       transfer_value DECIMAL(10,2),
       price DECIMAL(10,2) NOT NULL,
       data_criacao DATETIME DEFAULT CURRENT_TIMESTAMP,
       data_atualizacao DATETIME ON UPDATE CURRENT_TIMESTAMP,

       CONSTRAINT fk_cdp_clinic FOREIGN KEY (clinic_id) REFERENCES table_clinica(id)
                ON UPDATE CASCADE ON DELETE RESTRICT,
       CONSTRAINT fk_cdp_doctor FOREIGN KEY (doctor_id) REFERENCES table_doctor(id)
                ON UPDATE CASCADE ON DELETE RESTRICT,
       CONSTRAINT fk_cdp_procedure FOREIGN KEY (medical_procedure_id) REFERENCES table_medical_procedure(id)
                ON UPDATE CASCADE ON DELETE RESTRICT,
       CONSTRAINT uk_clinic_doctor_procedure UNIQUE (clinic_id, doctor_id, medical_procedure_id)
);