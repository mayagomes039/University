USE bd_tp;


INSERT INTO Curso
(ID, NOME)
VALUES
(1, "Ciências e Tecnologias"), 
(2,"Artes"), 
(3,"Economia"), 
(4,"Humanidade");

INSERT INTO Disciplina
(ID, NOME, MEDIA, ID_CURSO)
VALUES
 (1,'Matemática', 0, 1),
 (2,'Matemática', 0, 3),
 (3,'Português', 0, 1),
 (4,'Português', 0, 2),
 (5,'Português', 0, 3),
 (6,'Português', 0, 4),
 (7,'Desenho', 0, 2),
 (8,'Filosofia', 0, 1),
 (9,'Filosofia', 0, 3),
 (10,'Filosofia', 0, 4),
 (11,'Biologia e Geologia', 0, 1),
 (12,'Psicologia', 0, 1),
 (13,'Psicologia', 0, 4),
 (14,'TIC', 0, 1),
 (15,'TIC', 0, 3),
 (16,'TIC', 0, 4);

INSERT INTO Plataforma_de_Apoio
(ID, NOME)
VALUES
(1,"Plataforma de apoio Aristoteles");

