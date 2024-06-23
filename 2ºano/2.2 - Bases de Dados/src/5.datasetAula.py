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
cursor.execute(query_delete_turma)"""


random_dates = [arrow.now().shift(days=-random.randint(1, 365)).format('YYYY-MM-DD') for _ in range(10)]
random_hours = [arrow.now().shift(hours=i).format('HH:mm') for i in range(10)]

query = "SELECT ID FROM Disciplina"
cursor.execute(query)
disciplina_id = [row[0] for row in cursor.fetchall()]


for id in disciplina_id:
        for i in range(3):
            duracao = random.choice([45, 90])
            data_aula = random.choice(random_dates)

            # Build the INSERT query
            query = "INSERT INTO Aula (DURACAO, DATA_AULA, ID_DISCIPLINA) VALUES (%s, %s, %s)"

            values = (duracao, data_aula, id)

            # Execute the INSERT query
            cursor.execute(query, values)

# Commit the changes to the database
cnx.commit()

query7 = "SELECT * FROM Aula"
cursor.execute(query7)

result7 = cursor.fetchall()

for row in result7:
    print(row)

# Close the cursor and database connection
cursor.close()
cnx.close()

print("Data inserted successfully!")