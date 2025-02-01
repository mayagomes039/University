import jwt from 'jsonwebtoken';

// Middleware to check if the user is authenticated
export const checkAuthMiddleware = (req: any, res: any, next: any) => {
    // Check for token in Authorization header or in cookies
    const token = req.headers['authorization']?.split(' ')[1] || req.cookies['AUTH'];

    if (!token) {
        return res.status(401).json({ message: 'Unauthorized: No token provided' });
    }

    try {
        // Verify the token using your secret
        const decoded = jwt.verify(token, process.env.JWT_SECRET || 'secret_key');
        req.user = decoded; // Attach user info to the request if needed
        next(); // Proceed to the next middleware or the route handler
    } catch (error) {
        return res.status(401).json({ message: 'Unauthorized: Invalid token' });
    }
};
