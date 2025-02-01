import { Entity, Column, PrimaryColumn, Check, ManyToOne, PrimaryGeneratedColumn } from "typeorm"
import { User } from "./User";

@Entity()
export class Day {
    @PrimaryColumn({ type: 'uuid' })
    userId!: string;
  
    @PrimaryColumn({ type: 'date' })
    day!: string;

    @Column({ type: 'int' })
    processed!: number;

    @ManyToOne(() => User)
    user!: User;

}