import { AppDataSource } from "../db/data-source";
import { Image } from "../db/Entities/Image";

export class ImageController {

    private image = AppDataSource.getRepository(Image)

    async all() {
        return this.image.find()
    }

    async one(imageId: string) {
        return this.image.findOneBy({ id: imageId })
    }

    async save(body: any) {
        return this.image.insert(body)
    }

    async update(imageId: string, body: any) {
        return this.image.update(imageId, body)
    }

    async remove(imageId: string) {
        return this.image.delete({ id: imageId })
    }

    async removeByFlag(projectId: string) {
        return this.image.delete({ projectId: projectId, isFinal: true })
    }

    async removeByProject(projectId: string) {
        return this.image.delete({ projectId: projectId })
    }

}