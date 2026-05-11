CREATE TABLE table_cliente (
   id SERIAL PRIMARY KEY,
   name VARCHAR(255) NOT NULL,
   social_name VARCHAR(255),

   rg TEXT NOT NULL,
   cpf TEXT NOT NULL,
   phone TEXT,
   email TEXT,
   biological_sex TEXT NOT NULL,
   sexual_orientation TEXT,

   cpf_hash VARCHAR(64) NOT NULL UNIQUE,

   logradouro VARCHAR(255),
   bairro VARCHAR(100),
   cep VARCHAR(10),
   numero VARCHAR(20),
   complemento VARCHAR(255),
   cidade VARCHAR(100),
   uf VARCHAR(2),

   status BOOLEAN DEFAULT TRUE,
   data_criacao TIMESTAMP NOT NULL,
   data_atualizacao TIMESTAMP
);