import { AppDataSource } from "../db/data-source";
import { Project } from "../db/Entities/Project";

export class ProjectController {

    private project = AppDataSource.getRepository(Project)

    async all() {
        return this.project.find()
    }

    async allProjectsByUser(userId: string) {
        return this.project.find({ where: { userId } });
    }

    async one(projectId: string) {
        return this.project.findOneBy({ id: projectId })
    }

    async oneProjectFullInformation(projectId: string) {
        return this.project.findOne({
            where: {
                id: projectId
            },
            relations: {
                images: true,
                tools: true
            }
        })
    }

    async oneByUserId(userId: string) {
        return this.project.findOneBy({ userId: userId })
    }

    async save(body: any) {
        return this.project.insert(body)
    }

    async update(projectId: string, body: any) {
        return this.project.update(projectId, body)
    }

    async remove(projectId: string) {
        return this.project.delete({ id: projectId })
    }

    async getToolByProject(projectId: string){
        return this.project.findOne({
            where: {
                id: projectId
            },
            relations: {
                tools: true
            }
        })
    }

}