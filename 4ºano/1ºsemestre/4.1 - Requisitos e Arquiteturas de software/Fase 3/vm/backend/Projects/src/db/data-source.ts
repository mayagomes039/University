import "reflect-metadata"
import { DataSource } from "typeorm"
import { Project } from "./Entities/Project"
import { Image } from "./Entities/Image"
import { Tool } from "./Entities/Tool"
import { SnakeNamingStrategy } from 'typeorm-naming-strategies';

export const AppDataSource = new DataSource({
    type: "postgres",
    host:     process.env.POSTGRES_HOST      || "localhost",
    port:     parseInt(process.env.POSTGRES_PORT || "5434", 10),
    username: process.env.POSTGRES_USER  || "project",
    password: process.env.POSTGRES_PASS  || "project",
    database: process.env.POSTGRES_DB    || "project",
    synchronize: true,
    logging: false,
    entities: [Project, Image, Tool],
    migrations: [],
    subscribers: [],
    namingStrategy: new SnakeNamingStrategy(),
})