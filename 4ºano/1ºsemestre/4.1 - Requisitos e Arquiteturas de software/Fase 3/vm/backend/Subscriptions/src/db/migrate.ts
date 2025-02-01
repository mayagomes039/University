import { AppDataSource } from "./data-source"; 

// Function to initialize and synchronize the database for Subscriptions
export async function migrateSubscriptionsDatabase() {
  try {
    console.log("Initializing Subscriptions database...");

    await AppDataSource.initialize();
    console.log("Subscriptions Data Source initialized successfully!");

    // Synchronize the database schema
    await AppDataSource.synchronize();
    console.log("Subscriptions database schema synchronized!");
  } catch (err) {
    console.error("Subscriptions database initialization failed:", err);
  }
}

// Call the function to migrate Subscriptions database
migrateSubscriptionsDatabase();
