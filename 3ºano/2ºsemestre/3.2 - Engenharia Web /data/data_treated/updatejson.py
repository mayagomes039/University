import os
import json

def update_json_paths(json_file_path, base_path):
    # Load the existing JSON data
    with open(json_file_path, 'r', encoding='utf-8') as file:
        data = json.load(file)

    # Traverse the JSON structure and update the image paths
    for rua in data:
        if "figuras" in rua:
            for figura in rua["figuras"]:
                image_path = figura["imagem"]
                if "atual" in image_path:
                    # Extract the path after "atual/"
                    new_path = image_path.split("atual/", 1)[1]
                    figura["imagem"] = f"../atual/{new_path}"

    # Save the updated JSON data back to the file
    with open(json_file_path, 'w', encoding='utf-8') as file:
        json.dump(data, file, ensure_ascii=False, indent=4)

# Example usage
json_file_path = 'ruas.json'  # Replace with the path to your JSON file
update_json_paths(json_file_path, "../atual/")