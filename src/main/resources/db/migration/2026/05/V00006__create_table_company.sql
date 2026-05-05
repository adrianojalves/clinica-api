CREATE TABLE table_company (
       id BIGINT AUTO_INCREMENT PRIMARY KEY,
       corporate_name VARCHAR(255) NOT NULL,
       trade_name VARCHAR(255) NOT NULL,
       cnpj VARCHAR(20) NOT NULL UNIQUE,
       phone VARCHAR(20) NOT NULL,
       email VARCHAR(255) NOT NULL,
       logo_url VARCHAR(500),
       logradouro VARCHAR(255) NOT NULL,
       bairro VARCHAR(100) NOT NULL,
       cep VARCHAR(9) NOT NULL,
       numero VARCHAR(20),
       complemento VARCHAR(255),
       cidade VARCHAR(100) NOT NULL,
       uf CHAR(2) NOT NULL,
       data_criacao DATETIME DEFAULT CURRENT_TIMESTAMP,
       data_atualizacao DATETIME ON UPDATE CURRENT_TIMESTAMP
);