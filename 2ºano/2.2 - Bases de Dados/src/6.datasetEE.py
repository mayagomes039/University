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
dataset1_path = '../datasets/student_data.csv'  
dataset1 = read_dataset(dataset1_path)

# Read dataset 2
dataset2_path = '../datasets/Student_Behaviour.csv'  
dataset2 = read_dataset(dataset2_path)

# Read dataset 3
dataset3_path = '../datasets/xAPI-Edu-Data.csv'  
dataset3 = read_dataset(dataset3_path)


# Connect to MySQL database
cnx = mysql.connector.connect(
    host='localhost',
    user='root',
    password='teste',
    database='bd_tp',
    ssl_disabled=True
)

## Feature selection
def select_columns(dataset, columns):
    selected_dataset = []
    for record in dataset:
        selected_record = [record[column_index] for column_index in columns]
        selected_dataset.append(selected_record)
    return selected_dataset

selected_columns = [7, 9]
selected_dataset = select_columns(dataset1, selected_columns)

# Select 40 random lines from the dataset
random_lines = random.sample(selected_dataset, 40)

# Create a cursor to execute SQL queries
cursor = cnx.cursor()

"""# Delete all records from the 'Avaliacao' table
query_delete_turma = "DELETE FROM Avaliacao"
cursor.execute(query_delete_turma)

# Delete all records from the 'Aluno' table
query_delete_turma = "DELETE FROM Aluno"
cursor.execute(query_delete_turma)

# Execute a DESCRIBE query to retrieve the table structure
query = "DELETE FROM EncarregadoEducacao"  # Replace 'EncarregadoEducacao' with your actual table name
cursor.execute(query)"""

# Commit the deletion
cnx.commit()

# Execute a DESCRIBE query to retrieve the table structure
query = "DESCRIBE EncarregadoEducacao"  # Replace 'EncarregadoEducacao' with your actual table name
cursor.execute(query)

# Fetch all the rows returned by the query
result = cursor.fetchall()

# Print the table structure
for row in result:
    print(row)

fake = Faker()

random_names = [fake.name().split()[0] for _ in range(40)]

for i, row in enumerate(random_lines):
    nome = random_names[i]
    grauEscolaridade = row[0]
    profissao = row [1]

    # Build the INSERT query
    query = "INSERT INTO EncarregadoEducacao (NOME, PROFISSAO, GRAU_ESCOLARIDADE) VALUES ( %s, %s, %s)"
    values = ( nome, profissao, grauEscolaridade)

    # Execute the INSERT query
    cursor.execute(query, values)


# Commit the changes to the database
cnx.commit()

query3 = "SELECT * FROM EncarregadoEducacao"
cursor.execute(query3)

result3 = cursor.fetchall()

# Print the table structure
for row in result3:
    print(row)

# Close the cursor and database connection
cursor.close()
cnx.close()

print("Data inserted successfully!")