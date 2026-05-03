CREATE TABLE table_doctor (
      id BIGINT AUTO_INCREMENT PRIMARY KEY,
      name VARCHAR(255) NOT NULL,
      crm VARCHAR(20) UNIQUE,
      status TINYINT(1) NOT NULL DEFAULT 1,
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