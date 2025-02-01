import { Entity, Column, Check, OneToMany, PrimaryGeneratedColumn } from "typeorm"
import { UserType } from "../types";
import { Day } from "./Day";

@Entity()
@Check(`"type" IN ('free', 'premium', 'anonymous')`)
export class User {
    @PrimaryGeneratedColumn('uuid')
    id!: string;

    @Column({ type: 'varchar', length: 200 })
    name!: string;

    @Column({ type: 'citext', unique: true, nullable: true })
    email!: string | null;

    @Column({ type: 'varchar', nullable: true, length: 255 })
    passwordHash!: string | null;

    @Column({ type: 'varchar' })
    type!: UserType;

    @OneToMany(() => Day, day => day.user)
    days!: Day[];
}