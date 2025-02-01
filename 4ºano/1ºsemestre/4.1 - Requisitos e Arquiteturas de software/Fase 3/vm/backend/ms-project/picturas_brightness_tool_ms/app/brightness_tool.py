from PIL import Image, ImageEnhance
from .core.tool import Tool
from .brightness_request_message import BrightnessParameters
from .brightness_result_message import BrightnessResultOutput

from .config import (S3_ENDPOINT_URL, S3_ACCESS_KEY, S3_SECRET_KEY, S3_USE_SSL, S3_BUCKET_NAME)
from .S3Facade_Shared import S3Facade
import os
import tempfile


class BrightnessTool(Tool):
    
    def apply(self, parameters: BrightnessParameters):
        """
        Apply brightness enhancement to an image.

        Args:
            parameters (BrightnessParameters): Parameters containing input and output image URIs,
                                               and optional bucket name.

        Returns:
            BrightnessResultOutput: Output containing the type and URI of the enhanced image.

        Raises:
            FileNotFoundError: If the input image does not exist in the S3 bucket.
            ValueError: If the input image URI does not have a valid file extension.
            Exception: For other errors during processing.
        """

        # Validate parameters
        if parameters.brightness_factor < 0:
            raise ValueError("Brightness factor must be greater than or equal to 0.")

        if not parameters.inputImageURI or '/' not in parameters.inputImageURI:
            raise ValueError("Invalid inputImageURI. Must be in 'bucketName/fileName' format.")

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

            # Check if the input image exists in the bucket
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
            # Download the input image from S3 to a temporary file
            s3_facade.download_file(s3BucketName, inputImageFileName, temp_input)

            # Open the input image for processing
            with Image.open(temp_input) as input_image:
                # Create a brightness enhancer using the input image
                enhancer = ImageEnhance.Brightness(input_image)
                # Adjust the brightness using the specified factor
                enhanced_image = enhancer.enhance(parameters.brightness_factor)

                # Save the enhanced image to a temporary file
                enhanced_image.save(temp_output)

            # Upload the processed image to S3
            print(f"Uploading file to S3: {parameters.outputImageURI}")
            s3_facade.upload_file(s3BucketName, temp_output, parameters.outputImageURI)

            print(f"Brightness successfully adjusted and saved to {parameters.outputImageURI}")

            # Create and return the result output
            result = BrightnessResultOutput(type="image", imageURI=f"{s3BucketName}/{parameters.outputImageURI}")
            return result
        except Exception as e:
            # Handle any exception that occurs and re-raise it for further handling
            print(f"Error while adjusting brightness: {e}")
            raise e
        finally:
            # Ensure temporary files are cleaned up
            for temp_file in [temp_input, temp_output]:
                if temp_file and os.path.exists(temp_file):
                    os.remove(temp_file)
