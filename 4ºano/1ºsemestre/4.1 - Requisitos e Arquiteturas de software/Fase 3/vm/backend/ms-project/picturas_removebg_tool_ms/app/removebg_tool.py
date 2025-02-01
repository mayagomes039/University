from PIL import Image, ImageOps
from .core.tool import Tool
from .removebg_request_message import RemovebgParameters
from .removebg_result_message import RemovebgResultOutput

from .config import (S3_ENDPOINT_URL, S3_ACCESS_KEY, S3_SECRET_KEY, S3_USE_SSL, S3_BUCKET_NAME)
from .S3Facade_Shared import S3Facade
import os
import tempfile
from rembg import remove


class RemovebgTool(Tool):

    def apply(self, parameters: RemovebgParameters):
        """
        Remove the background from an input image and save the output.

        Args:
            parameters (RemovebgParameters): Parameters containing input and output paths.

        Returns:
            RemovebgResultOutput: Contains the type and URI of the processed image.

        Raises:
            FileNotFoundError: If the input image is not found in the specified S3 bucket.
            ValueError: If the input image URI does not have a valid file extension.
            Exception: For other unexpected errors during processing.
        """
        temp_input = ""  # Temporary file path for the input image
        temp_output = ""  # Temporary file path for the output image

        try:
            # Parse S3 bucket and file name from inputImageURI
            parts = parameters.inputImageURI.split('/')
            if len(parts) < 2:
                raise ValueError("Invalid inputImageURI. Must be in 'bucketName/fileName' format.")

            s3BucketName = parts[0].strip("{}")
            inputImageFileName = "/".join(parts[1:]).strip("{}")

            # Initialize the S3Facade for S3 operations
            s3_facade = S3Facade(endpoint_url=S3_ENDPOINT_URL, access_key=S3_ACCESS_KEY, 
                                 secret_key=S3_SECRET_KEY, use_ssl=S3_USE_SSL)

            # Check if the input image exists in the S3 bucket
            if not s3_facade.file_exists(s3BucketName, inputImageFileName):
                raise FileNotFoundError(f"Input image {inputImageFileName} not found in bucket {s3BucketName}.")

            # Extract the file extension from the input image URI
            file_extension = os.path.splitext(inputImageFileName)[1]
            if not file_extension:
                raise ValueError("Input image URI does not have a valid file extension.")

            # Generate temporary file paths with the same file extension
            temp_input = tempfile.NamedTemporaryFile(suffix=file_extension, delete=False).name
            temp_output = tempfile.NamedTemporaryFile(suffix=".png", delete=False).name  # Output as PNG for transparency

            print(f"Downloading file from S3: {inputImageFileName}")
            # Download the input image from S3 to a temporary file
            s3_facade.download_file(s3BucketName, inputImageFileName, temp_input)

            # Open the input image and remove its background
            with open(temp_input, "rb") as input_image:
                result = remove(input_image.read())

            # Save the output to the temporary file
            with open(temp_output, "wb") as output_image:
                output_image.write(result)

            # Upload the processed image to S3
            print(f"Uploading file to S3: {parameters.outputImageURI}")
            s3_facade.upload_file(s3BucketName, temp_output, parameters.outputImageURI)

            print(f"Background successfully removed and saved to {parameters.outputImageURI}")

            # Return the result output containing the processed image URI
            result = RemovebgResultOutput(type="image", imageURI=f"{s3BucketName}/{parameters.outputImageURI}")
            return result
        except Exception as e:
            # Log and re-raise any exceptions that occur
            print(f"Error while removing background: {e}")
            raise e
        finally:
            # Ensure temporary files are deleted to prevent clutter
            for temp_file in [temp_input, temp_output]:
                if temp_file and os.path.exists(temp_file):
                    os.remove(temp_file)