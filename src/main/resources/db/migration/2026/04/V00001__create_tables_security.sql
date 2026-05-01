CREATE TABLE table_perfil (
                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                              nome VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE table_usuario (
                               id BIGINT AUTO_INCREMENT PRIMARY KEY,
                               nome VARCHAR(100) NOT NULL,
                               login VARCHAR(50) NOT NULL UNIQUE,
                               senha VARCHAR(255) NOT NULL,
                               email VARCHAR(100),
                               telefone VARCHAR(20),
                               ativo BOOLEAN DEFAULT TRUE,
                               data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               data_atualizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE table_usuario_perfil (
                                      id_usuario BIGINT NOT NULL,
                                      id_perfil BIGINT NOT NULL,
                                      PRIMARY KEY (id_usuario, id_perfil),
                                      CONSTRAINT fk_usuario FOREIGN KEY (id_usuario) REFERENCES table_usuario(id) ON DELETE CASCADE,
                                      CONSTRAINT fk_perfil FOREIGN KEY (id_perfil) REFERENCES table_perfil(id) ON DELETE CASCADE
);

INSERT INTO table_perfil (nome) VALUES ('ROLE_ADMIN');
INSERT INTO table_perfil (nome) VALUES ('ROLE_CADASTROS');
INSERT INTO table_perfil (nome) VALUES ('ROLE_ATENDIMENTO');
INSERT INTO table_perfil (nome) VALUES ('ROLE_RELATORIOS');