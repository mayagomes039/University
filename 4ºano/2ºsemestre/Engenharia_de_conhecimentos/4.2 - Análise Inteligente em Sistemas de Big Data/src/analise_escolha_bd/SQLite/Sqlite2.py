import csv
import sqlite3
import time  

# Função para conectar ao SQLite
def connect_to_sqlite(db_name):
    conn = sqlite3.connect(db_name)
    conn.execute("PRAGMA foreign_keys = ON;")
    return conn

def load_states(cursor, row):
    cursor.execute('''
        INSERT INTO states (state_name, year)
        VALUES (?, ?)
    ''', (row['State'], row['year']))
    return cursor.lastrowid  

def load_crime_rates(cursor, row, state_id):
    cursor.execute('''
        INSERT INTO crime_rates (state_id, data_population, data_rates_property_all, 
                                 data_rates_property_burglary, data_rates_property_larceny, 
                                 data_rates_property_motor, data_rates_violent_all, 
                                 data_rates_violent_assault, data_rates_violent_murder, 
                                 data_rates_violent_rape, data_rates_violent_robbery)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    ''', (state_id,
          float(row['Data.Population']) if row['Data.Population'].replace('.', '', 1).isdigit() else None,
          float(row['Data.Rates.Property.All']) if row['Data.Rates.Property.All'].replace('.', '', 1).isdigit() else None,
          float(row['Data.Rates.Property.Burglary']) if row['Data.Rates.Property.Burglary'].replace('.', '', 1).isdigit() else None,
          float(row['Data.Rates.Property.Larceny']) if row['Data.Rates.Property.Larceny'].replace('.', '', 1).isdigit() else None,
          float(row['Data.Rates.Property.Motor']) if row['Data.Rates.Property.Motor'].replace('.', '', 1).isdigit() else None,
          float(row['Data.Rates.Violent.All']) if row['Data.Rates.Violent.All'].replace('.', '', 1).isdigit() else None,
          float(row['Data.Rates.Violent.Assault']) if row['Data.Rates.Violent.Assault'].replace('.', '', 1).isdigit() else None,
          float(row['Data.Rates.Violent.Murder']) if row['Data.Rates.Violent.Murder'].replace('.', '', 1).isdigit() else None,
          float(row['Data.Rates.Violent.Rape']) if row['Data.Rates.Violent.Rape'].replace('.', '', 1).isdigit() else None,
          float(row['Data.Rates.Violent.Robbery']) if row['Data.Rates.Violent.Robbery'].replace('.', '', 1).isdigit() else None))

def load_crime_totals(cursor, row, state_id):
    cursor.execute('''
        INSERT INTO crime_totals (state_id, data_totals_property_all, data_totals_property_burglary, 
                                  data_totals_property_larceny, data_totals_property_motor, 
                                  data_totals_violent_all, data_totals_violent_assault, data_totals_violent_murder, 
                                  data_totals_violent_rape, data_totals_violent_robbery)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    ''', (state_id,
          int(row['Data.Totals.Property.All']) if row['Data.Totals.Property.All'].isdigit() else None,
          int(row['Data.Totals.Property.Burglary']) if row['Data.Totals.Property.Burglary'].isdigit() else None,
          int(row['Data.Totals.Property.Larceny']) if row['Data.Totals.Property.Larceny'].isdigit() else None,
          int(row['Data.Totals.Property.Motor']) if row['Data.Totals.Property.Motor'].isdigit() else None,
          int(row['Data.Totals.Violent.All']) if row['Data.Totals.Violent.All'].isdigit() else None,
          int(row['Data.Totals.Violent.Assault']) if row['Data.Totals.Violent.Assault'].isdigit() else None,
          int(row['Data.Totals.Violent.Murder']) if row['Data.Totals.Violent.Murder'].isdigit() else None,
          int(row['Data.Totals.Violent.Rape']) if row['Data.Totals.Violent.Rape'].isdigit() else None,
          int(row['Data.Totals.Violent.Robbery']) if row['Data.Totals.Violent.Robbery'].isdigit() else None))

