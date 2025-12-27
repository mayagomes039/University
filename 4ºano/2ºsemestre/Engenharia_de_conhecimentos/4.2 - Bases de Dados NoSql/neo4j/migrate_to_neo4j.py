from neo4j import GraphDatabase
import oracledb


connection = oracledb.connect(user="bookstore", password="bookstore", dsn="localhost/ORCLPDB1")

driver = GraphDatabase.driver("bolt://localhost:7687")

cursor = connection.cursor()

driver.execute_query(
    """
    CREATE DATABASE bookstore IF NOT EXISTS
    """,
)

driver.execute_query(
    """
    MATCH (n) DETACH DELETE n
    """,
    database_="bookstore",
)

constraints = [
    "CREATE CONSTRAINT IF NOT EXISTS FOR (a:Author) REQUIRE a.author_id IS UNIQUE",
    "CREATE CONSTRAINT IF NOT EXISTS FOR (p:Publisher) REQUIRE p.publisher_id IS UNIQUE",
    "CREATE CONSTRAINT IF NOT EXISTS FOR (l:Language) REQUIRE l.language_id IS UNIQUE",
    "CREATE CONSTRAINT IF NOT EXISTS FOR (b:Book) REQUIRE b.book_id IS UNIQUE",
    "CREATE CONSTRAINT IF NOT EXISTS FOR (s:ShippingMethod) REQUIRE s.method_id IS UNIQUE",
    "CREATE CONSTRAINT IF NOT EXISTS FOR (c:Country) REQUIRE c.country_id IS UNIQUE",
    "CREATE CONSTRAINT IF NOT EXISTS FOR (addr:Address) REQUIRE addr.address_id IS UNIQUE", # Changed alias for clarity
    "CREATE CONSTRAINT IF NOT EXISTS FOR (cust:Customer) REQUIRE cust.customer_id IS UNIQUE", # Changed alias for clarity
    "CREATE CONSTRAINT IF NOT EXISTS FOR (o:Order) REQUIRE o.order_id IS UNIQUE",
    "CREATE CONSTRAINT IF NOT EXISTS FOR (h:OrderHistory) REQUIRE h.history_id IS UNIQUE",
]

for constraint_query in constraints:
    try:
        driver.execute_query(constraint_query, database_="bookstore")
        print(f"Successfully created constraint: {constraint_query}")
    except Exception as e:
        print(f"Error creating constraint: {constraint_query}")
        print(e)



cursor.execute("""
    SELECT * FROM AUTHOR
    """)

authors = cursor.fetchall()

def create_authors(author_id, author_name):
    summary = driver.execute_query(
        """
        MERGE (a:Author {author_id: $author_id})
        SET a.author_name = $author_name        
        """,
        author_id=author_id,
        author_name=author_name,
        database_="bookstore",
    ).summary
    print(f"Created {summary.counters.nodes_created} nodes and {summary.counters.relationships_created} relationships.")

for author in authors:
    create_authors(author[0], author[1])

cursor.execute("""
    SELECT * FROM PUBLISHER
    """)

publishers = cursor.fetchall()


def create_publishers(publisher_id, publisher_name):
    summary = driver.execute_query(
        """
        MERGE (p:Publisher {publisher_id: $publisher_id})
        SET p.publisher_name = $publisher_name        
        """,
        publisher_id=publisher_id,
        publisher_name=publisher_name,
        database_="bookstore",
    ).summary
    print(f"Created {summary.counters.nodes_created} nodes and {summary.counters.relationships_created} relationships.")

for publisher in publishers:
    create_publishers(publisher[0], publisher[1])

cursor.execute("""
    SELECT * FROM BOOK_LANGUAGE
    """)

languages = cursor.fetchall()

def create_languages(language_id, language_code, language_name):
    summary = driver.execute_query(
        """
        MERGE (l:Language {language_id: $language_id})
        SET l.language_code = $language_code,
            l.language_name = $language_name        
        """,
        language_id=language_id,
        language_code=language_code,
        language_name=language_name,
        database_="bookstore",
    ).summary
    print(f"Created {summary.counters.nodes_created} nodes and {summary.counters.relationships_created} relationships.")

for language in languages:
    create_languages(language[0], language[1], language[2])

