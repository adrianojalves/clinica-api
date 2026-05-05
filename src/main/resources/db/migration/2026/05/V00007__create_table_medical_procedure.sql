CREATE TABLE table_medical_procedure (
     id BIGINT AUTO_INCREMENT PRIMARY KEY,
     name VARCHAR(255) NOT NULL,
     description TEXT,
     type VARCHAR(50) NOT NULL,
     transfer_value DECIMAL(10,2), -- Valor de repasse (Pode ser nulo)
     price DECIMAL(10,2) NOT NULL, -- Valor total
     active BOOLEAN NOT NULL DEFAULT TRUE,
     data_criacao DATETIME DEFAULT CURRENT_TIMESTAMP,
     data_atualizacao DATETIME ON UPDATE CURRENT_TIMESTAMP
);