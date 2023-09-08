#nao da print pq a turma ainda nao da tbm 
#esta a dar todos se for possivel reduzir para 10

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

"""# Execute a DELETE query to clear the 'Turma_has_Professor' table
query = "DELETE FROM Turma_has_Professor"
cursor.execute(query)"""

query = "SELECT ID FROM Turma"
cursor.execute(query)
turma_ids = [row[0] for row in cursor.fetchall()]

query = "SELECT ID FROM Professor"
cursor.execute(query)
professor_id = [row[0] for row in cursor.fetchall()]

for id in turma_ids:
    random_professors = random.sample(professor_id, 4)  # Seleciona 4 professores aleatórios

    for id1 in random_professors:
        # Build the INSERT query
        query = "INSERT INTO Turma_has_Professor (ID_TURMA, ID_PROFESSOR) VALUES (%s, %s)"
        values = (id, id1)

        # Execute the INSERT query
        cursor.execute(query, values)


# Commit the changes to the database
cnx.commit()

query2 = "SELECT * FROM Turma_has_Professor"
cursor.execute(query2)
result2 = cursor.fetchall()

for row in result2:
    print(row)

# Close the cursor and database connection
cursor.close()
cnx.close()

print("Data inserted successfully!")