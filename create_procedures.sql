-- Create user procedure

DELIMITER //
DROP PROCEDURE IF EXISTS createUtilizador;
CREATE PROCEDURE createUtilizador (IN p_numero_cc int, IN p_nome varchar(512), IN p_password_hashed varchar(512), IN p_morada	varchar(512), IN p_contacto int, IN p_validade_cc date, IN p_tipo int, IN p_unidade_organica_nome varchar(512)) 
BEGIN 
  INSERT INTO utilizador (numero_cc,nome,password_hashed,morada,contacto,validade_cc,tipo) VALUES(p_numero_cc,p_nome,p_password_hashed,p_morada,p_contacto,p_validade_cc,p_tipo);
  INSERT INTO unidade_organica_utilizador (unidade_organica_nome,utilizador_numero_cc) VALUES(p_unidade_organica_nome,p_numero_cc);
END//
DELIMITER ;

-- Create election procedure

DELIMITER //
DROP PROCEDURE IF EXISTS createEleicao;
CREATE PROCEDURE createEleicao(IN p_titulo varchar(12) , IN p_inicio DATE, IN p_fim DATE, IN p_descricao varchar(512) , IN p_tipo int,IN p_unidade_organica_nome varchar(512)) 
BEGIN 
  DECLARE id_eleicao INT; 
  INSERT INTO eleicao (titulo,inicio,fim,descricao,tipo) VALUES (p_titulo,p_inicio,p_fim,p_descricao,p_tipo);

  SELECT MAX(id) INTO id_eleicao FROM eleicao;

  CASE
    WHEN p_tipo = 0 THEN INSERT INTO unidade_organica_eleicao (unidade_organica_nome,eleicao_id) VALUES(p_unidade_organica_nome, id_eleicao);
    WHEN p_tipo = 1 THEN INSERT INTO unidade_organica_eleicao (unidade_organica_nome,eleicao_id) SELECT u.nome, e.id FROM unidade_organica u, eleicao e WHERE u.pertence IS NULL;
    WHEN p_tipo = 3 THEN INSERT INTO unidade_organica_eleicao (unidade_organica_nome,eleicao_id) VALUES(p_unidade_organica_nome, id_eleicao);
    WHEN p_tipo = 4 THEN INSERT INTO unidade_organica_eleicao (unidade_organica_nome,eleicao_id) SELECT id_eleicao, unidade_organica_nome FROM unidade_organica WHERE pertence LIKE p_unidade_organica_nome;
    ELSE
      BEGIN
      END;
  END CASE;
  
END//
DELIMITER ;

-- Create election list procedure

DELIMITER //
DROP PROCEDURE IF EXISTS createLista;
CREATE PROCEDURE createLista(IN p_nome varchar(512),IN p_tipo_utilizador tinyint, IN p_eleicao_id INT, IN p_numero_cc int) 
BEGIN 

  INSERT INTO lista (nome, votos, tipo_utilizador, eleicao_id) VALUES (p_nome,0,p_tipo_utilizador, p_eleicao_id);
  INSERT INTO utilizador_lista (utilizador_numero_cc, lista_eleicao_id, lista_nome) VALUES (p_numero_cc, p_eleicao_id, p_nome);
END//
DELIMITER ;

-- Create voting table procedure

DELIMITER //
DROP PROCEDURE IF EXISTS createMesaVoto;
CREATE PROCEDURE createMesaVoto(IN p_unidade_organica_nome varchar(512), IN p_eleicao_id int, IN p_numero_cc INT) 
BEGIN 
  DECLARE id_lista INT;
  DECLARE numero_mesa INT;

  SELECT COUNT(*) INTO numero_mesa FROM mesa_voto WHERE eleicao_id = p_eleicao_id;

  INSERT INTO mesa_voto (numero, unidade_organica_nome, eleicao_id) VALUES (numero_mesa+1, p_unidade_organica_nome,p_eleicao_id);
  INSERT INTO mesa_voto_utilizador(mesa_voto_numero, eleicao_id, utilizador_numero_cc) VALUES (numero_mesa+1,p_eleicao_id, p_numero_cc);
END//
DELIMITER ;
