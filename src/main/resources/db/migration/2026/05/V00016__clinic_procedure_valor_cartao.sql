ALTER TABLE table_clinic_doctor_procedure
    ADD COLUMN transfer_value_card decimal(10,2) NOT NULL DEFAULT 0,
    ADD COLUMN price_card decimal(10,2) NOT NULL DEFAULT 0;