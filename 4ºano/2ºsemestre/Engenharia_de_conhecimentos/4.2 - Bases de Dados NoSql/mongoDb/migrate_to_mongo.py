import oracledb
from pymongo import MongoClient
import datetime
import os 
import dotenv
#maybe use dotenv
 
oracle_config = {
    "username": "system",
    "password": "admin",
    "dsn": "localhost/orclcdb"
}

mongo_config = {
    #mongodb://localhost:27018"
    "host": "",
    "port": "",
    "db_name": "bookstore"

}

# client = MongoClient("mongodb://localhost:27018")
# db = client["music_tables"]
# collection = db["TITULO"]

def migrate_books(oracle_connection, mongo_db):
    print("-- Migrating books --")

    # create the collection, if it exists remove the data 
    books_collection = mongo_db["books"]
    books_collection.delete_many({})

    try:
        cursor = oracle_connection.cursor()

        query = """
            SELECT
                B.BOOK_ID,
                B.TITLE,
                B.ISBN13,
                B.NUM_PAGES,
                B.PUBLICATION_DATE,
                BL.LANGUAGE_id,
                BL.LANGUAGE_CODE,
                BL.LANGUAGE_NAME,
                P.PUBLISHER_ID,
                P.PUBLISHER_NAME,
                A.AUTHOR_ID,
                A.AUTHOR_NAME
        
            FROM BOOK B
            LEFT JOIN BOOK_LANGUAGE BL ON B.LANGUAGE_ID = BL.LANGUAGE_ID
            LEFT JOIN PUBLISHER P ON B.PUBLISHER_ID = P.PUBLISHER_ID
            LEFT JOIN BOOK_AUTHOR BA ON B.BOOK_ID = BA.BOOK_ID
            LEFT JOIN AUTHOR A ON BA.AUTHOR_ID = A.AUTHOR_ID
            ORDER BY B.BOOK_ID
        """

        cursor.execute(query)
        rows = cursor.fetchall()

        books = {}

        for row in rows:
            book_id = row[0]
            title = row[1]
            isbn13 = row[2]
            num_pages = row[3]
            publication_date = row[4] # datetime
            language_id = row[5]
            language_code = row[6]
            language_name = row[7]
            publisher_id = row[8]
            publisher_name = row[9]
            author_id = row[10]
            author_name = row[11]

            if book_id not in books:
                books[book_id] = {
                    "_id": book_id,
                    "title": title,
                    "isbn13": isbn13,
                    "num_pages": num_pages,
                    "publication_date": publication_date,
                    "language": {
                        "language_id": language_id,
                        "language_code": language_code,
                        "language_name": language_name
                    },
                    "publisher": {
                        "publisher_id": publisher_id,
                        "publisher_name": publisher_name
                    },
                    "authors": []
                }

            if author_id and {"author_id": author_id, "author_name": author_name} not in books[book_id]["authors"]:
                books[book_id]["authors"].append({
                    "author_id": author_id,
                    "author_name": author_name
                })
        
        if books:
            books_to_inset = list(books.values())
            books_collection.insert_many(books_to_inset)
            print(f"Inserted {len(books_to_inset)} books into MongoDB")
        else:
            print("error")
            return None
        
    except oracledb.DatabaseError as e:
        error, = e.args
        print("Oracle Error: ", error.message)
        return None
    finally:
        if cursor:
            cursor.close()
        

