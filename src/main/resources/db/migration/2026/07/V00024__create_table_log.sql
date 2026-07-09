CREATE TABLE table_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    data_hora DATETIME NOT NULL,
    log TEXT NOT NULL,
    usuario_id BIGINT NOT NULL,
    CONSTRAINT fk_log_usuario FOREIGN KEY (usuario_id) REFERENCES table_usuario(id)
);
