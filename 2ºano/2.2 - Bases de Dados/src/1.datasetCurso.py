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
cursor.execute(query_delete_turma)

# Delete all records from the 'Curso' table
query_delete_curso = "DELETE FROM Curso"
cursor.execute(query_delete_curso)"""

nomes = [
    "Ciências e Tecnologias", 
    "Artes", 
    "Economia", 
    "Humanidade"
    ]


for i in range(4):
    nome = nomes[i]

    # Build the INSERT query
    query = "INSERT INTO Curso (NOME) VALUES (%s)"
    values = (nome,)

    # Execute the INSERT query
    cursor.execute(query, values)

# Commit the changes to the database
cnx.commit()


query2 = "SELECT * FROM Curso"
cursor.execute(query2)

result2 = cursor.fetchall()

for row in result2:
    print(row)

# Close the cursor and database connection
cursor.close()
cnx.close()

print("Data inserted successfully!")