cursor.execute("""
    SELECT * FROM BOOK
    JOIN BOOK_LANGUAGE ON BOOK.LANGUAGE_ID = BOOK_LANGUAGE.LANGUAGE_ID
    JOIN PUBLISHER ON BOOK.PUBLISHER_ID = PUBLISHER.PUBLISHER_ID
    """)

books = cursor.fetchall()

def create_books(book_id, title, isbn13, language_id, num_pages, publication_date, publisher_id):
    summary = driver.execute_query(
        """
        MERGE (b:Book {book_id: $book_id})
        SET b.title = $title,
            b.isbn13 = $isbn13,
            b.num_pages = $num_pages,
            b.publication_date = $publication_date
        WITH b
        MATCH (l:Language {language_id: $language_id})
        CREATE (b)-[:IN_LANGUAGE]->(l)
        WITH b
        MATCH (p:Publisher {publisher_id: $publisher_id})
        CREATE (b)-[:PUBLISHED_BY]->(p)
        """,
        book_id=book_id,
        title=title,
        isbn13=isbn13,
        num_pages=num_pages,
        publication_date=publication_date,
        language_id=language_id,
        publisher_id=publisher_id,
        database_="bookstore",
    ).summary
    print(f"Created {summary.counters.nodes_created} nodes and {summary.counters.relationships_created} relationships.")


cursor.execute("""
    SELECT * FROM BOOK_AUTHOR
    """)

book_authors = cursor.fetchall()

def create_book_authors(book_id, author_id):
    summary = driver.execute_query(
        """
        MATCH (b:Book {book_id: $book_id})
        MATCH (a:Author {author_id: $author_id})
        CREATE (b)-[:WRITTEN_BY]->(a)
        """,
        book_id=book_id,
        author_id=author_id,
        database_="bookstore",
    ).summary
    print(f"Created {summary.counters.nodes_created} nodes and {summary.counters.relationships_created} relationships.")

for book in books:
    create_books(book[0], book[1], book[2], book[3], book[4], book[5], book[6])

for book_author in book_authors:
    create_book_authors(book_author[0], book_author[1])



cursor.execute("""
    SELECT * FROM SHIPPING_METHOD
    """)

shipping_methods = cursor.fetchall()

def create_shipping_methods(method_id, method_name, cost):
    summary = driver.execute_query(
        """
        MERGE (s:ShippingMethod {method_id: $method_id})
        SET s.method_name = $method_name,
            s.cost = $cost        
        """,
        method_id=method_id,
        method_name=method_name,
        cost=cost,
        database_="bookstore",
    ).summary
    print(f"Created {summary.counters.nodes_created} nodes and {summary.counters.relationships_created} relationships.")

for shipping_method in shipping_methods:
    create_shipping_methods(shipping_method[0], shipping_method[1], shipping_method[2])

cursor.execute("""
    SELECT * FROM COUNTRY
    """)

countries = cursor.fetchall()

def create_countries(country_id, country_name):
    summary = driver.execute_query(
        """
        MERGE (c:Country {country_id: $country_id})
        SET c.country_name = $country_name        
        """,
        country_id=country_id,
        country_name=country_name,
        database_="bookstore",
    ).summary
    print(f"Created {summary.counters.nodes_created} nodes and {summary.counters.relationships_created} relationships.")

for country in countries:
    create_countries(country[0], country[1])

cursor.execute("""
    SELECT * FROM ADDRESS
    """)

addresses = cursor.fetchall()

def create_addresses(address_id, street_number, street_name, city, country_id):
    summary = driver.execute_query(
        """
        MERGE (a:Address {address_id: $address_id})
        SET a.street_number = $street_number,
            a.street_name = $street_name,
            a.city = $city
        WITH a
        MATCH (c:Country {country_id: $country_id})
        CREATE (a)-[:LOCATED_IN]->(c)
        """,
        address_id=address_id,
        street_number=street_number,
        street_name=street_name,
        city=city,
        country_id=country_id,
        database_="bookstore",
    ).summary
    print(f"Created {summary.counters.nodes_created} nodes and {summary.counters.relationships_created} relationships.")

for address in addresses:
    create_addresses(address[0], address[1], address[2], address[3], address[4])

cursor.execute("""
    SELECT * FROM CUSTOMER
    """)

customers = cursor.fetchall()

