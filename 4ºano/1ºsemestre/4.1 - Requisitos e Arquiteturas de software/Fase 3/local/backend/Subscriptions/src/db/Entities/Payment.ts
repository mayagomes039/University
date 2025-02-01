import { Entity, Column, ManyToOne, PrimaryGeneratedColumn } from "typeorm"
import { Subscription } from "./Subscription";

@Entity()
export class Payment {
    @PrimaryGeneratedColumn('uuid')
    id!: string;

    @Column({ type: 'uuid' })
    subscriptionId!: string;

    @Column({ type: 'jsonb' })
    extra!: Record<string, any>;

    @ManyToOne(() => Subscription)
    subscription!: Subscription;
}