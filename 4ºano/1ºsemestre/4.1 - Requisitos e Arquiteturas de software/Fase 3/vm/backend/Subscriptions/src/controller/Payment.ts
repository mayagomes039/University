import { AppDataSource } from "../db/data-source";
import { Payment } from "../db/Entities/Payment";

export class PaymentController {

    private payment = AppDataSource.getRepository(Payment)

    async all() {
        return this.payment.find()
    }

    async one(paymentId: string) {
        return this.payment.findOneBy({ id: paymentId })
    }

    async oneBySubscriptionId(subscriptionId: string) {
        return this.payment.findOneBy({ subscriptionId: subscriptionId })
    }

    async save(body: any) {
        return this.payment.insert(body)
    }

    async update(paymentId: string, body: any) {
        return this.payment.update(paymentId, body)
    }

    async remove(paymentId: string) {
        return this.payment.delete({ id: paymentId })
    }
}