def load_minimum_wages(cursor, row, state_id):
    cursor.execute('''
        INSERT INTO minimum_wages (state_id, state_minimum_wage, state_minimum_wage_2020_dollars, 
                                   federal_minimum_wage, federal_minimum_wage_2020_dollars, 
                                   effective_minimum_wage, effective_minimum_wage_2020_dollars, 
                                   department_of_labor_cleaned_low_value, department_of_labor_cleaned_low_value_2020_dollars, 
                                   department_of_labor_cleaned_high_value, department_of_labor_cleaned_high_value_2020_dollars)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    ''', (state_id,
          float(row['State.Minimum.Wage']) if row['State.Minimum.Wage'].replace('.', '', 1).isdigit() else None,
          float(row['State.Minimum.Wage.2020.Dollars']) if row['State.Minimum.Wage.2020.Dollars'].replace('.', '', 1).isdigit() else None,
          float(row['Federal.Minimum.Wage']) if row['Federal.Minimum.Wage'].replace('.', '', 1).isdigit() else None,
          float(row['Federal.Minimum.Wage.2020.Dollars']) if row['Federal.Minimum.Wage.2020.Dollars'].replace('.', '', 1).isdigit() else None,
          float(row['Effective.Minimum.Wage']) if row['Effective.Minimum.Wage'].replace('.', '', 1).isdigit() else None,
          float(row['Effective.Minimum.Wage.2020.Dollars']) if row['Effective.Minimum.Wage.2020.Dollars'].replace('.', '', 1).isdigit() else None,
          float(row['Department.Of.Labor.Cleaned.Low.Value']) if row['Department.Of.Labor.Cleaned.Low.Value'].replace('.', '', 1).isdigit() else None,
          float(row['Department.Of.Labor.Cleaned.Low.Value.2020.Dollars']) if row['Department.Of.Labor.Cleaned.Low.Value.2020.Dollars'].replace('.', '', 1).isdigit() else None,
          float(row['Department.Of.Labor.Cleaned.High.Value']) if row['Department.Of.Labor.Cleaned.High.Value'].replace('.', '', 1).isdigit() else None,
          float(row['Department.Of.Labor.Cleaned.High.Value.2020.Dollars']) if row['Department.Of.Labor.Cleaned.High.Value.2020.Dollars'].replace('.', '', 1).isdigit() else None))

def load_cpi(cursor, row, state_id):
    cursor.execute('''
        INSERT INTO cpi (state_id, cpi_average)
        VALUES (?, ?)
    ''', (state_id,
          float(row['CPI.Average']) if row['CPI.Average'].replace('.', '', 1).isdigit() else None))


