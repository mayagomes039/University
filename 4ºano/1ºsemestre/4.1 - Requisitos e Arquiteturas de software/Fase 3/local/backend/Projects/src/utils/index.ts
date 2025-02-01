import { ToolProcedure, ParamsPerTool } from "../db/types";


export function isJSONValid(json: string, tool: ToolProcedure): boolean {
    try {
        // Parse the input JSON string
        const parsedJson = JSON.parse(json);

        // Get the validation rules for the given tool from ParamsPerTool
        const rules = ParamsPerTool[tool];

        // If no specific rules are defined for the tool, the JSON is valid
        if (!rules) return true;

        // Validate each required field based on the rules
        for (const [key, type] of Object.entries(rules)) {
            if (type && typeof parsedJson[key] !== type) {
                // Validate other types
                return false;
            }
        }
    
        // If all validations pass, return true
        return true;
    } catch (e) {
        // If JSON parsing fails, return false
        return false;
    }
}
