import { Router } from "express";
import axios from "axios";
import { checkAuthMiddleware } from "../../middleware";

const userRouter = Router();

userRouter.get('/:id', checkAuthMiddleware, async(req, res) => {
    try {
        const response = await axios.get(`${process.env.USERS_API}/api/user/${req.params.id}`);
        res.json(response.data);
    } catch (error: any) {
        res.status(400).json({ error: error.response?.data?.message });
    }
});

userRouter.post('/login', async(req, res) => {
    try {
        const response = await axios.post(`${process.env.USERS_API}/api/user/login`, req.body);
        const token = response.data.token;
        if(token){
            res.cookie('AUTH', token, {
                domain: 'localhost',
                path: '/',          
                httpOnly: true,     
                secure: false,     
              });
        }
        res.json(response.data);
    } catch (error: any) {
        res.status(400).json({ error: error.response?.data?.message });
    }
});

userRouter.post('/register', async(req, res) => {
    try {
        const response = await axios.post(`${process.env.USERS_API}/api/user/register`, req.body);
        const token = response.data.token;  
        if(token){
            res.cookie('AUTH', token, { domain: 'localhost', path: '/' });
        }
        res.json(response.data);
    } catch (error: any) {
        res.status(400).json({ error: error.response?.data?.message });
    }
});

userRouter.post('/logout', checkAuthMiddleware, async(req, res) => {
    try {
        const response = await axios.post(`${process.env.USERS_API}/api/user/logout`, req.body);
        res.clearCookie('AUTH', { domain: 'localhost', path: '/' })
        res.json(response.data);
    } catch (error: any) {
        res.status(400).json({ error: error.response?.data?.message });
    }
})

userRouter.post('/edit/:id', checkAuthMiddleware, async(req, res) => {
    try {
        const response = await axios.post(`${process.env.USERS_API}/api/user/edit/${req.params.id}`, req.body);
        res.json(response.data);
    } catch (error: any) {
        res.status(400).json({ error: error.response?.data?.message });
    }
})


export default userRouter;