def migrate_customers(oracle_connection, mongo_db):
    print("-- Migrating customers --")

    # create the collection, if it exists remove the data 
    customers_collection = mongo_db["customers"]
    customers_collection.delete_many({})

    try:
        cursor = oracle_connection.cursor()

        query = """
            SELECT
                C.CUSTOMER_ID,
                C.FIRST_NAME,
                C.LAST_NAME,
                C.EMAIL,
                CA.ADDRESS_ID,
                ADDR.STREET_NUMBER,
                ADDR.STREET_NAME,
                ADDR.CITY,
                CO.COUNTRY_ID,
                CO.COUNTRY_NAME,
                ADS.STATUS_ID,        
                ADS.ADDRESS_STATUS    
            FROM CUSTOMER C
            LEFT JOIN CUSTOMER_ADDRESS CA ON C.CUSTOMER_ID = CA.CUSTOMER_ID
            LEFT JOIN ADDRESS ADDR ON CA.ADDRESS_ID = ADDR.ADDRESS_ID
            LEFT JOIN COUNTRY CO ON ADDR.COUNTRY_ID = CO.COUNTRY_ID
            LEFT JOIN ADDRESS_STATUS ADS ON CA.STATUS_ID = ADS.STATUS_ID 
            ORDER BY C.CUSTOMER_ID
        """

        cursor.execute(query)
        rows = cursor.fetchall()

        customers = {}

        for row in rows:
            customer_id = row[0]
            first_name = row[1]
            last_name = row[2]
            email = row[3]
            address_id = row[4]
            street_number = row[5]
            street_name = row[6]
            city = row[7]
            country_id = row[8]
            country_name = row[9]
            status_id = row[10]
            address_status = row[11]

            if customer_id not in customers:
                customers[customer_id] = {
                    "_id": customer_id,
                    "first_name": first_name,
                    "last_name": last_name,
                    "email": email,
                    "addresses": []
                }

            if address_id: 
                address_doc = {
                    "address_id": address_id,
                    "street_number": street_number,
                    "street_name": street_name,
                    "city": city,
                    "country": {
                        "country_id": country_id,
                        "country_name": country_name
                    } if country_id else None,
                    "status": {
                        "status_id": status_id,
                        "status_value": address_status
                    } if status_id else None
                }

                if address_doc not in customers[customer_id]["addresses"]:
                    customers[customer_id]["addresses"].append(address_doc)

        if customers:
            customers_to_insert = list(customers.values())
            customers_collection.insert_many(customers_to_insert)
            print(f"Inserted {len(customers_to_insert)} customers into MongoDB")
        else:
            print("No customers found to insert.")
            return None
        
    except oracledb.DatabaseError as e:
        error, = e.args
        print("Oracle Error: ", error.message)
        return None
        
    except Exception as e:
        print("Error: ", e)
        return None
    
    finally:
        if cursor:
            cursor.close()
    
