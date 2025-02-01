import { AppDataSource } from "./data-source";

// Function to initialize and synchronize the database for Users
export async function migrateUsersDatabase() {
  try {
    console.log("Initializing Users database...");

    await AppDataSource.initialize();
    console.log("Users Data Source initialized successfully!");

    // Synchronize the database schema
    await AppDataSource.synchronize();
    console.log("Users database schema synchronized!");
  } catch (err) {
    console.error("Users database initialization failed:", err);
  }
}

// Call the function to migrate the Users database
migrateUsersDatabase();
