ALTER TABLE table_clinic_doctor_procedure
DROP FOREIGN KEY fk_cdp_clinic,
    DROP FOREIGN KEY fk_cdp_procedure;


ALTER TABLE table_clinic_doctor_procedure
DROP INDEX uk_clinic_doctor_procedure;

ALTER TABLE table_clinic_doctor_procedure
    ADD _doctor_id_distinct bigint(20) GENERATED ALWAYS AS (IFNULL(doctor_id, -1)) VIRTUAL;

ALTER TABLE table_clinic_doctor_procedure
    ADD CONSTRAINT uk_clinic_doctor_procedure
        UNIQUE (clinic_id, _doctor_id_distinct, medical_procedure_id);

ALTER TABLE table_clinic_doctor_procedure
    ADD CONSTRAINT fk_cdp_clinic FOREIGN KEY (clinic_id) REFERENCES table_clinica (id) ON UPDATE CASCADE,
    ADD CONSTRAINT fk_cdp_procedure FOREIGN KEY (medical_procedure_id) REFERENCES table_medical_procedure
        (id) ON UPDATE CASCADE;