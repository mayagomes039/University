import random
from faker import Faker
import mysql.connector
from mysql.connector import errorcode

# Read datasets
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

# Feature selection
def select_columns(dataset, columns):
    selected_dataset = []
    for record in dataset:
        selected_record = [record[column_index] for column_index in columns]
        selected_dataset.append(selected_record)
    return selected_dataset

selected_columns = [16, 17, 18]
selected_dataset = select_columns(dataset1, selected_columns)

# Select 10 random lines from the dataset
random_lines = random.sample(selected_dataset, 2)

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

"""# Execute a DELETE query to clear the 'Estatistica' table
query = "DELETE FROM Estatistica"
cursor.execute(query)"""

query = "SELECT NUMERO FROM Aluno"
cursor.execute(query)
aluno_ids = [row[0] for row in cursor.fetchall()]

for id in aluno_ids:
    for i, row in enumerate(random_lines):
        assiduidade = row[0]
        participacao = row[2]

        if assiduidade == "no":
            justificacao = random.choice(["yes", "no"])
        else:
            justificacao = None

        # Build the INSERT query
        query = "INSERT INTO Estatistica (ASSIDUIDADE, JUSTIFICACAO, PARTICIPACAO, NUMERO_ALUNO) VALUES (%s, %s, %s, %s)"
        values = (assiduidade, justificacao, participacao, id)

        # Execute the INSERT query
        cursor.execute(query, values)

# Commit the changes to the database
cnx.commit()

query2 = "SELECT * FROM Estatistica"
cursor.execute(query2)

result2 = cursor.fetchall()

for row in result2:
    print(row)

# Close the cursor and database connection
cursor.close()
cnx.close()

print("Data inserted successfully!")