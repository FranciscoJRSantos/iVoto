CREATE TABLE mesa_voto(
  numero		 int NOT NULL,
  unidade_organica_nome varchar(512) NOT NULL,
  eleicao_id	 int NOT NULL,
  key AK_KEY_1 (numero,eleicao_id)
);

CREATE TABLE utilizador(
  numero_cc	 int NOT NULL,
  nome		 varchar(512) NOT NULL,
  password_hashed varchar(512) NOT NULL,
  morada		 varchar(512) NOT NULL,
  contacto	 int UNIQUE NOT NULL,
  validade_cc	 date NOT NULL,
  tipo         tinyint NOT NULL,

  PRIMARY KEY(numero_cc)
);

CREATE TABLE lista(
  nome		 varchar(512) NOT NULL,
  votos		 int NOT NULL,
  tipo_utilizador tinyint NOT NULL,
  eleicao_id	 int NOT NULL,
  key AK_KEY_2 (eleicao_id,nome)
);

CREATE TABLE eleicao(
  id	 int AUTO_INCREMENT,
  titulo	 varchar(512) NOT NULL,
  inicio	 datetime NOT NULL,
  fim	 datetime NOT NULL,
  descricao varchar(512),
  tipo      tinyint NOT NULL,

  PRIMARY KEY(id)
);

CREATE TABLE unidade_organica(
  nome varchar(512) NOT NULL,
  pertence varchar(512) DEFAULT NULL,

  PRIMARY KEY(nome)
);

CREATE TABLE unidade_organica_eleicao(
  unidade_organica_nome varchar(512),
  eleicao_id		 int

);

CREATE TABLE unidade_organica_utilizador(
  unidade_organica_nome    varchar(512),
  utilizador_numero_cc	 int
);

CREATE TABLE eleicao_utilizador(
  unidade_organica_nome        varchar(512),
  eleicao_id		             int,
  utilizador_numero_cc		 int,
  mesa_voto_numero		     int NOT NULL,
  data_voto                    DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE mesa_voto_utilizador(
  mesa_voto_numero		    int,
  eleicao_id		            int,
  utilizador_numero_cc		int
);

CREATE TABLE utilizador_lista(
  utilizador_numero_cc int,
  lista_eleicao_id	 int,
  lista_nome		     varchar(512) NOT NULL
);

ALTER TABLE unidade_organica ADD CONSTRAINT unidade_organica_fk1 FOREIGN KEY (pertence) REFERENCES unidade_organica(nome) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE mesa_voto ADD CONSTRAINT mesa_voto_fk1 FOREIGN KEY (unidade_organica_nome) REFERENCES unidade_organica(nome) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE mesa_voto ADD CONSTRAINT mesa_voto_fk2 FOREIGN KEY (eleicao_id) REFERENCES eleicao(id) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE lista ADD CONSTRAINT lista_fk1 FOREIGN KEY (eleicao_id) REFERENCES eleicao(id) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE unidade_organica_eleicao ADD CONSTRAINT unidade_organica_eleicao_fk1 FOREIGN KEY (unidade_organica_nome) REFERENCES unidade_organica(nome) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE unidade_organica_eleicao ADD CONSTRAINT unidade_organica_eleicao_fk2 FOREIGN KEY (eleicao_id) REFERENCES eleicao(id) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE unidade_organica_utilizador ADD CONSTRAINT unidade_organica_utilizador_fk1 FOREIGN KEY (unidade_organica_nome) REFERENCES unidade_organica(nome) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE unidade_organica_utilizador ADD CONSTRAINT unidade_organica_utilizador_fk2 FOREIGN KEY (utilizador_numero_cc) REFERENCES utilizador(numero_cc) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE mesa_voto_utilizador ADD CONSTRAINT mesa_voto_utilizador_fk1 FOREIGN KEY (mesa_voto_numero,eleicao_id) REFERENCES mesa_voto(numero,eleicao_id) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE mesa_voto_utilizador ADD CONSTRAINT mesa_voto_utilizador_fk2 FOREIGN KEY (utilizador_numero_cc) REFERENCES utilizador(numero_cc) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE utilizador_lista ADD CONSTRAINT utilizador_lista_fk1 FOREIGN KEY (utilizador_numero_cc) REFERENCES utilizador(numero_cc) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE utilizador_lista ADD CONSTRAINT utilizador_lista_fk2 FOREIGN KEY (lista_eleicao_id) REFERENCES lista(eleicao_id) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE eleicao_utilizador ADD CONSTRAINT eleicao_utilizador_fk1 FOREIGN KEY (unidade_organica_nome) REFERENCES unidade_organica_eleicao(unidade_organica_nome) ON UPDATE CASCADE ON DELETE SET NULL;

ALTER TABLE eleicao_utilizador ADD CONSTRAINT eleicao_utilizador_fk2 FOREIGN KEY (eleicao_id) REFERENCES eleicao(id) ON UPDATE CASCADE ON DELETE SET NULL;

ALTER TABLE eleicao_utilizador ADD CONSTRAINT eleicao_utilizador_fk3 FOREIGN KEY (utilizador_numero_cc) REFERENCES utilizador(numero_cc) ON UPDATE CASCADE ON DELETE CASCADE;
