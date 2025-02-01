import { AppDataSource } from "../db/data-source";
import { User } from "../db/Entities/User";

export class UserController {

    private user = AppDataSource.getRepository(User)

    async all() {
        return this.user.find()
    }

    async one(userId: string) {
        return this.user.findOneBy({ id: userId })
    }

    async oneByEmail(email: string) {
        return this.user.findOneBy({ email: email })
    }

    async save(body: any) {
        return this.user.insert(body)
    }

    async update(userId: string, body: any) {
        return this.user.update(userId, body)
    }

    async remove(userId: string) {
        return this.user.delete({ id: userId })
    }

}