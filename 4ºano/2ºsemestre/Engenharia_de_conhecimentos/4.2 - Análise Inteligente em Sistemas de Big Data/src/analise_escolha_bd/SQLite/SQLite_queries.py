import sqlite3
import time

# Função para medir o tempo de execução das queries
def medir_tempo(query, params=None):
    conn = sqlite3.connect('crime_data.db')
    cursor = conn.cursor()
    
    start_time = time.time()
    
    if params:
        cursor.execute(query, params)
    else:
        cursor.execute(query)
    
    conn.commit()
    end_time = time.time()
    
    # Calcular o tempo decorrido
    tempo_decorrido = end_time - start_time
    conn.close()
    
    return tempo_decorrido

# Query 1: Consultar a média dos salários mínimos por estado
query1 = '''
    SELECT state_name, state_minimum_wage 
    FROM states
    JOIN minimum_wages ON states.state_id = minimum_wages.state_id;
'''
tempo_query1 = medir_tempo(query1)
print(f'Tempo da Query 1: {tempo_query1:.4f} segundos')

# Query 2: Consultar as taxas de criminalidade por estado
query2 = '''
    SELECT state_name, data_rates_property_all, data_rates_violent_all 
    FROM states
    JOIN crime_rates ON states.state_id = crime_rates.state_id;
'''
tempo_query2 = medir_tempo(query2)
print(f'Tempo da Query 2: {tempo_query2:.4f} segundos')

# Query 3: Consultar o total de crimes violentos por estado
query3 = '''
    SELECT state_name, data_totals_violent_all 
    FROM states
    JOIN crime_totals ON states.state_id = crime_totals.state_id;
'''
tempo_query3 = medir_tempo(query3)
print(f'Tempo da Query 3: {tempo_query3:.4f} segundos')

# Query 4: Consultar o índice de CPI por estado
query4 = '''
    SELECT state_name, cpi_average 
    FROM states
    JOIN cpi ON states.state_id = cpi.state_id;
'''
tempo_query4 = medir_tempo(query4)
print(f'Tempo da Query 4: {tempo_query4:.4f} segundos')

# Query 5: Consultar o nível educacional médio de homens e mulheres por estado
query5 = '''
    SELECT state_name, men_bachelors_degree, women_bachelors_degree 
    FROM states
    JOIN education_levels ON states.state_id = education_levels.state_id;
'''
tempo_query5 = medir_tempo(query5)
print(f'Tempo da Query 5: {tempo_query5:.4f} segundos')

# Query 6: Selecionar todas as linhas da tabela 'states'
query6_sqlite = 'SELECT * FROM states;'
tempo_query6_sqlite = medir_tempo(query6_sqlite)
print(f'Tempo da Query 6 (SQLite): {tempo_query6_sqlite:.4f} segundos')

# Query 6: Selecionar todas as linhas da tabela 'crime_rates'
query6_sqlite = 'SELECT * FROM crime_rates;'
tempo_query6_sqlite = medir_tempo(query6_sqlite)
print(f'Tempo da Query 6 (SQLite): {tempo_query6_sqlite:.4f} segundos')

# Query 6: Selecionar todas as linhas da tabela 'crime_totals'
query6_sqlite = 'SELECT * FROM crime_totals;'
tempo_query6_sqlite = medir_tempo(query6_sqlite)
print(f'Tempo da Query 6 (SQLite): {tempo_query6_sqlite:.4f} segundos')

# Query 6: Selecionar todas as linhas da tabela 'minimum_wages'
query6_sqlite = 'SELECT * FROM minimum_wages;'
tempo_query6_sqlite = medir_tempo(query6_sqlite)
print(f'Tempo da Query 6 (SQLite): {tempo_query6_sqlite:.4f} segundos')

# Query 6: Selecionar todas as linhas da tabela 'cpi'
query6_sqlite = 'SELECT * FROM cpi;'
tempo_query6_sqlite = medir_tempo(query6_sqlite)
print(f'Tempo da Query 6 (SQLite): {tempo_query6_sqlite:.4f} segundos')

# Query 6: Selecionar todas as linhas da tabela 'education_levels'
query6_sqlite = 'SELECT * FROM education_levels;'
tempo_query6_sqlite = medir_tempo(query6_sqlite)
print(f'Tempo da Query 6 (SQLite): {tempo_query6_sqlite:.4f} segundos')
