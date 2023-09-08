USE bd_tp;

-- Consultar todos os alunos de uma determinada turma
SELECT * FROM Aluno
WHERE Aluno.ID_TURMA = 34;

-- Consultar todos os professores de uma determinada turma
SELECT * FROM Turma_has_Professor
WHERE Turma_has_Professor.ID_TURMA = 34;

-- Consultar todas as notas dos alunos de um determinado curso
SELECT Aluno.NUMERO, Aluno.NOME, Avaliacao.NOTA
FROM Curso
JOIN Turma ON Turma.ID_CURSO = Curso.ID
JOIN Aluno ON Aluno.ID_TURMA = Turma.ID
LEFT JOIN Avaliacao ON Avaliacao.NUMERO_ALUNO = Aluno.NUMERO
WHERE Curso.ID = 3;

-- Consultar todas as notas dos alunos numa determinada disciplina
SELECT a.Nota, Al.nome from Avaliacao a, Aluno Al,Turma t, Curso c , Curso_has_Disciplina cd, Disciplina d
WHERE a.NUMERO_ALUNO=Al.Numero and 
Al.ID_TURMA= t.ID and 
t.ID_CURSO= c.ID and
c.ID=cd.ID_CURSO and 
cd.ID_DISCIPLINA = d.ID and 
d.ID=13;

-- otimizado:

SELECT a.Nota, Al.nome
FROM Avaliacao a
INNER JOIN Aluno Al ON a.NUMERO_ALUNO = Al.Numero
INNER JOIN Turma t ON Al.ID_TURMA = t.ID
INNER JOIN Curso c ON t.ID_CURSO = c.ID
INNER JOIN Curso_has_Disciplina cd ON c.ID = cd.ID_CURSO
INNER JOIN Disciplina d ON cd.ID_DISCIPLINA = d.ID
WHERE d.ID = 13;


-- Consultar as profissões dos encarregados de educação dos alunos de um curso 
SELECT Aluno.NOME, EncarregadoEducacao.PROFISSAO
FROM Curso 
JOIN Turma ON Curso.ID = Turma.ID_CURSO
JOIN Aluno ON Turma.ID = Aluno.ID_TURMA
JOIN EncarregadoEducacao ON Aluno.ID_EncarregadoEducacao = EncarregadoEducacao.ID
WHERE Curso.ID = 7;

-- Consultar a participação e assiduidade dos alunos de uma determinada turma
SELECT Estatistica.ASSIDUIDADE, Estatistica.PARTICIPACAO
FROM Aluno 
JOIN Estatistica ON Aluno.NUMERO = Estatistica.NUMERO_ALUNO
WHERE Aluno.ID_TURMA = 34;

-- Consultar os acessos a material de apoio de todos os alunos
SELECT Acesso.NUMERO_ALUNO
FROM Acesso;

-- Consultar a disponibilização de material de apoio por todos os professores
SELECT Recurso.ANEXO, Professor.ID AS ID_PROFESSOR
FROM Recurso 
INNER JOIN Acesso ON Recurso.ID = Acesso.ID_RECURSO
INNER JOIN Professor ON Acesso.ID_PROFESSOR = Professor.ID;


-- Consultar todas as aulas dadas numa determinada disciplina
SELECT Aula.ID, Aula.DATA_AULA, Aula.DURACAO
FROM Aula 
WHERE Aula.ID_DISCIPLINA = 18;


-- EXTRA
-- select * from Plataforma_de_Apoio

-- Consultar que materiais (recursos) os alunos acederam 
SELECT Aluno.NOME, Acesso.ID_RECURSO
FROM Aluno
LEFT JOIN Acesso ON Aluno.NUMERO = Acesso.NUMERO_ALUNO;

-- Consultar a participação e assiduidade de um determinado aluno
SELECT Estatistica.ASSIDUIDADE, Estatistica.PARTICIPACAO
FROM Estatistica 
WHERE Estatistica.NUMERO_ALUNO = 198;



