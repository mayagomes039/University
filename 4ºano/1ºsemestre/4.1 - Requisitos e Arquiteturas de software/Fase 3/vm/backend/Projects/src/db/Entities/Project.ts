import { Entity, Column, PrimaryGeneratedColumn, OneToMany } from "typeorm"
import { Image } from "./Image";
import { Tool } from "./Tool";

@Entity()
export class Project {
    @PrimaryGeneratedColumn('uuid')
    id!: string;

    @Column({ type: 'varchar', length: 200 })
    name!: string; 

    @Column({ type: 'uuid' })
    userId!: string;

    @OneToMany(() => Image, image => image.project)
    images!: Image[];

    @OneToMany(() => Tool, tool => tool.project)
    tools!: Tool[];
}