def load_education_levels(cursor, row, state_id):
    values = (
        state_id,
        float(row['less_than_hs']) if row.get('less_than_hs') and row['less_than_hs'].replace('.', '', 1).isdigit() else None,
        float(row['high_school']) if row.get('high_school') and row['high_school'].replace('.', '', 1).isdigit() else None,
        float(row['some_college']) if row.get('some_college') and row['some_college'].replace('.', '', 1).isdigit() else None,
        float(row['bachelors_degree']) if row.get('bachelors_degree') and row['bachelors_degree'].replace('.', '', 1).isdigit() else None,
        float(row['advanced_degree']) if row.get('advanced_degree') and row['advanced_degree'].replace('.', '', 1).isdigit() else None,
        
        float(row['men_less_than_hs']) if row.get('men_less_than_hs') and row['men_less_than_hs'].replace('.', '', 1).isdigit() else None,
        float(row['men_high_school']) if row.get('men_high_school') and row['men_high_school'].replace('.', '', 1).isdigit() else None,
        float(row['men_some_college']) if row.get('men_some_college') and row['men_some_college'].replace('.', '', 1).isdigit() else None,
        float(row['men_bachelors_degree']) if row.get('men_bachelors_degree') and row['men_bachelors_degree'].replace('.', '', 1).isdigit() else None,
        float(row['men_advanced_degree']) if row.get('men_advanced_degree') and row['men_advanced_degree'].replace('.', '', 1).isdigit() else None,
        
        float(row['women_less_than_hs']) if row.get('women_less_than_hs') and row['women_less_than_hs'].replace('.', '', 1).isdigit() else None,
        float(row['women_high_school']) if row.get('women_high_school') and row['women_high_school'].replace('.', '', 1).isdigit() else None,
        float(row['women_some_college']) if row.get('women_some_college') and row['women_some_college'].replace('.', '', 1).isdigit() else None,
        float(row['women_bachelors_degree']) if row.get('women_bachelors_degree') and row['women_bachelors_degree'].replace('.', '', 1).isdigit() else None,
        float(row['women_advanced_degree']) if row.get('women_advanced_degree') and row['women_advanced_degree'].replace('.', '', 1).isdigit() else None,
        
        float(row['white_less_than_hs']) if row.get('white_less_than_hs') and row['white_less_than_hs'].replace('.', '', 1).isdigit() else None,
        float(row['white_high_school']) if row.get('white_high_school') and row['white_high_school'].replace('.', '', 1).isdigit() else None,
        float(row['white_some_college']) if row.get('white_some_college') and row['white_some_college'].replace('.', '', 1).isdigit() else None,
        float(row['white_bachelors_degree']) if row.get('white_bachelors_degree') and row['white_bachelors_degree'].replace('.', '', 1).isdigit() else None,
        float(row['white_advanced_degree']) if row.get('white_advanced_degree') and row['white_advanced_degree'].replace('.', '', 1).isdigit() else None,
        
        float(row['black_less_than_hs']) if row.get('black_less_than_hs') and row['black_less_than_hs'].replace('.', '', 1).isdigit() else None,
        float(row['black_high_school']) if row.get('black_high_school') and row['black_high_school'].replace('.', '', 1).isdigit() else None,
        float(row['black_some_college']) if row.get('black_some_college') and row['black_some_college'].replace('.', '', 1).isdigit() else None,
        float(row['black_bachelors_degree']) if row.get('black_bachelors_degree') and row['black_bachelors_degree'].replace('.', '', 1).isdigit() else None,
        float(row['black_advanced_degree']) if row.get('black_advanced_degree') and row['black_advanced_degree'].replace('.', '', 1).isdigit() else None,
        
        float(row['hispanic_less_than_hs']) if row.get('hispanic_less_than_hs') and row['hispanic_less_than_hs'].replace('.', '', 1).isdigit() else None,
        float(row['hispanic_high_school']) if row.get('hispanic_high_school') and row['hispanic_high_school'].replace('.', '', 1).isdigit() else None,
        float(row['hispanic_some_college']) if row.get('hispanic_some_college') and row['hispanic_some_college'].replace('.', '', 1).isdigit() else None,
        float(row['hispanic_bachelors_degree']) if row.get('hispanic_bachelors_degree') and row['hispanic_bachelors_degree'].replace('.', '', 1).isdigit() else None,
        float(row['hispanic_advanced_degree']) if row.get('hispanic_advanced_degree') and row['hispanic_advanced_degree'].replace('.', '', 1).isdigit() else None
    )

    print(f"Valores para inserção: {values}")

    if len(values) != 31:
        print(f"ERRO: Esperados 31 valores, mas temos {len(values)} valores.")
        return

    query = '''
        INSERT INTO education_levels (
            state_id,
            less_than_hs, high_school, some_college, bachelors_degree, advanced_degree,
            men_less_than_hs, men_high_school, men_some_college, men_bachelors_degree, men_advanced_degree,
            women_less_than_hs, women_high_school, women_some_college, women_bachelors_degree, women_advanced_degree,
            white_less_than_hs, white_high_school, white_some_college, white_bachelors_degree, white_advanced_degree,
            black_less_than_hs, black_high_school, black_some_college, black_bachelors_degree, black_advanced_degree,
            hispanic_less_than_hs, hispanic_high_school, hispanic_some_college, hispanic_bachelors_degree, hispanic_advanced_degree
        ) VALUES (?,?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    '''
    print(f"Query: {query}")
    cursor.execute(query, values)

def load_data_to_sqlite(csv_file, conn):
    start_time = time.time()  # Marca o início do tempo
    cursor = conn.cursor()
    
    with open(csv_file, newline='', encoding='utf-8') as csvfile:
        reader = csv.DictReader(csvfile)
        
        for row in reader:
            state_id = load_states(cursor, row)
            
            load_crime_rates(cursor, row, state_id)
            load_crime_totals(cursor, row, state_id)
            load_minimum_wages(cursor, row, state_id)
            load_cpi(cursor, row, state_id)
            load_education_levels(cursor, row, state_id)
            
            print(f"Dados inseridos para o estado: {row['State']}, {row['year']}")
    
    conn.commit() 
    end_time = time.time()  # Marca o tempo final
    elapsed_time = end_time - start_time  # Calcula o tempo total de execução
    print(f"Tempo total para inserir os dados no SQLite: {elapsed_time:.2f} segundos")

if __name__ == "__main__":
    csv_file = "../datasets/merged_data_2.csv"  
    db_name = "crime_data.db"  

    conn = connect_to_sqlite(db_name)

    load_data_to_sqlite(csv_file, conn)

    conn.close()  

    print("Dados inseridos no SQLite com sucesso!")
