const express = require("express");

const app = express();
const PORT = 3000;

app.use(express.json());

// Middleware to catch invalid JSON
app.use((err, req, res, next) => {
    if (err instanceof SyntaxError && err.status === 400 && 'body' in err) {
        let message = "Invalid JSON format."
        console.log(message);
        return res.status(400).json({
            success: false,
            active: false,
            error: message
        });
    }
    next();
});

// Helper to check if a string contains only digits
function isDigitsOnly(str) {
    return /^\d+$/.test(str);
}

// Payment endpoint
app.post("/api/pay", (req, res) => {
    if (!req.body || Object.keys(req.body).length === 0) {
        let message = "Request body is empty or missing."
        console.log(message);
        return res.status(400).json({
            success: false,
            active: false,
            error: message
        });
    }

    const { cardNumber, cardName, ccv } = req.body;

    if (!cardNumber || !cardName || !ccv) {
        let message = "Missing payment information."
        console.log(message);
        return res.status(400).json({
            success: false,
            active: false,
            error: message
        });
    }

    // Generic validation for card number
    if (!isDigitsOnly(cardNumber) || cardNumber.length < 13 || cardNumber.length > 19) {
        let message = "Invalid card number."
        console.log(message);
        return res.status(400).json({
            success: false,
            active: false,
            error: message
        });
    }

    // Generic validation for CCV
    if (!isDigitsOnly(ccv) || ccv.length < 3 || ccv.length > 4) {
        let message = "Invalid CCV."
        console.log(message);
        return res.status(400).json({
            success: false,
            active: false,
            error: message
        });
    }

    // Generate expiry date (now + 1 month)
    const now = new Date();
    now.setMonth(now.getMonth() + 1);
    const expiryDate = now.toISOString(); // To ISO String to be parsed in the app backend into a date Object
    
    //Generate expiry date (now + 1 minute)
    //const now = new Date();
    //now.setMinutes(now.getMinutes() + 1);
    //const expiryDate = now.toISOString(); // ISO string for backend parsing


    let message = "Fake payment processed successfully!"
    console.log(message);
    // Success response
    return res.status(200).json({
        success: true,
        active: true,
        message: message,
        expiryDate: expiryDate
    });
});

app.listen(PORT, () => {
    console.log(`Fake Payment Service running at http://localhost:${PORT}`);
});
