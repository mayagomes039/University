import pymongo
import time

# Configurações do MongoDB
MONGO_URI = "mongodb://localhost:27017/"
DB_NAME = "crime_data"
COLLECTION_NAME = "states_crime_data"

# Função para conectar ao MongoDB
def connect_to_mongo(uri, db_name):
    client = pymongo.MongoClient(uri)
    db = client[db_name]
    return db

# Função para medir o tempo de execução de uma consulta
def medir_tempo(query):
    db = connect_to_mongo(MONGO_URI, DB_NAME)
    collection = db[COLLECTION_NAME]
    
    start_time = time.time()
    
    # Execute a query
    result = list(collection.aggregate(query))
    
    end_time = time.time()
    tempo_decorrido = end_time - start_time
    
    return tempo_decorrido, result

# Query 1: Consultar a média dos salários mínimos por estado
query1 = [
    {
        '$group': {
            '_id': '$state_name',
            'state_minimum_wage': {'$avg': '$state_minimum_wage'}
        }
    },
    {
        '$project': {
            '_id': 0,
            'state_name': '$_id',
            'state_minimum_wage': 1
        }
    }
]
tempo_query1, result_query1 = medir_tempo(query1)
print(f'Tempo da Query 1: {tempo_query1:.4f} segundos')

# Query 2: Consultar as taxas de criminalidade por estado
query2 = [
    {
        '$project': {
            'state_name': 1,
            'data_rates_property_all': 1,
            'data_rates_violent_all': 1
        }
    }
]
tempo_query2, result_query2 = medir_tempo(query2)
print(f'Tempo da Query 2: {tempo_query2:.4f} segundos')

# Query 3: Consultar o total de crimes violentos por estado
query3 = [
    {
        '$project': {
            'state_name': 1,
            'data_totals_violent_all': 1
        }
    }
]
tempo_query3, result_query3 = medir_tempo(query3)
print(f'Tempo da Query 3: {tempo_query3:.4f} segundos')

# Query 4: Consultar o índice de CPI por estado
query4 = [
    {
        '$project': {
            'state_name': 1,
            'cpi_average': 1
        }
    }
]
tempo_query4, result_query4 = medir_tempo(query4)
print(f'Tempo da Query 4: {tempo_query4:.4f} segundos')

# Query 5: Consultar o nível educacional médio de homens e mulheres por estado
query5 = [
    {
        '$project': {
            'state_name': 1,
            'men_bachelors_degree': 1,
            'women_bachelors_degree': 1
        }
    }
]
tempo_query5, result_query5 = medir_tempo(query5)
print(f'Tempo da Query 5: {tempo_query5:.4f} segundos')

# Query 6: Selecionar todos os documentos da coleção 'states_crime_data' no MongoDB
query6_mongo = []
tempo_query6_mongo, result_query6_mongo = medir_tempo(query6_mongo)
print(f'Tempo da Query 6 (MongoDB): {tempo_query6_mongo:.4f} segundos')

# Exemplo de como imprimir o número de documentos retornados (opcional)
print(f"Total de documentos retornados (MongoDB): {len(result_query6_mongo)}")
