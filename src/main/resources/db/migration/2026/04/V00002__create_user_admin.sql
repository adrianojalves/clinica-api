-- ==========================================
-- V2: Initial Data Load for Admin User
-- ==========================================

-- 1. Insert the root Admin User
-- The password is '123456' encrypted with BCrypt algorithm
INSERT INTO table_usuario (nome, login, senha, email, telefone, ativo, data_criacao, data_atualizacao)
VALUES (
           'System Administrator',
           'admin',
           '$2a$10$Y50UaMFOxteibQEYLrwuHeehHYfcoafCopUazP12.rqB41bsolF5.',
           'admin@admin.com.br',
           '00000000000',
           true,
           NOW(),
           NOW()
       );

INSERT INTO table_usuario_perfil (id_usuario, id_perfil)
VALUES (
           1,
           (SELECT id FROM table_perfil WHERE nome = 'ROLE_ADMIN')
       );