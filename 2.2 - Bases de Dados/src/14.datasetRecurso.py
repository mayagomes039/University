import arrow
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

# Disable foreign key checks
cursor.execute("SET FOREIGN_KEY_CHECKS = 0")

# Execute the DELETE query to remove existing rows
query_delete_recurso = "DELETE FROM Recurso"
cursor.execute(query_delete_recurso)

# Commit the deletion
cnx.commit()

# Enable foreign key checks
cursor.execute("SET FOREIGN_KEY_CHECKS = 1")

description1 = [
    "Estudar",
    "Aviso Teste",
    "Apoio",
    "Imprimir",
    "Trazer Quarta",
    "Materia Mini",
    "Teste 1",
    "Teste 2"
]

#ALTER TABLE Recurso MODIFY COLUMN DESCRICAO varchar(20);

anexos1 = [
    "PDF",
    "Power Point",
    "Documentos",
    "Ficheiros",
    "Anexos"
]


# Execute a DESCRIBE query to retrieve the table structure
query = "DESCRIBE Recurso"
cursor.execute(query)

# Fetch all the rows returned by the query
result = cursor.fetchall()

# Print the table structure
for row in result:
    print(row)


sequence = list(range(1, 101))
random_lines = random.sample(sequence, 10)

random_hours = [arrow.now().shift(hours=i).format('HH:mm') for i in range(24)]

query = "SELECT ID FROM Plataforma_de_Apoio"
cursor.execute(query)

plataforma_id = [row[0] for row in cursor.fetchall()]

for id in plataforma_id:
    for i, row in enumerate(random_lines):
        hour = random_hours[i]
        anexo = random.choice(anexos1)
        descricao = random.choice(description1)

        # Build the INSERT query
        query = "INSERT INTO Recurso (ANEXO, DESCRICAO, HORA_PUBLICACAO, ID_PLATAFORMA_DE_APOIO ) VALUES ( %s, %s, %s, %s)"
        values = (anexo, descricao, hour, id)

        # Execute the INSERT query
        cursor.execute(query, values)

# Commit the changes to the database
cnx.commit()

query3 = "SELECT * FROM Recurso"
cursor.execute(query3)
result3 = cursor.fetchall()

# Print the table structure
for row in result3:
    print(row)

# Close the cursor and database connection
cursor.close()
cnx.close()

print("Data inserted successfully!")