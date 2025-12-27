import sqlite3

conn = sqlite3.connect('crime_data.db')

conn.execute("PRAGMA foreign_keys = ON;")

cursor = conn.cursor()

# Criar as tabelas
cursor.execute('''
    CREATE TABLE IF NOT EXISTS states (
        state_id INTEGER PRIMARY KEY,
        state_name TEXT NOT NULL,
        year INTEGER NOT NULL
    );
''')

cursor.execute('''
    CREATE TABLE IF NOT EXISTS crime_rates (
        crime_rate_id INTEGER PRIMARY KEY,
        state_id INTEGER,
        data_population INTEGER,
        data_rates_property_all REAL,
        data_rates_property_burglary REAL,
        data_rates_property_larceny REAL,
        data_rates_property_motor REAL,
        data_rates_violent_all REAL,
        data_rates_violent_assault REAL,
        data_rates_violent_murder REAL,
        data_rates_violent_rape REAL,
        data_rates_violent_robbery REAL,
        FOREIGN KEY (state_id) REFERENCES states(state_id)
    );
''')

cursor.execute('''
    CREATE TABLE IF NOT EXISTS crime_totals (
        crime_total_id INTEGER PRIMARY KEY,
        state_id INTEGER,
        data_totals_property_all INTEGER,
        data_totals_property_burglary INTEGER,
        data_totals_property_larceny INTEGER,
        data_totals_property_motor INTEGER,
        data_totals_violent_all INTEGER,
        data_totals_violent_assault INTEGER,
        data_totals_violent_murder INTEGER,
        data_totals_violent_rape INTEGER,
        data_totals_violent_robbery INTEGER,
        FOREIGN KEY (state_id) REFERENCES states(state_id)
    );
''')

cursor.execute('''
    CREATE TABLE IF NOT EXISTS minimum_wages (
        wage_id INTEGER PRIMARY KEY,
        state_id INTEGER,
        state_minimum_wage REAL,
        state_minimum_wage_2020_dollars REAL,
        federal_minimum_wage REAL,
        federal_minimum_wage_2020_dollars REAL,
        effective_minimum_wage REAL,
        effective_minimum_wage_2020_dollars REAL,
        department_of_labor_cleaned_low_value REAL,
        department_of_labor_cleaned_low_value_2020_dollars REAL,
        department_of_labor_cleaned_high_value REAL,
        department_of_labor_cleaned_high_value_2020_dollars REAL,
        FOREIGN KEY (state_id) REFERENCES states(state_id)
    );
''')

cursor.execute('''
    CREATE TABLE IF NOT EXISTS cpi (
        cpi_id INTEGER PRIMARY KEY,
        state_id INTEGER,
        cpi_average REAL,
        FOREIGN KEY (state_id) REFERENCES states(state_id)
    );
''')

cursor.execute('''
               DROP TABLE IF EXISTS education_levels;
''')

cursor.execute('''
    CREATE TABLE IF NOT EXISTS education_levels (
    education_id INTEGER PRIMARY KEY AUTOINCREMENT,
    state_id INTEGER,
    less_than_hs REAL,
    high_school REAL,
    some_college REAL,
    bachelors_degree REAL,
    advanced_degree REAL,
    men_less_than_hs REAL,
    men_high_school REAL,
    men_some_college REAL,
    men_bachelors_degree REAL,
    men_advanced_degree REAL,
    women_less_than_hs REAL,
    women_high_school REAL,
    women_some_college REAL,
    women_bachelors_degree REAL,
    women_advanced_degree REAL,
    white_less_than_hs REAL,
    white_high_school REAL,
    white_some_college REAL,
    white_bachelors_degree REAL,
    white_advanced_degree REAL,
    black_less_than_hs REAL,
    black_high_school REAL,
    black_some_college REAL,
    black_bachelors_degree REAL,
    black_advanced_degree REAL,
    hispanic_less_than_hs REAL,
    hispanic_high_school REAL,
    hispanic_some_college REAL,
    hispanic_bachelors_degree REAL,
    hispanic_advanced_degree REAL,
    FOREIGN KEY (state_id) REFERENCES states(state_id)
);
''')

conn.commit()

conn.close()

print("Tabelas criadas com sucesso!")
