import jwt from 'jsonwebtoken';

const requireOwnerAuth = (req, res, next) => {
    try {
        

        let token = req.cookies.owner_auth_token;

        if (!token && req.headers.authorization && req.headers.authorization.startsWith('Bearer ')) {
            token = req.headers.authorization.split(' ')[1];
        }

        if (!token) {
            return res.status(401).json({ message: 'Authentication required' });
        }

        const decoded = jwt.verify(token, process.env.JWT_SECRET);

        if (decoded.sub !== 'owner') {
            return res.status(403).json({ message: 'Invalid token subject' });
        }

        // Token is valid
        req.user = decoded;
        next();
    } catch (error) {
       
        return res.status(401).json({ message: 'Invalid or expired token' });
    }
};

export default requireOwnerAuth;
