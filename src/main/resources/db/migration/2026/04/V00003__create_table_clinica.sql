CREATE TABLE table_clinica (
                               id BIGINT AUTO_INCREMENT PRIMARY KEY,
                               nome VARCHAR(255) NOT NULL,
                               cnpj VARCHAR(20) NOT NULL UNIQUE,
                               fone1 VARCHAR(20) NOT NULL,
                               fone2 VARCHAR(20),
                               site VARCHAR(255),
                               email VARCHAR(255) NOT NULL,
                               ativo BOOLEAN NOT NULL,

                               logradouro VARCHAR(255) NOT NULL,
                               bairro VARCHAR(100) NOT NULL,
                               cep VARCHAR(9) NOT NULL,
                               numero VARCHAR(20) NOT NULL,
                               complemento VARCHAR(100),
                               cidade VARCHAR(100) NOT NULL,
                               uf CHAR(2) NOT NULL,

                               data_criacao DATETIME NOT NULL,
                               data_atualizacao DATETIME
);