import { AppDataSource } from "../db/data-source";
import { Day } from "../db/Entities/Day";

export class DayController {

    private day = AppDataSource.getRepository(Day)

    async all() {
        return this.day.find()
    }

    async one(userId: string, day: string) {
        return this.day.findOneBy({ userId: userId, day: day })
    }

    async save(body: any) {
        return this.day.insert(body)
    }

    async update(userId: string, day: string, body: any) {
        return this.day.update({ userId: userId, day: day }, body)
    }

    async remove(userId: string, day: string) {
        return this.day.delete({ userId: userId, day: day })
    }

}