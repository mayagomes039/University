import random
from faker import Faker
import mysql.connector
from mysql.connector import errorcode

## Read datasets

def read_dataset(file_path):
    dataset = []
    with open(file_path, 'r') as file:
        for line in file:
            record = line.strip().split(',')
            dataset.append(record)
    return dataset

# Read dataset 1
dataset1_path = '../datasets/student_data.csv'  # Replace with the actual file path
dataset1 = read_dataset(dataset1_path)

# Read dataset 2
dataset2_path = '../datasets/Student_Behaviour.csv'  # Replace with the actual file path
dataset2 = read_dataset(dataset2_path)

# Read dataset 3
dataset3_path = '../datasets/xAPI-Edu-Data.csv'  # Replace with the actual file path
dataset3 = read_dataset(dataset3_path)

## Feature selection

def select_columns(dataset, columns):
    selected_dataset = []
    for record in dataset:
        selected_record = [record[column_index] for column_index in columns]
        selected_dataset.append(selected_record)
    return selected_dataset

selected_columns = [1, 2, 6, 8, 13, 17, 29, 30, 31, 32]
selected_dataset = select_columns(dataset1, selected_columns)

# Select 40 random lines from the dataset
random_lines = random.sample(selected_dataset[1:], 40)

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

"""# Delete all records from the 'Acesso' table
query_delete_turma = "DELETE FROM Acesso"
cursor.execute(query_delete_turma)

# Delete all records from the 'Hobbie_Academico' table
query_delete_turma = "DELETE FROM Hobbie_Academico"
cursor.execute(query_delete_turma)

# Delete all records from the 'Estatistica' table
query_delete_turma = "DELETE FROM Estatistica"
cursor.execute(query_delete_turma)

# Delete all records from the 'Avaliacao' table
query_delete_turma = "DELETE FROM Avaliacao"
cursor.execute(query_delete_turma)

# Delete all records from the 'Aluno' table
query_delete_turma = "DELETE FROM Aluno"
cursor.execute(query_delete_turma)"""

fake = Faker()

random_names = [fake.name().split()[0] for _ in range(40)]
random_emails = [fake.email() for _ in range(40)]

query = "SELECT ID FROM Turma"
cursor.execute(query)

turma_ids = [row[0] for row in cursor.fetchall()]

query = "SELECT ID FROM EncarregadoEducacao"
cursor.execute(query)
ee_ids = [row[0] for row in cursor.fetchall()]

for i, row in enumerate(random_lines):
    turma_id = turma_ids[i % len(turma_ids)]
    ee_id = ee_ids[i % len(ee_ids)]
    sexo = row[0]
    idade = row[1]
    numero_horas_estudo_diarias = row[7]
    nome = random_names[i]
    email = random_emails[i]

    
    media = 0

            # Build the INSERT query
    query = "INSERT INTO Aluno (SEXO, IDADE, NUMERO_HORAS_ESTUDO_DIARIAS, MEDIA, NOME, EMAIL, ID_TURMA, ID_EncarregadoEducacao) VALUES (%s, %s, %s, %s, %s,%s,%s, %s)"
    values = (sexo, idade, numero_horas_estudo_diarias, media, nome, email, turma_id, ee_id)

            # Execute the INSERT query
    cursor.execute(query, values)

# Commit the changes to the database
cnx.commit()

query2 = "SELECT * FROM Aluno"
cursor.execute(query2)

result2 = cursor.fetchall()

for row in result2:
    print(row)

# Close the cursor and database conn
# ection
cursor.close()
cnx.close()

print("Data inserted successfully!")