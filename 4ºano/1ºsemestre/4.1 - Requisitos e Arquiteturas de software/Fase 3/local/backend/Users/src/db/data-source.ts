import "reflect-metadata"
import { DataSource } from "typeorm"
import { User } from "./Entities/User"
import { Day } from "./Entities/Day"
import { SnakeNamingStrategy } from 'typeorm-naming-strategies';

export const AppDataSource = new DataSource({
    type: "postgres",
    host:     process.env.POSTGRES_HOST      || "localhost",
    port:     parseInt(process.env.POSTGRES_PORT  || "5433", 10),
    username: process.env.POSTGRES_USER  || "user",
    password: process.env.POSTGRES_PASS  || "user",
    database: process.env.POSTGRES_DB    || "user",
    synchronize: true,
    logging: false,
    entities: [User, Day],
    migrations: [],
    subscribers: [],
    namingStrategy: new SnakeNamingStrategy(),
})