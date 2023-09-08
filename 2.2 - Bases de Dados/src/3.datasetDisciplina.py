
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

# Create a cursor to execute SQL queries
cursor = cnx.cursor()

"""# Delete all records from the 'Aula' table
query_delete_aula = "DELETE FROM Aula"
cursor.execute(query_delete_aula)

# Delete all records from the 'Disciplina' table
query_delete_c_has_d = "DELETE FROM Curso_has_Disciplina"
cursor.execute(query_delete_c_has_d)

# Delete all records from the 'Disciplina' table
query_delete_disciplina = "DELETE FROM Disciplina"
cursor.execute(query_delete_disciplina)

# Delete all records from the 'Turma' table
query_delete_turma = "DELETE FROM Turma"
cursor.execute(query_delete_turma)"""


cnx.commit()


queries = [
     "INSERT INTO Disciplina (NOME, MEDIA, ID_CURSO) VALUES ('Matemática', 0, 1)",
     "INSERT INTO Disciplina (NOME, MEDIA, ID_CURSO) VALUES ('Matemática', 0, 3)",
     "INSERT INTO Disciplina (NOME, MEDIA, ID_CURSO) VALUES ('Português', 0, 1)",
     "INSERT INTO Disciplina (NOME, MEDIA, ID_CURSO) VALUES ('Português', 0, 2)",
     "INSERT INTO Disciplina (NOME, MEDIA, ID_CURSO) VALUES ('Português', 0, 3)",
     "INSERT INTO Disciplina (NOME, MEDIA, ID_CURSO) VALUES ('Português', 0, 4)",
     "INSERT INTO Disciplina (NOME, MEDIA, ID_CURSO) VALUES ('Desenho', 0, 2)",
     "INSERT INTO Disciplina (NOME, MEDIA, ID_CURSO) VALUES ('Filosofia', 0, 1)",
     "INSERT INTO Disciplina (NOME, MEDIA, ID_CURSO) VALUES ('Filosofia', 0, 3)",
     "INSERT INTO Disciplina (NOME, MEDIA, ID_CURSO) VALUES ('Filosofia', 0, 4)",
     "INSERT INTO Disciplina (NOME, MEDIA, ID_CURSO) VALUES ('Biologia e Geologia', 0, 1)",
     "INSERT INTO Disciplina (NOME, MEDIA, ID_CURSO) VALUES ('Psicologia', 0, 1)",
     "INSERT INTO Disciplina (NOME, MEDIA, ID_CURSO) VALUES ('Psicologia', 0, 4)",
     "INSERT INTO Disciplina (NOME, MEDIA, ID_CURSO) VALUES ('TIC', 0, 1)",
     "INSERT INTO Disciplina (NOME, MEDIA, ID_CURSO) VALUES ('TIC', 0, 3)",
     "INSERT INTO Disciplina (NOME, MEDIA, ID_CURSO) VALUES ('TIC', 0, 4)"
]


for i in range(len(queries)):
    cursor.execute(queries[i])


# Commit the changes to the database
cnx.commit()

query5 = "SELECT * FROM Disciplina"
cursor.execute(query5)

result5 = cursor.fetchall()

for row in result5:
    print(row)


# Close the cursor and database connection
cursor.close()
cnx.close()

print("Data inserted successfully!")