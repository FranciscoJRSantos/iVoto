DELIMITER $$
CREATE TRIGGER after_eleicao_create 
    AFTER INSERT ON eleicao
    FOR EACH ROW 
BEGIN
  INSERT INTO lista (nome,tipo_utilizador,votos,eleicao_id) VALUES ("Blank",0,0,new.ID);
  INSERT INTO lista (nome,tipo_utilizador,votos,eleicao_id) VALUES ("Null",0,0,new.ID);
END$$
DELIMITER ;