def create_customers(customer_id, first_name, last_name, email):
    summary = driver.execute_query(
        """
        MERGE (c:Customer {customer_id: $customer_id})
        SET c.first_name = $first_name,
            c.last_name = $last_name,
            c.email = $email        
        """,
        customer_id=customer_id,
        first_name=first_name,
        last_name=last_name,
        email=email,
        database_="bookstore",
    ).summary
    print(f"Created {summary.counters.nodes_created} nodes and {summary.counters.relationships_created} relationships.")

for customer in customers:
    create_customers(customer[0], customer[1], customer[2], customer[3])

cursor.execute("""
    SELECT * FROM CUSTOMER_ADDRESS
    JOIN ADDRESS_STATUS ON CUSTOMER_ADDRESS.STATUS_ID = ADDRESS_STATUS.STATUS_ID
    """)

customer_addresses = cursor.fetchall()

def create_customer_addresses(customer_id, address_id, address_status):
    summary = driver.execute_query(
        """
        MATCH (c:Customer {customer_id: $customer_id})
        MATCH (a:Address {address_id: $address_id})
        CREATE (c)-[:LIVES_AT {status: $address_status}]->(a)
        """,
        customer_id=customer_id,
        address_id=address_id,
        address_status=address_status,
        database_="bookstore",
    ).summary
    print(f"Created {summary.counters.nodes_created} nodes and {summary.counters.relationships_created} relationships.")

for customer_address in customer_addresses:
    create_customer_addresses(customer_address[0], customer_address[1], customer_address[4])



cursor.execute("""
    SELECT * FROM CUST_ORDER
""")

order = cursor.fetchall()


def create_orders(order_id, order_date, customer_id, shipping_method_id, dest_address_id):
    summary = driver.execute_query(
        """
        MERGE (o:Order {order_id: $order_id})
        SET o.order_date = $order_date
        WITH o
        MATCH (c:Customer {customer_id: $customer_id})
        CREATE (o)-[:PLACED_BY]->(c)
        WITH o
        MATCH (s:ShippingMethod {method_id: $shipping_method_id})
        CREATE (o)-[:SHIPPED_VIA]->(s)
        WITH o
        MATCH (a:Address {address_id: $dest_address_id})
        CREATE (o)-[:DELIVERED_TO]->(a)
        """,
        order_id=order_id,
        order_date=order_date,
        customer_id=customer_id,
        shipping_method_id=shipping_method_id,
        dest_address_id=dest_address_id,
        database_="bookstore",
    ).summary
    print(f"Created {summary.counters.nodes_created} nodes and {summary.counters.relationships_created} relationships.")

for order in order:
    create_orders(order[0], order[1], order[2], order[3], order[4])

cursor.execute("""
    SELECT * FROM ORDER_LINE
""")

order_lines = cursor.fetchall()

def create_order_lines(line_id, order_id, book_id, price):
    summary = driver.execute_query(
        """
        MATCH (o:Order {order_id: $order_id})
        MATCH (b:Book {book_id: $book_id})
        CREATE (o)-[:CONTAINS {price: $price}]->(b)
        """,
        line_id=line_id,
        order_id=order_id,
        book_id=book_id,
        price=price,
        database_="bookstore",
    ).summary
    print(f"Created {summary.counters.nodes_created} nodes and {summary.counters.relationships_created} relationships.")

for order_line in order_lines:
    create_order_lines(order_line[0], order_line[1], order_line[2], order_line[3])


cursor.execute("""
    SELECT * FROM ORDER_HISTORY
    JOIN ORDER_STATUS ON ORDER_HISTORY.STATUS_ID = ORDER_STATUS.STATUS_ID
""")

order_history = cursor.fetchall()

print(order_history[0])

def create_order_history(history_id, order_id, status_date, status_value):
    summary = driver.execute_query(
        """
        MERGE (h:OrderHistory {history_id: $history_id})
        SET h.status_date = $status_date,
            h.status_value = $status_value
        WITH h
        MATCH (o:Order {order_id: $order_id})
        CREATE (h)-[:FOR_ORDER]->(o)
        """,
        history_id=history_id,
        order_id=order_id,
        status_date=status_date,
        status_value=status_value,
        database_="bookstore",
    ).summary
    print(f"Created {summary.counters.nodes_created} nodes and {summary.counters.relationships_created} relationships.")


for order_history in order_history:
    create_order_history(order_history[0], order_history[1], order_history[3], order_history[5])