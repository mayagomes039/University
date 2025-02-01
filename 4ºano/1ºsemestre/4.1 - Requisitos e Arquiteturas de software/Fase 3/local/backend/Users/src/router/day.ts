import { Router } from "express";
import { AppDataSource } from "../db/data-source";
import { Day } from "../db/Entities/Day";
import { User } from "../db/Entities/User";
import { UserController } from "../controller/User";
import { DayController } from "../controller/Day";

const dayRouter = Router();
const userController = new UserController()
const dayController = new DayController()

// Add processed for user
dayRouter.post("/:id", async (req, res, next) => {
    const userId = req.params.id
    try{
        const user = await userController.one(userId)        
        if(!user){
            res.status(400).json({ message: "User doesn't exist" });
            return
        }

        const currentDate = new Date().toISOString().split('T')[0]

        const day = await dayController.one(userId, currentDate)

        // If exists, add 1 to processed images
        if(day){
            await dayController.update(userId, currentDate, {
                processed: day.processed + 1
            })
        }
        // Else create new entry
        else{
            await dayController.save({
                userId: userId,
                day: currentDate,
                processed: 1
            })
        }

        res.status(200).json({ message: "User day updated" });
    }
    catch{
        res.status(400).json({ message: "Error adding user day information" });
    }



});



export default dayRouter;