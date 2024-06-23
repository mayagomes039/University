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