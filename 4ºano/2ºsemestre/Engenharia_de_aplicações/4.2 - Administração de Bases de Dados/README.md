# SteamBench

## Load Data

- Download the dataset (https://storage.googleapis.com/abd25/data.zip) and extract it.

- Install the requirements:
  - With `pip`:
    ```shell
    pip3 install -r db/requirements.txt
    ```
  - With `apt`:
    ```shell
    apt install python3-psycopg2
    ```

- Install the `pgvector` extension: https://github.com/pgvector/pgvector?tab=readme-ov-file#installation

- Create a database to store the data.

- Load:
    ```shell
    # replace 'HOST', 'PORT', 'DBNAME', 'USER', and 'PASSWORD' with the
    # respective connection variables.
    python3 db/load.py --data data -H HOST -P PORT -d DBNAME -u USER -p PASSWORD
    ```


## Transactional workload (Java 21)

- Install:
```shell
cd transactional
mvn package
```

- Run:
```shell
# replace the connection, warmup, runtime, and client variables with the respective values
java -jar target/transactional-1.0-SNAPSHOT.jar -d jdbc:postgresql://HOST:PORT/DBNAME -U USER -P PASSWORD -W WARMUP -R RUNTIME -c CLIENTS
# E.g.:
java -jar target/transactional-1.0-SNAPSHOT.jar -d jdbc:postgresql://localhost:5432/steam -U postgres -P postgres -W 15 -R 180 -c 16
```


## Analytical workloads

The analytical queries can be found in the `analytical` folder.

---

## **Optimization Script**

This script is designed to run optimizations on both transactional and analytical queries stored in the `scripts` folder. It allows you to selectively apply optimizations to specific queries or all queries at once.

### **Installing Requirements**

Before running the optimization script, ensure you have the required dependencies installed:

1. **Install the Python dependencies**:

   ```shell
   pip3 install -r requirements.txt
   ```

2. **Install `psql` client** (if not already installed on your system):

   * On Ubuntu, for example:

   ```shell
   sudo apt-get install postgresql-client
   ```

### **Setup Environment Variables**

The script uses environment variables for connecting to the PostgreSQL database. Create a `.env` file with the following variables:

```env
PG_USER=your_postgres_user
PG_PASSWORD=your_postgres_password
PG_DB=your_database_name
PG_HOST=localhost
PG_PORT=5432
```

Ensure the file is in the same directory as the script, or set the environment variables manually.

### **Running the Script**

You can run the script to optimize queries in different ways:

> [!NOTE]
> You can use the argument `--help` to see all available arguments.
> 
> Example:
> ```shell
>   python3 run_script.py --help
>   ```

#### **To run optimizations for specific query categories:**

1. **Run optimizations for all analytical queries**:

   ```shell
   python3 run_script.py --optimize_analytics
   ```

2. **Run optimizations for all transactional queries**:

   ```shell
   python3 run_script.py --optimize_transactionals
   ```

3. **Run optimizations for a specific analytical query (e.g., Q1)**:

   ```shell
   python3 run_script.py --optimize_Q1
   ```

4. **Run optimizations for a specific transactional query (e.g., `gameReviews`)**:

   ```shell
   python3 run_script.py --optimize_gameReviews
   ```

#### **To run all optimizations (both analytical and transactional):**

```shell
python3 run_script.py --all_optimizations
```

### **Script Overview**

The script supports the following options:

* `--optimize_analytics`: Runs optimizations for all analytical queries (Q1, Q2, ...).
* `--optimize_transactionals`: Runs optimizations for all transactional queries (e.g., `gameReviews`, `searchGames`).
* `--optimize_<query_name>`: Runs optimizations for a specific query (e.g., `--optimize_Q1` or `--optimize_gameReviews`).
* `--all_optimizations`: Runs all optimizations, both for analytical and transactional queries.

---

## Tuning Automation Script

This script automates the process of optimizing PostgreSQL performance parameters through benchmarking. It allows for testing different parameter configurations, benchmarking PostgreSQL performance with a custom tool, and applying the best configuration based on throughput.

### Features

- Benchmarking PostgreSQL: Uses a custom Java benchmark tool to measure the throughput of the PostgreSQL instance under different configurations.

- Automatic Parameter Tuning: Tests different PostgreSQL configuration parameters, such as shared_buffers, work_mem, and others, to determine the optimal settings.

- Performance Improvement Reporting: Generates reports with before-and-after performance metrics, including throughput, response time, and abort rate.

- Configuration Management: Automatically updates PostgreSQL configuration parameters and restarts the PostgreSQL server if necessary.

- History and Logging: Saves benchmarking results and configurations to log files and history for future reference.

#### Dependencies

Install python package `python-dotenv` if you haven't before.

#### **Setup Environment Variables**

The script uses environment variables for connecting to the PostgreSQL database. Create a `.env` in the **repository root directory** file with the following variables:

```env
PG_USER=your_postgres_user
PG_PASSWORD=your_postgres_password
PG_DB=your_database_name
PG_HOST=localhost
BENCHMARK_CMD_JAR_PATH=path/to/transactional-1.0-SNAPSHOT.jar
```

#### **Running the Script**

To run the optimization process, execute the following command:

```bash
python transactional/optimize_postgres.py
```

#### What the script does:

1. **Initial Benchmark:** It runs a benchmark using the current PostgreSQL settings and logs the results.
2. **Parameter Tuning:** It then tests multiple configurations for various PostgreSQL parameters (shared_buffers, work_mem, etc.) and identifies the optimal setting for each parameter based on throughput.
3. **Final Benchmark:** After applying the optimal configurations, it runs the benchmark again to compare the performance improvement.
4. **Results Reporting:** The script saves the results in a series of text files within the `transactional/resultados_testes/` directory.

#### Output Files

The script generates several output files:

- `transactional/resultados_testes/historico/benchmark_<timestamp>_<param_value>.txt`: Contains detailed results for each benchmark run.

- `transactional/resultados_testes/postgres_optimized_config.txt`: Contains the optimized PostgreSQL configuration.

- `transactional/resultados_testes/postgresql_optimized.conf`: A configuration file suitable for directly applying the optimized parameters to PostgreSQL.

- `transactional/resultados_testes/relatorio_final.md`: A markdown report summarizing the optimization results, including before-and-after performance metrics.
#### Notes

The script doesn't reset the parameters back to default values. The user need to change them manually.