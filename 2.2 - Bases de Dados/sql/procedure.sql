
-- Procedimentos para obter os alunos por nome
DELIMITER $$
CREATE PROCEDURE BuscarAlunosPorNome
   (IN p_nome VARCHAR(45))
BEGIN
   SELECT * FROM Aluno WHERE NOME = p_nome;
END$$
DELIMITER ;


-- Procedimentos para obter os alunos com média >= 10
DELIMITER $$
CREATE PROCEDURE BuscarAlunosPorMedia()
BEGIN
   SELECT * FROM Aluno WHERE MEDIA >= 10;
END$$
DELIMITER ;


-- Procedimentos para obter os professores por nome
DELIMITER $$
CREATE PROCEDURE BuscarProfsPorNome
   (IN p_nome VARCHAR(45))
BEGIN
   SELECT * FROM Professor WHERE NOME = p_nome;
END$$
DELIMITER ;


-- Procedimentos para obter as disciplinas por nome
DELIMITER $$
CREATE PROCEDURE BuscarDiscPorNome
   (IN p_nome VARCHAR(45))
BEGIN
   SELECT * FROM Disciplina WHERE NOME = p_nome;
END$$
DELIMITER ;


-- select * from Disciplina 
CALL BuscarAlunosPorNome ('Jeff Dennis');
CALL BuscarAlunosPorMedia;
CALL BuscarProfsPorNome ('Eric Smith');
CALL BuscarDiscPorNome ('Matemática');