def migrate_orders(oracle_connection, mongo_db):
    print("-- Migrating orders --")

    # create the collection, if it exists remove the data 
    orders_collection = mongo_db["orders"]
    orders_collection.delete_many({})

    try:
        cursor = oracle_connection.cursor()

        # como informaçoes dos livros como titulo e o isbn provalmente é para serem visualizadas secalhar metemos aqui tambem
        query = """
            SELECT
                CO.ORDER_ID,
                CO.ORDER_DATE,
                CO.CUSTOMER_ID,
                SM.METHOD_ID,
                SM.METHOD_NAME,
                SM.COST,
                DA.ADDRESS_ID AS DEST_ADDRESS_ID,
                DA.STREET_NUMBER AS DEST_STREET_NUMBER,
                DA.STREET_NAME AS DEST_STREET_NAME,
                DA.CITY AS DEST_CITY,
                DC.COUNTRY_ID AS DEST_COUNTRY_ID,
                DC.COUNTRY_NAME AS DEST_COUNTRY_NAME,
                OL.LINE_ID,
                OL.BOOK_ID,
                OL.PRICE,
                B.TITLE AS BOOK_TITLE, 
                B.ISBN13 AS BOOK_ISBN13,
                OH.HISTORY_ID,
                OH.STATUS_DATE,
                OS.STATUS_ID AS ORDER_STATUS_ID,
                OS.STATUS_VALUE AS ORDER_STATUS_VALUE
            FROM CUST_ORDER CO
            LEFT JOIN SHIPPING_METHOD SM ON CO.SHIPPING_METHOD_ID = SM.METHOD_ID
            LEFT JOIN ADDRESS DA ON CO.DEST_ADDRESS_ID = DA.ADDRESS_ID -- CORRECTED JOIN CONDITION
            LEFT JOIN COUNTRY DC ON DA.COUNTRY_ID = DC.COUNTRY_ID
            LEFT JOIN ORDER_LINE OL ON CO.ORDER_ID = OL.ORDER_ID
            LEFT JOIN BOOK B ON OL.BOOK_ID = B.BOOK_ID
            LEFT JOIN ORDER_HISTORY OH ON CO.ORDER_ID = OH.ORDER_ID
            LEFT JOIN ORDER_STATUS OS ON OH.STATUS_ID = OS.STATUS_ID
       
            ORDER BY CO.ORDER_ID, OL.LINE_ID, OH.HISTORY_ID
        """

        cursor.execute(query)
        rows = cursor.fetchall()
        orders = {}

        for row in rows:
            order_id = row[0]
            order_date = row[1]
            customer_id = row[2]
            shipping_method_id = row[3]
            shipping_method_name = row[4]
            shipping_cost = row[5]
            dest_address_id = row[6]
            dest_street_number = row[7]
            dest_street_name = row[8]
            dest_city = row[9]
            dest_country_id = row[10]
            dest_country_name = row[11]
            line_id = row[12]
            book_id = row[13]
            line_price = row[14]
            book_title = row[15]
            book_isbn13 = row[16]
            history_id = row[17]
            status_date = row[18]
            order_status_id = row[19]
            order_status_value = row[20]

            if order_id not in orders:
                orders[order_id] = {
                    "_id": order_id,
                    "order_date": order_date,
                    "customer_id": customer_id,
                    "shipping_method": {
                        "method_id": shipping_method_id,
                        "method_name": shipping_method_name,
                        "cost": shipping_cost
                    } if shipping_method_id else None,
                    "destination_address": {
                        "address_id": dest_address_id,
                        "street_number": dest_street_number,
                        "street_name": dest_street_name,
                        "city": dest_city,
                        "country": {
                            "country_id": dest_country_id,
                            "country_name": dest_country_name
                        } if dest_country_id else None
                    } if dest_address_id else None,
                    "order_lines": [],
                    "order_history": []
                }

            # add order line inf
            if line_id is not None:
                # cria um set das line_ids existentes para verificar se a linha já existe
                existing_line_ids_for_order = {ol["line_id"] for ol in orders[order_id]["order_lines"]}
                
                if line_id not in existing_line_ids_for_order:
                    order_line_doc = {
                        "line_id": line_id,
                        "book_id": book_id,
                        "title": book_title,
                        "isbn13": book_isbn13,
                        "price_at_order": line_price
                    }
                    orders[order_id]["order_lines"].append(order_line_doc)

            if history_id is not None:
                existing_history_ids_for_order = {oh["history_id"] for oh in orders[order_id]["order_history"]}
                
                if history_id not in existing_history_ids_for_order:
                    history_doc = {
                        "history_id": history_id,
                        "status_id": order_status_id,
                        "status_value": order_status_value,
                        "status_date": status_date
                    }
                    orders[order_id]["order_history"].append(history_doc)


        if orders:
            documents_to_insert = list(orders.values())
            orders_collection.insert_many(documents_to_insert)
            print(f"Migrated {len(documents_to_insert)} orders.")
        else:
            print("No orders found to migrate.")

    except oracledb.DatabaseError as e:
        error, = e.args
        print("Oracle Error: ", error.message)
        return None


    except Exception as e:
        print("Error: ", e)
        return None
    
    finally:
        if cursor:
            cursor.close()
        

def connect_to_oracle():
    try:
        connection = oracledb.connect(
            user=oracle_config["username"], 
            password=oracle_config["password"],
            dsn=oracle_config["dsn"]
        )
        print("Connected to oracle sucessfully")
        return connection
    except Exception as e:
        print("Error: ", e)
        return None
    
def connect_to_mongo():
    try:
        client = MongoClient("mongodb://localhost:27017") 
        db = client[mongo_config["db_name"]]
        print("Connected to MongoDB")
        return db
    except Exception as e:
        print("Error connecting to mongo: ", e)
        return None

def main():
    oracle_connection = connect_to_oracle()
    if not oracle_connection:
        return

    mongo_db = connect_to_mongo()
    if mongo_db is None:
        return

    migrate_books(oracle_connection, mongo_db)
    migrate_customers(oracle_connection, mongo_db)
    migrate_orders(oracle_connection, mongo_db)

    oracle_connection.close()
    print("Oracle connection closed.")

if __name__ == "__main__":
    main()
