import "reflect-metadata"
import { DataSource } from "typeorm"
import { Subscription } from "./Entities/Subscription"
import { Payment } from "./Entities/Payment"
import { SnakeNamingStrategy } from 'typeorm-naming-strategies';

export const AppDataSource = new DataSource({
    type: "postgres",
    host:     process.env.POSTGRES_HOST      || "localhost",
    port:     parseInt(process.env.POSTGRES_PORT || "5435", 10),
    username: process.env.POSTGRES_USER  || "subscription",
    password: process.env.POSTGRES_PASS  || "subscription",
    database: process.env.POSTGRES_DB    || "subscription",
    synchronize: true,
    logging: false,
    entities: [Subscription, Payment],
    migrations: [],
    subscribers: [],
    namingStrategy: new SnakeNamingStrategy(),
})