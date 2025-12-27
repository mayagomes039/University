import os
import argparse
import subprocess
import logging
from dotenv import load_dotenv

# ========== LOGGING SETUP ==========
logging.basicConfig(
    level=logging.INFO,
    format='%(levelname)s - %(message)s'
)

# ========== LOAD ENV VARIABLES ==========
load_dotenv()

PG_USER = os.getenv("PG_USER")
PG_PASSWORD = os.getenv("PG_PASSWORD")
PG_DB = os.getenv("PG_DB")
PG_HOST = os.getenv("PG_HOST", "localhost")
PG_PORT = os.getenv("PG_PORT", "5432")

# ========== PATHS ==========
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
SCRIPTS_DIR = os.path.join(BASE_DIR, "scripts")


# ========== FUNCTIONS ==========
def run_sql_file(file_path):
    """
    Run a SQL script without saving the output to a file, just print results.
    """
    logging.info(f"Running SQL script: {file_path}")
    
    env = os.environ.copy()
    env["PGPASSWORD"] = PG_PASSWORD

    result = subprocess.run([
        "psql",
        "-U", PG_USER,
        "-d", PG_DB,
        "-h", PG_HOST,
        "-p", PG_PORT,
        "-f", file_path
    ], env=env, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    
    if result.returncode != 0:
        logging.error(f"Error executing: {file_path}. Return code: {result.returncode}")
    else:
        logging.info(f"Successfully executed: {file_path}")



def run_transactionals_optimizations(query=None):
    """
    Run optimization scripts for transactionals queries.
    If a query is specified, only run optimizations for that query.
    """
    transactionals_dir = os.path.join(SCRIPTS_DIR, "transactional")
    for subdir in sorted(os.listdir(transactionals_dir)):
        subdir_path = os.path.join(transactionals_dir, subdir)
        if os.path.isdir(subdir_path):
            optimization_script = os.path.join(subdir_path, f"{subdir}_optimizations.sql")
            
            if os.path.exists(optimization_script):
                if query is None or query == subdir:
                    logging.info(f"Running optimizations for {subdir}. This action may take a while...")
                    run_sql_file(optimization_script)
            else:
                logging.error(f"Optimization script not found: {optimization_script}")



def run_analytics_optimizations(query=None):
    """
    Run optimization scripts for analytics queries.
    If a query is specified, only run optimizations for that query.
    """
    analytics_dir = os.path.join(SCRIPTS_DIR, "analytical")
    for q_folder in sorted(os.listdir(analytics_dir)):
        folder_path = os.path.join(analytics_dir, q_folder)
        if os.path.isdir(folder_path):
            if query is not None and query != q_folder:
                continue  # Skip if the query doesn't match the target query
            opt_file = os.path.join(folder_path, f"{q_folder}_optimizations.sql")
            if os.path.exists(opt_file):
                logging.info(f"Running optimizations for {q_folder}. This action may take a while...")
                run_sql_file(opt_file)
            else:
                logging.error(f"Optimization script not found: {opt_file}")


# ========== MAIN ==========
def main():
    parser = argparse.ArgumentParser(description="Run SQL optimization scripts on PostgreSQL.")
    
    # Arguments for running optimizations
    parser.add_argument("--optimize_analytics", action="store_true", help="Run optimization scripts for all analytics queries.")
    parser.add_argument("--optimize_transactionals", action="store_true", help="Run optimization scripts for all transactional queries.")
    
    # For individual query optimizations
    parser.add_argument("--optimize_Q1", action="store_true", help="Run optimization for Q1 analytics query.")
    parser.add_argument("--optimize_Q2", action="store_true", help="Run optimization for Q2 analytics query.")
    parser.add_argument("--optimize_Q3", action="store_true", help="Run optimization for Q3 analytics query.")
    parser.add_argument("--optimize_Q4", action="store_true", help="Run optimization for Q4 analytics query.")
    parser.add_argument("--optimize_Q5", action="store_true", help="Run optimization for Q5 analytics query.")
    
    # Similar options for transactional queries
    parser.add_argument("--optimize_gameReviews", action="store_true", help="Run optimization for gameReviews transactional query.")
    parser.add_argument("--optimize_recentGamesPerTag", action="store_true", help="Run optimization for recentGamesPerTag transactional query.")
    parser.add_argument("--optimize_searchGames", action="store_true", help="Run optimization for searchGames transactional query.")
    parser.add_argument("--optimize_userInfo", action="store_true", help="Run optimization for userInfo transactional query.")
    
    # To run all optimizations (both analytics and transactionals)
    parser.add_argument("--all_optimizations", action="store_true", help="Run all optimization scripts (analytics + transactionals).")
    
    args = parser.parse_args()

    no_flags = not any([
        args.optimize_analytics,
        args.optimize_transactionals,
        args.optimize_Q1,
        args.optimize_Q2,
        args.optimize_Q3,
        args.optimize_Q4,
        args.optimize_Q5,
        args.optimize_gameReviews,
        args.optimize_recentGamesPerTag,
        args.optimize_searchGames,
        args.optimize_userInfo,
        args.all_optimizations
    ])

    # If no flags are provided or --all_optimizations is used
    if args.all_optimizations or no_flags:
        logging.info("Running all optimizations (analytics + transactionals)...")
        run_analytics_optimizations()
        run_transactionals_optimizations()
    else:
        if args.optimize_analytics:
            logging.info("Running optimizations for all analytics queries...")
            run_analytics_optimizations()
        if args.optimize_transactionals:
            logging.info("Running optimizations for all transactional queries...")
            run_transactionals_optimizations()

        # Running individual query optimizations for analytics
        if args.optimize_Q1:
            run_analytics_optimizations(query="Q1")
        if args.optimize_Q2:
            run_analytics_optimizations(query="Q2")
        if args.optimize_Q3:
            run_analytics_optimizations(query="Q3")
        if args.optimize_Q4:
            run_analytics_optimizations(query="Q4")
        if args.optimize_Q5:
            run_analytics_optimizations(query="Q5")

        # Running individual query optimizations for transactionals
        if args.optimize_gameReviews:
            run_transactionals_optimizations(query="gameReviews")
        if args.optimize_recentGamesPerTag:
            run_transactionals_optimizations(query="recentGamesPerTag")
        if args.optimize_searchGames:
            run_transactionals_optimizations(query="searchGames")
        if args.optimize_userInfo:
            run_transactionals_optimizations(query="userInfo")


if __name__ == "__main__":
    main()
