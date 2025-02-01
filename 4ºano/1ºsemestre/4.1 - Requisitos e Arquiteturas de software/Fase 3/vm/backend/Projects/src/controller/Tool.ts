import { AppDataSource } from "../db/data-source";
import { Tool } from "../db/Entities/Tool";

export class ToolController {

    private tool = AppDataSource.getRepository(Tool)

    async all() {
        return this.tool.find()
    }

    async one(toolId: string) {
        return this.tool.findOneBy({ id: toolId })
    }

    async oneWherePosition(projectId: string, position: number) {
        return this.tool.findOneBy({ projectId: projectId, position: position })
    }

    async save(body: any) {
        return this.tool.insert(body)
    }

    async update(toolId: string, body: any) {
        return this.tool.update(toolId, body)
    }

    async remove(toolId: string) {
        return this.tool.delete({ id: toolId })
    }

    async removeByProject(projectId: string) {
        return this.tool.delete({ projectId: projectId })
    }

}