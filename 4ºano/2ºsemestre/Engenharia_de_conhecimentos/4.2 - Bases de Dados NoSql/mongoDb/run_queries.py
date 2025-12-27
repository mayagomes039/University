from pymongo import MongoClient
from queries import query_1, query_2, query_3, query_4, query_5, query_6, query_7, query_8, query_9, query_10, query_11, query_12, query_13
import json


mongo_config = {
    "host": "localhost",
    "port": 27017,
    "db_name": "bookstore" #tp
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

def print_query_results(query_func, name, db):
    print(f"\n=== Resultados de {name} ===")
    results = query_func(db)
    print(f"Tipo: {type(results)}")
    print(f"Número de resultados: {len(results)}")
    for doc in results:
        print(doc)

#debug para query 8, ver quantos ela devolve
def count_customers_with_multiple_addresses(db):
    pipeline = [
        {"$project": {"address_count": {"$size": "$addresses"}}},
        {"$match": {"address_count": {"$gt": 1}}},
        {"$count": "total_clientes_multiplos_enderecos"}
    ]

    result = list(db.customers.aggregate(pipeline))
    return result  

#debug para query 9, ver quantos ela devolve
def count_language_publisher_combinations(db):
    pipeline = [
        {
            "$group": {
                "_id": {
                    "language_id": "$language.language_id",
                    "publisher_id": "$publisher.publisher_id"
                }
            }
        },
        {"$count": "total_combinacoes"}
    ]

    return list(db.books.aggregate(pipeline))  


def save_results_to_file(results, filename):
    with open(filename, "w", encoding="utf-8") as f:
        json.dump(results, f, ensure_ascii=False, indent=4)
    print(f"Resultados guardados em {filename}")


if __name__ == "__main__":
    db = connect_to_mongo()
    if db is not None:
        results = query_13(db)
        print(f"Obtidos {len(results)} resultados da Query 8")
        save_results_to_file(results, "query_8_results.json")
        #print_query_results(query_12, "Query 1", db)

#print_query_results(query_2, "Query 2", db)
# print_query_results(query_3, "Query 3", db)
# print_query_results(query_4, "Query 4", db)
# print_query_results(query_5, "Query 5", db)
# print_query_results(query_6, "Query 6", db)
# print_query_results(query_7, "Query 7", db)

# print_query_results(query_8, "Query 8", db)
# print_query_results(count_customers_with_multiple_addresses, "Query 8 - debug", db)
        #results = query_8(db)
        #print(f"Obtidos {len(results)} resultados da Query 8")
        #save_results_to_file(results, "query_8_results.json")

# print_query_results(query_9, "Query 9", db)
# print_query_results(count_language_publisher_combinations, "Query 9 - debug", db)
# print_query_results(query_10, "Query 10", db)
#print_query_results(query_11, "Query 11", db, order_id="1")
# print_query_results(query_12, "Query 12", db)
# print_query_results(query_13, "Query 13", db)