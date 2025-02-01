import { Router } from "express";
import { isJSONValid } from "../utils";
import { SubscriptionState, SubscriptionType } from "../db/types";
import { PaymentController } from "../controller/Payment";
import { SubscriptionController } from "../controller/Subscription";

const paymentRouter = Router();
const subscriptionController = new SubscriptionController();
const paymentController = new PaymentController();

// User payment
paymentRouter.post("/", async (req, res, next) => {
    const { userId, extra } = req.body;

    // Validate fields
    if (!extra || !userId || typeof userId !== "string" || typeof extra !== "string" || !isJSONValid(extra)) {
        res.status(400).json({ message: "Missing required fields" });
        return;
    }

    try{
        const subscription = await subscriptionController.oneByUserId(userId);

        if (!subscription) {
            res.status(400).json({ message: "User does not have a subscription" });
            return;
        }

        else if(subscription.state == SubscriptionState.INACTIVE){
            res.status(400).json({ message: "Can't do payments on inactive subscriptions" });
            return;
        }

        let timeToAdd  = 1
        if(subscription.type == SubscriptionType.ANNUAL){
            timeToAdd  = 12
        }
        
        const expirationDate = new Date(subscription.insertedAt);
        expirationDate.setMonth(expirationDate.getMonth() + timeToAdd);

        /*if(expirationDate > new Date()){
            res.status(400).json({ message: "User subscription is still active" });
            return
        }*/
    
        await paymentController.save({
            subscriptionId: subscription.id,
            extra: JSON.parse(extra)
        })

        await subscriptionController.update(subscription.id, {
            insertedAt: new Date()
        })

        res.status(200).json({ message: "Payment success" });

    } catch (e) {
        res.status(400).json({ message: "Error creating payment" });
        return;
    }
});


export default paymentRouter;