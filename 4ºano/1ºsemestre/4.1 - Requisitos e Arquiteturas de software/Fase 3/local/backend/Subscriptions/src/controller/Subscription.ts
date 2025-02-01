import { AppDataSource } from "../db/data-source";
import { Subscription } from "../db/Entities/Subscription";
import { SubscriptionState } from "../db/types";

export class SubscriptionController {

    private subscription = AppDataSource.getRepository(Subscription)

    async all() {
        return this.subscription.find()
    }

    async one(subscriptionId: string) {
        return this.subscription.findOneBy({ id: subscriptionId })
    }

    async oneByUserId(userId: string) {
        return this.subscription.findOneBy({ userId: userId })
    }

    async save(body: any) {
        return this.subscription.insert(body)
    }

    async update(subscriptionId: string, body: any) {
        return this.subscription.update(subscriptionId, body)
    }

    async remove(subscriptionId: string) {
        return this.subscription.delete({ id: subscriptionId })
    }
}