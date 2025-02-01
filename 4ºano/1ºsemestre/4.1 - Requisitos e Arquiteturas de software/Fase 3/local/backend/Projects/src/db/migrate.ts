import { AppDataSource } from "./data-source";

// Function to initialize and synchronize the database for Projects
export async function migrateProjectsDatabase() {
  try {
    console.log("Initializing Projects database...");

    await AppDataSource.initialize();
    console.log("Projects Data Source initialized successfully!");

    // Synchronize the database schema
    await AppDataSource.synchronize();
    console.log("Projects database schema synchronized!");
  } catch (err) {
    console.error("Projects database initialization failed:", err);
  }
}

// Call the function to migrate the Projects database
migrateProjectsDatabase();
