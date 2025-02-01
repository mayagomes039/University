import { Entity, Column, OneToMany, PrimaryGeneratedColumn, ManyToOne } from "typeorm"
import { Project } from "./Project";

@Entity()
export class Image {
    @PrimaryGeneratedColumn('uuid')
    id!: string;

    @Column({ type: 'uuid' })
    projectId!: string;

    @Column({ type: 'text' })
    uri!: string;

    @Column({ type: 'boolean', default: false })
    isFinal!: boolean;

    @ManyToOne(() => Project)
    project!: Project;
    
}