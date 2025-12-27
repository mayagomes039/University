import datetime
from pymongo import MongoClient

# -------------------------------
# Ficheiro com o equivalente ao procedures.sql mas para MongoDB
# -------------------------------
mongo_config = {
    "host": "localhost",
    "port": 27017,
    "db_name": "bookstore"  #"tp" 
}

# Conectar ao MongoDB
def connect_to_mongo():
    try:
        client = MongoClient(f"mongodb://{mongo_config['host']}:{mongo_config['port']}")
        db = client[mongo_config["db_name"]]
        print("Conectado ao MongoDB")
        return db
    except Exception as e:
        print("Erro ao conectar ao MongoDB:", e)
        return None

def get_status_value(mongo_db, status_id):
    status_doc = mongo_db["order_status"].find_one({"status_id": status_id})
    return status_doc["status_value"] if status_doc else "Desconhecido"

# Função que simula a procedure
def update_order_status(mongo_db, order_id, new_status_id):
    orders_collection = mongo_db["orders"]

    order = orders_collection.find_one({"_id": order_id})

    if not order:
        print(f"Pedido com ID {order_id} não encontrado.")
        return

    order_history = order.get("order_history", [])
    
    last_status_id = None
    if order_history:
        last_status = sorted(order_history, key=lambda x: x["status_date"], reverse=True)[0]
        last_status_id = last_status["status_id"]

    if last_status_id != new_status_id:
        new_status = {
            "history_id": len(order_history) + 1,
            "status_id": new_status_id,
            "status_value": get_status_value(mongo_db, new_status_id),
            "status_date": datetime.datetime.utcnow()
        }

        result = orders_collection.update_one(
            {"_id": order_id},
            {"$push": {"order_history": new_status}}
        )

        if result.modified_count > 0:
            print(f"Status atualizado com sucesso para o pedido {order_id}")
        else:
            print("Ocorreu um erro ao atualizar o status.")
    else:
        print(f"O status do pedido {order_id} já está como {new_status_id}")

if __name__ == "__main__":
    db = connect_to_mongo()
    if db is not None:
        # Exemplo: Atualizar o status do pedido com ID 1234 para status 2
        update_order_status(db, order_id=1234, new_status_id=2)
