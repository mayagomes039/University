import { Entity, Column, PrimaryGeneratedColumn, ManyToOne, Check } from "typeorm"
import { Project } from "./Project";
import { ToolProcedure } from "../types";

@Entity()
@Check(`"procedure" IN ('crop', 'scale', 'border', 'brightness', 'contrast', 'rotate', 'rotate', 'autocrop', 'extracttext', 'objectrecognition', 'peoplecount', 'watermark', 'removebg')`)
export class Tool {
    @PrimaryGeneratedColumn('uuid')
    id!: string;

    @Column({ type: 'int' })
    position!: number; 

    @Column({ type: 'varchar', length: 200 })
    procedure!: ToolProcedure;

    @Column({ type: 'jsonb' })
    parameters!: Record<string, any>;

    @Column({ type: 'uuid' })
    projectId!: string;
    
    @ManyToOne(() => Project)
    project!: Project;
}