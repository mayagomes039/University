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

# Execute a DELETE query to clear the 'Curso_has_Disciplina' table
query = "DELETE FROM Curso_has_Disciplina"
cursor.execute(query)


queries = [
     "INSERT INTO Curso_has_Disciplina (ID_CURSO, ID_DISCIPLINA) VALUES (1, 1)",
     "INSERT INTO Curso_has_Disciplina (ID_CURSO, ID_DISCIPLINA) VALUES (1, 3)",
     "INSERT INTO Curso_has_Disciplina (ID_CURSO, ID_DISCIPLINA) VALUES (1, 8)",
     "INSERT INTO Curso_has_Disciplina (ID_CURSO, ID_DISCIPLINA) VALUES (1, 11)",
     "INSERT INTO Curso_has_Disciplina (ID_CURSO, ID_DISCIPLINA) VALUES (1, 12)",
     "INSERT INTO Curso_has_Disciplina (ID_CURSO, ID_DISCIPLINA) VALUES (1, 14)",
     "INSERT INTO Curso_has_Disciplina (ID_CURSO, ID_DISCIPLINA) VALUES (2, 4)",
     "INSERT INTO Curso_has_Disciplina (ID_CURSO, ID_DISCIPLINA) VALUES (2, 7)",
     "INSERT INTO Curso_has_Disciplina (ID_CURSO, ID_DISCIPLINA) VALUES (3, 2)",
     "INSERT INTO Curso_has_Disciplina (ID_CURSO, ID_DISCIPLINA) VALUES (3, 5)",
     "INSERT INTO Curso_has_Disciplina (ID_CURSO, ID_DISCIPLINA) VALUES (3, 9)",
     "INSERT INTO Curso_has_Disciplina (ID_CURSO, ID_DISCIPLINA) VALUES (3, 15)",
     "INSERT INTO Curso_has_Disciplina (ID_CURSO, ID_DISCIPLINA) VALUES (4, 6)",
     "INSERT INTO Curso_has_Disciplina (ID_CURSO, ID_DISCIPLINA) VALUES (4, 10)",
     "INSERT INTO Curso_has_Disciplina (ID_CURSO, ID_DISCIPLINA) VALUES (4, 13)",
     "INSERT INTO Curso_has_Disciplina (ID_CURSO, ID_DISCIPLINA) VALUES (4, 16)"
]


for i in range(len(queries)):
    cursor.execute(queries[i])

# Commit the changes to the database
cnx.commit()

query2 = "SELECT * FROM Curso_has_Disciplina"
cursor.execute(query2)

result2 = cursor.fetchall()

for row in result2:
    print(row)

# Close the cursor and database connection
cursor.close()
cnx.close()

print("Data inserted successfully!")