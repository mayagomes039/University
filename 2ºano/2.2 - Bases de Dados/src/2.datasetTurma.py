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

# Create a cursor to execute SQL queries
cursor = cnx.cursor()

# Delete all records from the 'Acesso' table
query_delete_turma = "DELETE FROM Acesso"
cursor.execute(query_delete_turma)

"""# Delete all records from the 'Estatistica' table
query_delete_turma = "DELETE FROM Estatistica"
cursor.execute(query_delete_turma)

# Delete all records from the 'Avaliacao' table
query_delete_turma = "DELETE FROM Avaliacao"
cursor.execute(query_delete_turma)

# Delete all records from the 'Aluno' table
query_delete_turma = "DELETE FROM Aluno"
cursor.execute(query_delete_turma)

# Delete all records from the 'Turma' table
query_delete_turma = "DELETE FROM Turma"""
cursor.execute(query_delete_turma)

query3 = "DESCRIBE Turma"
cursor.execute(query3)

result3 = cursor.fetchall()

# Print the table structure
for row in result3:
    print(row)

# Populating the Turma table
n_alunos = 5

query = "SELECT ID FROM Curso"
cursor.execute(query)

curso_ids = [row[0] for row in cursor.fetchall()]

for id in curso_ids:
    for i in range(2):
    # Build the INSERT query
        query = "INSERT INTO Turma (N_ALUNOS, ID_CURSO) VALUES (%s,%s)"
        values = (n_alunos,id)

    # Execute the INSERT query
        cursor.execute(query, values)

# Commit the changes to the database
cnx.commit()

query5 = "SELECT * FROM Turma"
cursor.execute(query5)

result5 = cursor.fetchall()

for row in result5:
    print(row)

# Close the cursor and database connection
cursor.close()
cnx.close()

print("Data inserted successfully!")