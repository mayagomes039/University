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

"""# Execute a DELETE query to clear the 'Acesso' table
query = "DELETE FROM Acesso"  # Replace 'Acesso' with your actual table name
cursor.execute(query)"""

fake = Faker()
random_emails = [fake.email() for _ in range(30)]

# Execute a DESCRIBE query to retrieve the table structure
query = "DESCRIBE Acesso"
cursor.execute(query)

# Fetch all the rows returned by the query
result = cursor.fetchall()

# Print the table structure
for row in result:
    print(row)

query = "SELECT ID FROM Professor"
cursor.execute(query)
prof_ids = [row[0] for row in cursor.fetchall()]

query = "SELECT NUMERO FROM Aluno"
cursor.execute(query)
aluno_ids = [row[0] for row in cursor.fetchall()]

query = "SELECT ID FROM Recurso"
cursor.execute(query)
recurso_ids = [row[0] for row in cursor.fetchall()]

if len(recurso_ids) == 0:
    print("No 'recurso_ids' available. Make sure the table 'Recurso' has records.")

sequence = list(range(1, 101))
random_lines = random.sample(sequence, 30)

for i, row in enumerate(random_lines):
    prof_id = prof_ids[i % len(prof_ids)]
    aluno_id = aluno_ids[i % len(aluno_ids)]

    # Check if recurso_ids is not empty before accessing its elements
    if len(recurso_ids) > 0:
        recurso_id = recurso_ids[i % len(recurso_ids)]
    else:
        break

    passe = random_emails[i]

    # Build the INSERT query
    query = "INSERT INTO Acesso (PASSE, ID_RECURSO, ID_PROFESSOR, NUMERO_ALUNO) VALUES (%s, %s, %s, %s)"
    values = (passe, recurso_id, prof_id, aluno_id)

    # Execute the INSERT query
    cursor.execute(query, values)

# Commit the changes to the database
cnx.commit()

query3 = "SELECT * FROM Acesso"
cursor.execute(query3)
result3 = cursor.fetchall()

# Print the table structure
for row in result3:
    print(row)

# Close the cursor and database connection
cursor.close()
cnx.close()

print("Data inserted successfully!")