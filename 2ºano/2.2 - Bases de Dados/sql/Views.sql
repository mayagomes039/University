USE bd_tp;

-- Informação sobre todos os alunos 

CREATE VIEW Aluno_detalhado AS
SELECT A.NUMERO AS ID, A.NOME AS Nome, A.IDADE AS Idade,
	   A.SEXO AS Sexo, A.EMAIL AS Email, A.MEDIA AS Media,
       A.NUMERO_HORAS_ESTUDO_DIARIAS AS HORAS_ESTUDO,
       T.ID AS Turma, E.NOME AS EE
FROM Aluno AS A
	INNER JOIN Turma AS T
    ON A.ID_TURMA = T.ID
    INNER JOIN EncarregadoEducacao AS E
    ON A.ID_EncarregadoEducacao = E.ID
ORDER BY A.NOME;


-- Informação sobre os professores

CREATE VIEW Profesores_detalhados AS
SELECT P.ID AS ID, P.NOME AS Nome, P.EMAIL AS Email,
	   D.NOME AS Disciplina
FROM Professor AS P
	INNER JOIN Disciplina AS D
    ON P.ID_DISCIPLINA = D.ID 
ORDER BY P.NOME;


-- mostra as avaliações da escola

CREATE VIEW Avaliacoes AS
SELECT NUMERO, DATA_AULA, NOTA, ID_PROFESSOR, NUMERO_ALUNO, ID_DISCIPLINA
FROM Avaliacao;



SELECT * FROM Aluno_detalhado;






