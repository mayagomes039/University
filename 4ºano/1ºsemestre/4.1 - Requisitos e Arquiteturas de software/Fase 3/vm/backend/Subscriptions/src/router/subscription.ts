import { Router } from "express";
import { SubscriptionState, SubscriptionType } from "../db/types";
import { isJSONValid } from "../utils";
import axios from "axios";
import { SubscriptionController } from "../controller/Subscription";
import { PaymentController } from "../controller/Payment";

const subscriptionRouter = Router();
const subscriptionController = new SubscriptionController();
const paymentController = new PaymentController();

// Add user subscription
subscriptionRouter.post("/", async (req, res, next) => {
    let { userId, type, extra } = req.body;

    // Validate fields
    if (!userId || !type || !extra || typeof userId !== "string" || typeof extra !== "string" || !isJSONValid(extra) || !Object.values(SubscriptionType).includes(type)) {
        res.status(400).json({ message: "Missing required fields in subscription" });
        return;
    }

    try{
        let subscription = await subscriptionController.oneByUserId(userId);
        console.log("SUSBCRIP: ",subscription)

        // If subscription entry already exists, update the information
        if(subscription){

            if (subscription.state == SubscriptionState.ACTIVE) {
                res.status(400).json({ message: "User already has an active subscription" });
                return;
            }

            await subscriptionController.update(subscription.id, { 
                type: type,
                state: SubscriptionState.ACTIVE,
                insertedAt: new Date()
            })

            subscription = await subscriptionController.oneByUserId(userId);
            if (!subscription) {
                res.status(400).json({ message: "Error updating subscription" });
                return;
            }

            await paymentController.save({
                subscriptionId: subscription.id,
                extra: JSON.parse(extra)
            })
        }
        else{
            console.log("URL: ","http://user-server:3002/api/user/" + userId)
            const user = await axios.get("http://user-server:3002/api/user/" + userId)
            if (user.status !== 200 || !user.data) {
                res.status(400).json({ message: "User doesn't exist" });
                return;
            }

            const newSubscription = await subscriptionController.save({
                userId: userId,
                type: type,
                state: SubscriptionState.ACTIVE,
                insertedAt: new Date()
            })
    
            await paymentController.save({
                subscriptionId: newSubscription.identifiers[0].id,
                extra: JSON.parse(extra)
            })

            console.log("URL: ","http://user-server:3002/api/user/edit/" + userId)
            const updateUserType = await axios.post("http://user-server:3002/api/user/edit/" + userId, {
                type: "premium"
            })
            if (updateUserType.status !== 200) {
                res.status(400).json({ message: "Failed to update user type" });
                return;
            }
        }
        res.status(200).json({ message: "Subscription added successfully" });

    }
    catch (e) {
        console.log(e)
        res.status(400).json({ message: "Error in the database" });
        return;
    }
});

// Cancel user subscription
subscriptionRouter.post("/cancel", async (req, res, next) => {
    const { userId } = req.body;

    // Validate fields
    if (!userId ||  typeof userId !== "string") {
        res.status(400).json({ message: "Missing required fields" });
        return;
    }

    const subscription = await subscriptionController.oneByUserId(userId);

    if (!subscription) {
        res.status(400).json({ message: "User does not have a subscription" });
        return;
    }

    else if (subscription.state === SubscriptionState.INACTIVE) {
        res.status(400).json({ message: "User subscription already inactive" });
        return;
    }

    await subscriptionController.update(subscription.id, {
        state: SubscriptionState.INACTIVE
    });

    res.status(200).json({ message: "User subscription cancelled successfully" });
});

export default subscriptionRouter;