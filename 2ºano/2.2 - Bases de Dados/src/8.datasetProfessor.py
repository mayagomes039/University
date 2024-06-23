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

query = "SET FOREIGN_KEY_CHECKS=0"
cursor.execute(query)

query = "SET FOREIGN_KEY_CHECKS=1"
cursor.execute(query)

fake = Faker()

sequence = list(range(1, 101))
random_lines = random.sample(sequence, 7)
random_names = [fake.name().split()[0] for _ in range(7)]
random_emails = [fake.email() for _ in range(7)]

query = "SELECT ID FROM Disciplina"
cursor.execute(query)
disc_id = [row[0] for row in cursor.fetchall()]

query = "SELECT ID, NOME FROM Disciplina"
cursor.execute(query)
disciplinas = cursor.fetchall()

professores_por_disciplina = {}  # Dicionário para associar professores a disciplinas

for id, nome_disciplina in disciplinas:
    if not random_names:
        # Se a lista random_names estiver vazia, recria com novos nomes
        random_names = [fake.name().split()[0] for _ in range(7)]
        random_emails = [fake.email() for _ in range(7)]

    if nome_disciplina not in professores_por_disciplina:
        # Se o nome da disciplina ainda não está no dicionário, associa um novo professor
        nome = random_names.pop(0)
        email = random_emails.pop(0)
        professores_por_disciplina[nome_disciplina] = (nome, email)

    nome, email = professores_por_disciplina[nome_disciplina]
    values = (nome, email, id)

    # Build the INSERT query
    query = "INSERT INTO Professor (NOME, EMAIL, ID_DISCIPLINA) VALUES (%s, %s, %s)"

    # Execute the INSERT query
    cursor.execute(query, values)

# Commit the changes to the database
cnx.commit()

query2 = "SELECT * FROM Professor"
cursor.execute(query2)

result2 = cursor.fetchall()

# Print the table structure
for row in result2:
    print(row)

# Close the cursor and database connection
cursor.close()
cnx.close()

print("Data inserted successfully!")