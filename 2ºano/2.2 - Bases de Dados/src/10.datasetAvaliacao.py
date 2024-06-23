

import arrow
import random
from faker import Faker
import mysql.connector
from mysql.connector import errorcode



# Connect to MySQL database
cnx = mysql.connector.connect(
    host='localhost',
    user='root',
    password='teste',
    database='bd_tp',
    ssl_disabled=True
)
## Feature selection
# Create a cursor to execute SQL queries
cursor = cnx.cursor()

"""# Delete all records from the 'Avaliacao' table
query_delete_aula = "DELETE FROM Avaliacao"
cursor.execute(query_delete_aula)
# Commit the deletion
cnx.commit()"""

random_dates = [arrow.now().shift(days=-random.randint(1, 365)).format('YYYY-MM-DD') for _ in range(10)]
cursor.execute("SELECT NUMERO, ID_TURMA FROM Aluno")
alunos = cursor.fetchall()
for idAluno, idTurma in alunos:
    cursor.execute(f"SELECT ID_CURSO FROM Turma WHERE Turma.ID = {idTurma}")
    idCurso = cursor.fetchone()[0]

    cursor.execute(f"SELECT ID_DISCIPLINA FROM Curso_Has_Disciplina WHERE ID_CURSO = {idCurso}")
    idDisciplinas = cursor.fetchall()

    for idDisciplina in idDisciplinas:
        cursor.execute(f"SELECT ID FROM Professor WHERE ID_DISCIPLINA = {idDisciplina[0]}")
        idProfessores = cursor.fetchall()

        for i in range(2):
            nota_random = (random.randint(0, 2000))/100
            prof_random = random.choice(idProfessores)[0]
            data_random = random.choice(random_dates)
            cursor.execute(f'INSERT INTO Avaliacao (NOTA, ID_PROFESSOR, NUMERO_ALUNO, ID_DISCIPLINA, DATA_AULA) VALUES ({nota_random}, {prof_random}, {idAluno}, {idDisciplina[0]}, "{data_random}")')
            cnx.commit()

# atualizar as medias dos ALUNOS
for idAluno, idTurma in alunos:
    cursor.execute(f"SELECT NOTA FROM Avaliacao WHERE Avaliacao.NUMERO_ALUNO = {idAluno}")
    sum = 0
    notas = cursor.fetchall()
    i = len(notas)
    for nota in notas:
        sum += float(nota[0])
    media = sum/i
    cursor.execute(f"UPDATE Aluno SET MEDIA = {media} WHERE Aluno.NUMERO = {idAluno}")
    cnx.commit()

# atualizar as medias das DISCIPLINAS
cursor.execute("SELECT ID FROM DISCIPLINA")
disciplinas = cursor.fetchall()

for disciplina in disciplinas:
    cursor.execute(f"SELECT NOTA FROM Avaliacao WHERE Avaliacao.ID_DISCIPLINA = {disciplina[0]}")
    sum = 0
    notas = cursor.fetchall()
    i = len(notas)
    for nota in notas:
        sum += float(nota[0])
    media = sum/i
    cursor.execute(f"UPDATE Disciplina SET MEDIA = {media} WHERE Disciplina.ID = {disciplina[0]}")
    cnx.commit()



# Close the cursor and database connection
cursor.close()
cnx.close()

print("Data inserted successfully!")
