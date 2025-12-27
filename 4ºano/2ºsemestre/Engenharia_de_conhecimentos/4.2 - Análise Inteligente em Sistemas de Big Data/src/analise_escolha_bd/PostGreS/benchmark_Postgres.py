import psycopg2
import time

db_params = {
    'dbname': 'bigdata',
    'user': 'postgres',
    'password': 'postgres',
    'host': 'localhost',
    'port': '5432',
}

conn = psycopg2.connect(**db_params)
cursor = conn.cursor()

def benchmark_query(title, query):
    print(f"\nRunning: {title}")
    start = time.time()
    cursor.execute(query)
    cursor.fetchall()
    duration = time.time() - start
    print(f"{title} took {duration:.4f} seconds.")

# Query 1: Média dos salários mínimos por estado
query1 = """
SELECT state, AVG(state_minimum_wage) AS avg_min_wage
FROM minimum_wage
GROUP BY state;
"""

# Query 2: Taxas de criminalidade por estado
query2 = """
SELECT state, AVG(violent_all + property_all) / AVG(population) AS crime_rate
FROM crime
GROUP BY state;
"""

# Query 3: Total de crimes violentos por estado
query3 = """
SELECT state, SUM(violent_all) AS total_violent_crimes
FROM crime
GROUP BY state;
"""

# Query 4: Índice de CPI por estado
query4 = """
SELECT state, AVG(total_violent_all + total_property_all) / AVG(population) AS cpi_index
FROM crime
GROUP BY state;
"""

# Query 5: Nível educacional médio de homens e mulheres por estado
query5 = """
SELECT state,
       AVG(men_bachelors_degree) AS avg_men_degree,
       AVG(women_bachelors_degree) AS avg_women_degree
FROM education
GROUP BY state;
"""

# Query 6: Selecionar todos os dados (linha completa)
query6 = """
SELECT *
FROM crime
JOIN minimum_wage ON crime.state = minimum_wage.state AND crime.year = minimum_wage.year
JOIN education ON crime.state = education.state AND crime.year = education.year;
"""

benchmark_query("Query 1: ", query1)
benchmark_query("Query 2: ", query2)
benchmark_query("Query 3: ", query3)
benchmark_query("Query 4: ", query4)
benchmark_query("Query 5: ", query5)
benchmark_query("Query 6: ", query6)

cursor.close()
conn.close()
