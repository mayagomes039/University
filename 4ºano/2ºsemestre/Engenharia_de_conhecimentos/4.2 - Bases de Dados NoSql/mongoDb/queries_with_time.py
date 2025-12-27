import time
from pymongo import MongoClient
from queries import query_1, query_2, query_3, query_4, query_5, query_6, query_7, query_8, query_9, query_10, query_11, query_12, query_13  

mongo_config = {
    "host": "localhost",
    "port": 27017,
    "db_name": "bookstore"  # tp
}

def connect_to_mongo():
    try:
        client = MongoClient(f"mongodb://{mongo_config['host']}:{mongo_config['port']}")
        db = client[mongo_config["db_name"]]
        print("Conectado ao MongoDB")
        return db
    except Exception as e:
        print("Erro ao conectar ao MongoDB:", e)
        return None

def measure_query_time(query_func, db, label):
    print(f"--- Executando: {label} ---")
    start_time = time.perf_counter()
    results = query_func(db)  # Passa a instância db à query
    end_time = time.perf_counter()
    duration = (end_time - start_time) * 1000
    print(f"{label} - Tempo de execução: {duration:.2f} ms")
    print(f"Total de documentos encontrados: {len(results)}\n")
    return duration

def main(db):
    timings = {}
    timings["Query 1"] = measure_query_time(query_1, db, "Query 1:")
    timings["Query 2"] = measure_query_time(query_2, db, "Query 2:")
    timings["Query 3"] = measure_query_time(query_3, db, "Query 3:")
    timings["Query 4"] = measure_query_time(query_4, db, "Query 4:")
    timings["Query 5"] = measure_query_time(query_5, db, "Query 5:")
    timings["Query 6"] = measure_query_time(query_6, db, "Query 6:")
    timings["Query 7"] = measure_query_time(query_7, db, "Query 7:")
    timings["Query 8"] = measure_query_time(query_8, db, "Query 8:")
    timings["Query 9"] = measure_query_time(query_9, db, "Query 9:")
    timings["Query 10"] = measure_query_time(query_10, db, "Query 10:")
    timings["Query 11"] = measure_query_time(query_11, db, "Query 11:")
    timings["Query 12"] = measure_query_time(query_12, db, "Query 12:")
    timings["Query 13"] = measure_query_time(query_13, db, "Query 13:")

    print("\n--- Resumo dos Tempos de Execução ---")
    for label, time_ms in timings.items():
        print(f"{label}: {time_ms:.2f} ms")

if __name__ == "__main__":
    db = connect_to_mongo()
    if db is not None:
        main(db)
