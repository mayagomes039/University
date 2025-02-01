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
                path: '/', 
                httpOnly: true, // Secure cookies
                secure: process.env.NODE_ENV === 'production', // HTTPS only in production
                sameSite: 'lax', // Lax mode for cross-origin requests
            });
            //res.cookie('AUTH', token, {
            //    domain: process.env.CURRENTHOST || 'localhost',
            //    path: '/',          
            //    httpOnly: true,     
            //    secure: false,     
            //  });
        }
        res.json(response.data);
    } catch (error: any) {
        res.status(400).json({ error: error.response?.data?.message });
    }
});

userRouter.post('/register', async(req, res) => {
    try {
        console.log('USERS_API:', process.env.USERS_API);
        console.log('Request body:', req.body);

        const response = await axios.post(`${process.env.USERS_API}/api/user/register`, req.body);
        const token = response.data.token;  
        
        console.log('Response data:', response.data);
        
        if(token){
            res.cookie('AUTH', token, { 
                path: '/', 
                httpOnly: true, // Secure cookies
                secure: process.env.NODE_ENV === 'production', // HTTPS only in production
                sameSite: 'lax', // Lax mode for cross-origin requests
            });
            //res.cookie('AUTH', token, { domain: process.env.CURRENTHOST || 'localhost', path: '/', sameSite: 'lax' });
        }
        
        res.json(response.data);
    } catch (error: any) {
        res.status(400).json({ error: error.response?.data?.message || "An unknown error occurred." });
    }
});

userRouter.post('/logout', checkAuthMiddleware, async(req, res) => {
    try {
        const response = await axios.post(`${process.env.USERS_API}/api/user/logout`, req.body);
        res.clearCookie('AUTH', {
            path: '/', 
            httpOnly: true, // Matches the secure cookie setting
            secure: process.env.NODE_ENV === 'production', // Matches the production secure setting
            sameSite: 'lax', // Matches the sameSite policy used when setting the cookie
        });
        //res.clearCookie('AUTH', { domain: process.env.CURRENTHOST ||  'localhost', path: '/' })
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