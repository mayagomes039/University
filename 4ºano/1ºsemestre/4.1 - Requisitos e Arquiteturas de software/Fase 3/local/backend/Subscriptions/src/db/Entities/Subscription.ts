import { Entity, Column, Check, OneToMany, PrimaryGeneratedColumn } from "typeorm"
import { SubscriptionState, SubscriptionType } from "../types";
import { Payment } from "./Payment";

@Entity()
@Check(`"type" IN ('monthly', 'annual')`)
@Check(`"state" IN ('active', 'inactive')`)
export class Subscription {
    @PrimaryGeneratedColumn('uuid')
    id!: string;

    @Column({ type: 'uuid' })
    userId!: string;

    @Column({ type: 'varchar' })
    type!: SubscriptionType;

    @Column({ type: 'varchar', default: 'active' })
    state!: SubscriptionState;

    @Column({ type: 'timestamptz' })
    insertedAt!: Date;
    
    @OneToMany(() => Payment, payment => payment.subscription)
    payments!: Payment[];
}