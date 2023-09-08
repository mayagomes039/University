-- SEGURANÇA SQL
SELECT User FROM mysql.user WHERE User = 'Diretor' AND Host = 'localhost';
DROP USER 'Diretor'@'localhost';
DROP USER 'Aluno'@'localhost';
DROP USER 'EncarregadoEducacao'@'localhost';
DROP USER 'Professor'@'localhost';


-- Diretor (Administrador )
USE bd_tp;

CREATE USER 'Diretor'@'localhost'
	identified by 'adminpassword';
GRANT SELECT, INSERT, DELETE, UPDATE 
ON *.* 
TO 'Diretor'@'localhost';
REVOKE DROP, CREATE 
ON *.* 
FROM 'Diretor'@'localhost';



-- Aluno

CREATE USER  'Aluno'@'localhost'
	identified by 'alunopassword';
GRANT SELECT ON bd_tp.EncarregadoEducacao TO 'Aluno'@'localhost';
GRANT SELECT ON bd_tp.Avaliacao TO 'Aluno'@'localhost';
GRANT SELECT, DELETE, UPDATE
(NOME, IDADE, SEXO,
EMAIL, NUMERO_HORAS_ESTUDO_DIARIAS)
ON bd_tp.Aluno TO  'Aluno'@'localhost';
GRANT SELECT, DELETE, INSERT, UPDATE 
ON bd_tp.Aluno TO 'Aluno'@'localhost';

REVOKE DROP, CREATE
ON bd_tp.Aluno 
FROM 'Aluno'@'localhost';
REVOKE DROP, CREATE, DELETE, UPDATE, INSERT
ON bd_tp.EncarregadoEducacao 
FROM 'Aluno'@'localhost';
REVOKE DROP, CREATE, DELETE, UPDATE, INSERT
ON bd_tp.Avaliacao 
FROM 'Aluno'@'localhost';



-- Encarregado de Educação
CREATE USER 'EncarregadoEducacao'@'localhost'
	identified BY 'eepassword';
GRANT SELECT ON bd_tp.Aluno TO 'EncarregadoEducacao'@'localhost';
GRANT SELECT ON bd_tp.Avaliacao TO 'EncarregadoEducacao'@'localhost';
GRANT SELECT, DELETE, UPDATE
(NOME , PROFISSAO, GRAU_ESCOLARIDADE)
ON bd_tp.EncarregadoEducacao 
TO 'EncarregadoEducacao'@'localhost';
GRANT SELECT, INSERT, UPDATE ON bd_tp.EncarregadoEducacao TO 'EncarregadoEducacao'@'localhost';

REVOKE DROP, CREATE
ON bd_tp.EncarregadoEducacao
FROM 'EncarregadoEducacao'@'localhost';
REVOKE DROP, CREATE, DELETE, UPDATE, INSERT
ON bd_tp.Aluno
FROM 'EncarregadoEducacao'@'localhost';
REVOKE DROP, CREATE, DELETE, UPDATE, INSERT
ON bd_tp.Avaliacao
FROM 'EncarregadoEducacao'@'localhost';

-- Professor
CREATE USER 'Professor'@'localhost'
	identified BY 'professorpassword';
GRANT SELECT ON bd_tp.Aluno TO 'Professor'@'localhost';
GRANT SELECT ON bd_tp.Disciplina TO 'Professor'@'localhost';
GRANT SELECT ON bd_tp.Avaliacao TO 'Professor'@'localhost';
GRANT SELECT, DELETE, UPDATE
(NOME , EMAIL)
ON bd_tp.Professor 
TO 'Professor'@'localhost';
GRANT SELECT, INSERT, UPDATE 
ON bd_tp.Professor 
TO 'Professor'@'localhost';

REVOKE DROP, CREATE
ON bd_tp.Professor
FROM 'Professor'@'localhost';
REVOKE DROP, CREATE, DELETE, UPDATE, INSERT
ON bd_tp.Aluno
FROM 'Professor'@'localhost';
REVOKE DROP, CREATE, DELETE, UPDATE, INSERT
ON bd_tp.Disciplina
FROM 'Professor'@'localhost';
REVOKE DROP, CREATE, DELETE, UPDATE, INSERT
ON bd_tp.Avaliacao
FROM 'Professor'@'localhost';











