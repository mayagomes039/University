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
query = "DELETE FROM Hobbie_Academico"  # Replace 'Hobbie_Academico' with your actual table name
cursor.execute(query)"""

"""fake = Faker()
description = fake.text(max_nb_chars=45) """ # Generate a random text with a maximum of 45 characters

descriptions = [
    "Xadrez",
    "Futebol",
    "Clube de matemática, basebol",
    "Basquete",
    "Xadrez e natação",
    "Vólei",
    "Dança",
    "Desenho e Xadrez",
    "Futebol, basquete e clube de matemática",
    "Natação e Futebol",
    "Dança e xadrez",
    "Dança e vólei",
    "Vólei e clube de matemática"
]

# Execute a DESCRIBE query to retrieve the table structure
query = "DESCRIBE Hobbie_Academico"
cursor.execute(query)

# Fetch all the rows returned by the query
result = cursor.fetchall()

# Print the table structure
for row in result:
    print(row)

query = "SELECT NUMERO FROM Aluno"
cursor.execute(query)
aluno_ids = [row[0] for row in cursor.fetchall()]


sequence = list(range(1, 101))
random_lines = random.sample(sequence, 30)

for i, row in enumerate(random_lines):
    #descricao = description [i]
    descricao = random.choice(descriptions)
    aluno_id = aluno_ids[i % len(aluno_ids)]
    
    
    # Build the INSERT query
    query = "INSERT INTO Hobbie_Academico (DESCRICAO, NUMERO_ALUNO) VALUES (%s, %s)"
    values = (descricao, aluno_id)

    # Execute the INSERT query
    cursor.execute(query, values)

# Commit the changes to the database
cnx.commit()

query3 = "SELECT * FROM Hobbie_Academico"
cursor.execute(query3)
result3 = cursor.fetchall()

# Print the table structure
for row in result3:
    print(row)

# Close the cursor and database connection
cursor.close()
cnx.close()

print("Data inserted successfully!")