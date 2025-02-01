from PIL import Image
from .core.tool import Tool
from .rotate_request_message import RotateParameters
from .rotate_result_message import RotateResultOutput

from .config import (S3_ENDPOINT_URL, S3_ACCESS_KEY, S3_SECRET_KEY, S3_USE_SSL, S3_BUCKET_NAME)
from .S3Facade_Shared import S3Facade
import os
import tempfile


class RotateTool(Tool):
    def apply(self, parameters: RotateParameters):
        """
        Apply rotation to an image.

        Args:
            parameters (RotateParameters): Contains input and output image URIs, and optional S3 bucket name.

        Returns:
            RotateResultOutput: Contains the type and URI of the rotated image.

        Raises:
            FileNotFoundError: If the input image does not exist in the specified S3 bucket.
            ValueError: If the input image URI does not have a valid file extension.
            Exception: For other unexpected errors.
        """
        # Validate parameters
        if not parameters.inputImageURI or '/' not in parameters.inputImageURI:
            raise ValueError("Invalid inputImageURI. Must be in 'bucketName/fileName' format.")

        if not (0 <= parameters.angle <= 360):
            raise ValueError("Angle must be a number between 0 and 360.")

        temp_input = ""  # Temporary file path for the input image
        temp_output = ""  # Temporary file path for the output image

        try:
            # Parse S3 bucket and file name from inputImageURI
            parts = parameters.inputImageURI.split('/')
            if len(parts) < 2:
                raise ValueError("Invalid inputImageURI. Must be in 'bucketName/fileName' format.")

            s3BucketName = parts[0].strip("{}")
            inputImageFileName = "/".join(parts[1:]).strip("{}")

            # Initialize the S3Facade for interacting with S3
            s3_facade = S3Facade(endpoint_url=S3_ENDPOINT_URL, access_key=S3_ACCESS_KEY, 
                                 secret_key=S3_SECRET_KEY, use_ssl=S3_USE_SSL)

            # Check if the input image exists in the S3 bucket
            if not s3_facade.file_exists(s3BucketName, inputImageFileName):
                raise FileNotFoundError(f"Input image {inputImageFileName} not found in bucket {s3BucketName}.")

            # Extract the file extension from the input image URI
            file_extension = os.path.splitext(inputImageFileName)[1]
            if not file_extension:
                raise ValueError("Input image URI does not have a valid file extension.")

            # Generate temporary file names for processing, preserving the file extension
            temp_input = tempfile.NamedTemporaryFile(suffix=file_extension, delete=False).name
            temp_output = tempfile.NamedTemporaryFile(suffix=file_extension, delete=False).name

            print(f"Downloading file from S3: {inputImageFileName}")
            # Download the input image from S3 to the temporary file
            s3_facade.download_file(s3BucketName, inputImageFileName, temp_input)

            # Open the input image for processing
            with Image.open(temp_input) as input_image:
                # Rotate the image by the specified angle and expand to fit the new dimensions
                rotated_image = input_image.rotate(parameters.angle, expand=True)

                # Save the rotated image to the temporary output file
                rotated_image.save(temp_output)

            # Upload the rotated image to S3
            print(f"Uploading file to S3: {parameters.outputImageURI}")
            s3_facade.upload_file(s3BucketName, temp_output, parameters.outputImageURI)

            print(f"Image successfully rotated and saved to {parameters.outputImageURI}")

            # Create and return the result output
            result = RotateResultOutput(type="image", imageURI=f"{s3BucketName}/{parameters.outputImageURI}")
            return result
        except Exception as e:
            # Log and re-raise any exceptions
            print(f"Error while rotating image: {e}")
            raise e
        finally:
            # Ensure temporary files are deleted
            for temp_file in [temp_input, temp_output]:
                if temp_file and os.path.exists(temp_file):
                    os.remove(temp_file)