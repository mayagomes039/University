
import mysql.connector
from mysql.connector import errorcode

cnx = mysql.connector.connect(
    host='localhost',
    user='root',
    password='teste',
    database='bd_tp',
    ssl_disabled=True
)

# Create a cursor to execute SQL queries
cursor = cnx.cursor()

# Delete related records from the 'acesso' table
query_delete_acesso = "DELETE FROM Acesso WHERE NUMERO_ALUNO IN (SELECT NUMERO FROM Aluno)"
cursor.execute(query_delete_acesso)

# Delete related records from the 'Hobbie_Academico' table
query_delete_hobbie = "DELETE FROM Hobbie_Academico WHERE NUMERO_ALUNO IN (SELECT NUMERO FROM Aluno)"
cursor.execute(query_delete_hobbie)

# Delete related records from the 'Avaliacao' table
query_delete_avaliacao = "DELETE FROM Avaliacao WHERE NUMERO_ALUNO IN (SELECT NUMERO FROM Aluno)"
cursor.execute(query_delete_avaliacao)

# Delete related records from the 'Estatistica' table
query_delete_estatistica = "DELETE FROM Estatistica WHERE NUMERO_ALUNO IN (SELECT NUMERO FROM Aluno)"
cursor.execute(query_delete_estatistica)

# Delete all records from the 'Aluno' table
query_delete_aluno = "DELETE FROM Aluno"
cursor.execute(query_delete_aluno)

# Delete related records from the 'Aula' table
query_delete_aula = "DELETE FROM Aula WHERE ID_DISCIPLINA IN (SELECT ID FROM Disciplina)"
cursor.execute(query_delete_aula)

# Delete all records from the 'Disciplina' table
query_delete_disciplina = "DELETE FROM Disciplina"
cursor.execute(query_delete_disciplina)

# Delete related records from the 'Disciplina' table
query_delete_disciplina = "DELETE FROM Disciplina WHERE ID_CURSO IN (SELECT ID FROM Curso)"
cursor.execute(query_delete_disciplina)

# Delete related records from the 'Turma' table
query_delete_turma = "DELETE FROM Turma WHERE ID_CURSO IN (SELECT ID FROM Curso)"
cursor.execute(query_delete_turma)

# Delete all records from the 'Curso' table
query_delete_curso = "DELETE FROM Curso"
cursor.execute(query_delete_curso)

# Commit the changes to the database
cnx.commit()

# Close the cursor and database connection
cursor.close()
cnx.close()

print("Data inserted successfully!")