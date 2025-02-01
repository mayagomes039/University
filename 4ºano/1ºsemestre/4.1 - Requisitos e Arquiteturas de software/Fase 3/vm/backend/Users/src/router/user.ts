import { Router } from "express";
import informationHash from "../lib/information-hash";
import { UserType } from "../db/types";
import { UserController } from "../controller/User";
import * as jwt from "jsonwebtoken";

const userRouter = Router();
const userController = new UserController()

// Check if user exists
userRouter.get("/:id", async (req, res, next) => {
  const userId = req.params.id

  try{
    const user = await userController.one(userId)
    res.status(200).json(user);
  }
  catch{
    res.status(400).json({ message: "Error fetching user data" });
  }
});


// User authentication
userRouter.post("/login", async (req, res, next) => {
  const { email, password } = req.body

  // Validate fields
  if (!email || !password || typeof email !== 'string' || typeof password !== 'string') {
    res.status(400).json({ message: "Missing required fields" });
    return
  }

  const user = await userController.oneByEmail(email)

  if (user) {
      const encryptedPassword = informationHash.encrypt(password)
      if (encryptedPassword === user.passwordHash) {
          const sessionToken = jwt.sign({ email: email, plan: user.type, name: user.name, id: user.id}, process.env.SECRET_KEY_JWT || "secret_key", { expiresIn: '1h' });
          res.status(200).json({ message: "Login successful", token: sessionToken });
          return
      }
  }

  res.status(400).json({ message: "Invalid credentials" });

  
});


// User register
userRouter.post("/register", async (req, res, next) => {
  const { name, email, password, type } = req.body

  // Validate fields
  if (!name || !type || typeof name !== 'string' || !Object.values(UserType).includes(type)) {
    res.status(400).json({ message: "Missing required fields" });
    return;
  }

  // If type is not anonymous, email and password are required
  if (type !== UserType.ANONYMOUS) {
    if (!email || !password || typeof email !== 'string' || typeof password !== 'string') {
      res.status(400).json({ message: "Missing required fields" });
      return;
    }

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) {
      res.status(400).json({ message: "Invalid email format" });
      return;
    }

    const user = await userController.oneByEmail(email)
    if(user){
      res.status(400).json({ message: "Email is already registered" });
      return
    }
  }

  try{
      const user = await userController.save({
      name,
      email: type !== UserType.ANONYMOUS ? email : null,
      passwordHash: type !== UserType.ANONYMOUS ? informationHash.encrypt(password) : null,
      type: type === UserType.PREMIUM ? UserType.FREE : type
    })
    console.log(user)

    let sessionToken = null
    if(type != UserType.ANONYMOUS){
      sessionToken = jwt.sign({ email: email, id: user.identifiers[0].id }, process.env.SECRET_KEY_JWT || "secret_key", { expiresIn: '1h' });
    }
    else{
      sessionToken = jwt.sign({ name: name, id: user.identifiers[0].id }, process.env.SECRET_KEY_JWT || "secret_key", { expiresIn: '1h' });
    }

    res.status(200).json({ message: "User created", token: sessionToken });
    return
  }
  catch(e){
    res.status(400).json({ message: "Error creating user" });
  }

});


userRouter.post("/logout", async (req, res, next) => {
  res.status(200).json({ message: "Logged out" });
});


// User edit
userRouter.post("/edit/:id", async (req, res, next) => {
  const userId = req.params.id
  const { name, email, password, type } = req.body

  try{
    const user = await userController.one(userId)
    if(!user){
      res.status(400).json({ message: "User doesn't exist" });
      return
    }

    const updateData: any = {};

    // Validate and include `name` if provided
    if (name) {
      if (typeof name !== "string") {
        res.status(400).json({ message: "Invalid name" });
        return;
      }
      updateData.name = name;
    }

    // Validate and include `type` if provided
    if (type) {
      if (!Object.values(UserType).includes(type)) {
        res.status(400).json({ message: "Invalid type" });
        return;
      }
      updateData.type = type;
    }

    // If type is not anonymous, email and password are required
    if (user.type !== UserType.ANONYMOUS) {
      if (email) {
        if (typeof email !== "string") {
          res.status(400).json({ message: "Invalid email" });
          return;
        }

        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(email)) {
          res.status(400).json({ message: "Invalid email format" });
          return;
        }

        // Check if the email is already registered by another user
        const existingUser = await userController.oneByEmail(email);
        if (existingUser && existingUser.id !== userId) {
          res.status(400).json({ message: "Email is already registered" });
          return;
        }

        updateData.email = email;
      }

      if (password) {
        if (typeof password !== "string") {
          res.status(400).json({ message: "Invalid password" });
          return;
        }
        updateData.passwordHash = informationHash.encrypt(password);
      }
    }

    // Perform the update if there's data to update
    if (Object.keys(updateData).length === 0) {
      res.status(400).json({ message: "No valid fields provided for update" });
      return;
    }

    await userController.update(userId, updateData);

    //atualizar o token com os novos valores
    const sessionToken = jwt.sign({ email: email, plan: type, name: name, id: userId}, process.env.SECRET_KEY_JWT || "secret_key", { expiresIn: '1h' });
    res.cookie('AUTH', sessionToken, { 
      path: '/', 
      httpOnly: true, // Secure cookies
      secure: process.env.NODE_ENV === 'production', // HTTPS only in production
      sameSite: 'lax', // Lax mode for cross-origin requests
    });
    //res.cookie('AUTH', sessionToken, { domain: process.env.CURRENTHOST || 'localhost', path: '/' })
    //mandar token atualizado
    res.status(200).json({ message: "User edited successfully", token: sessionToken });
  } catch (e) {
    console.log(e);
    res.status(400).json({ message: "Error editing user" });
  }
});


export default userRouter;