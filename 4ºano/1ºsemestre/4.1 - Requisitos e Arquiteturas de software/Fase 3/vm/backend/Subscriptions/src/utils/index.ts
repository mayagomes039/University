export function isJSONValid(json: string): boolean {
    try {
        // Parse the input to a JSON object
        const parsedJson = JSON.parse(json);

        // Check if both transaction_id and receipt_url are present
        if (typeof parsedJson.transaction_id === 'string' && typeof parsedJson.receipt_url === 'string') {
            return true;
        }

        // If any of the required fields are missing or not strings, return false
        return false;
    } catch (e) {
        // If JSON parsing fails, return false
        return false;
    }
}
