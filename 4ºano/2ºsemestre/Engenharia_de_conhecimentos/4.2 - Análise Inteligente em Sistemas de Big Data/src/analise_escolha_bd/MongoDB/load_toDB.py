import csv
import pymongo
from pymongo import MongoClient
import time  

# Configurações do MongoDB
MONGO_URI = "mongodb://localhost:27017/" 
DB_NAME = "crime_data"  
COLLECTION_NAME = "states_crime_data"  

# Função para conectar ao MongoDB
def connect_to_mongo(uri, db_name):
    client = MongoClient(uri)
    db = client[db_name]
    return db

# Função para carregar os dados do arquivo CSV e armazenar no MongoDB
def load_data_to_mongo(csv_file, db):
    start_time = time.time()  # Marca o início do tempo
    
    with open(csv_file, newline='', encoding='utf-8') as csvfile:
        reader = csv.DictReader(csvfile)
        collection = db[COLLECTION_NAME]
        
        for row in reader:
            row = {key.replace('.', '_'): value for key, value in row.items()}
            
            for key in row:
                if isinstance(row[key], str) and row[key].replace('.', '', 1).isdigit():
                    row[key] = float(row[key])
            
            collection.insert_one(row)
            print(f"Documento inserido: {row['State']}, {row['year']}")
    
    end_time = time.time()  # Marca o tempo final
    elapsed_time = end_time - start_time  # Calcula o tempo de execução
    print(f"Tempo total para inserir os dados no MongoDB: {elapsed_time:.2f} segundos")

if __name__ == "__main__":
    csv_file = "../datasets/merged_data_2.csv"  

    db = connect_to_mongo(MONGO_URI, DB_NAME)

    load_data_to_mongo(csv_file, db)

    print("Dados inseridos no MongoDB com sucesso!")
