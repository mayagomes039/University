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


"""# Execute a DESCRIBE query to retrieve the table structure
query = "DELETE FROM Acesso"
cursor.execute(query)

# Delete all records from the 'Recurso' table
query_delete_turma = "DELETE FROM Recurso"
cursor.execute(query_delete_turma)

# Execute a DESCRIBE query to retrieve the table structure
query = "DELETE FROM Plataforma_de_Apoio"  
# Replace 'EncarregadoEducacao' with your actual table name
cursor.execute(query)"""

sequence = list(range(1, 101))
random_lines = random.sample(sequence, 1)


for i, row in enumerate(random_lines):
    nome = "Plataforma de apoio Aristoteles"

    # Build the INSERT query
    query = "INSERT INTO Plataforma_de_Apoio (NOME) VALUES (%s)"
    values = (nome, )

    # Execute the INSERT query
    cursor.execute(query, values)

# Commit the changes to the database
cnx.commit()

query2 = "SELECT * FROM Plataforma_de_Apoio"
cursor.execute(query2)

result2 = cursor.fetchall()

# Print the table structure
for row in result2:
    print(row)

# Close the cursor and database connection
cursor.close()
cnx.close()

print("Data inserted successfully!")