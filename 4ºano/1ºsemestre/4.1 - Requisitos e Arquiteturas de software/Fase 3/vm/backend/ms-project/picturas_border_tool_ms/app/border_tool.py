from PIL import Image, ImageOps
from .core.tool import Tool
from .border_request_message import BorderParameters
from .border_result_message import BorderResultOutput

from .config import (S3_ENDPOINT_URL, S3_ACCESS_KEY, S3_SECRET_KEY, S3_USE_SSL, S3_BUCKET_NAME)
from .S3Facade_Shared import S3Facade
import os
import tempfile


class BorderTool(Tool):

    def apply(self, parameters: BorderParameters):
        """
        Apply a border to the input image and save the output.

        Args:
            parameters (BorderParameters): Parameters containing input and output paths.

        Returns:
            BorderResultOutput: Contains the type and URI of the processed image.

        Raises:
            FileNotFoundError: If the input image is not found in the specified S3 bucket.
            ValueError: If the input image URI does not have a valid file extension.
            Exception: For other unexpected errors during processing.

        """
        # Validate parameters
        if parameters.border_width <= 0:
            raise ValueError("Border width must be greater than 0.")

        if not isinstance(parameters.border_color, tuple) or len(parameters.border_color) != 3 or \
           any(c < 0 or c > 255 for c in parameters.border_color):
            raise ValueError("Invalid border color. It must be a tuple of three integers (0-255).")

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
            temp_output = tempfile.NamedTemporaryFile(suffix=file_extension, delete=False).name

            print(f"Downloading file from S3: {inputImageFileName}")
            # Download the input image from S3 to a temporary file
            s3_facade.download_file(s3BucketName, inputImageFileName, temp_input)

            # Open the input image for processing
            with Image.open(temp_input) as input_image:
                # Add the border to the image
                bordered_image = ImageOps.expand(input_image, border=parameters.border_width, fill=parameters.border_color)

                # Save the image with the border to the temporary output file
                bordered_image.save(temp_output)

            # Upload the processed image to S3
            print(f"Uploading file to S3: {parameters.outputImageURI}")
            s3_facade.upload_file(s3BucketName, temp_output, parameters.outputImageURI)

            print(f"Border successfully added to image and saved to {parameters.outputImageURI}")

            # Return the result output containing the processed image URI
            result = BorderResultOutput(type="image", imageURI=f"{s3BucketName}/{parameters.outputImageURI}")
            return result
        except Exception as e:
            # Log and re-raise any exceptions that occur
            print(f"Error while adding border: {e}")
            raise e
        finally:
            # Ensure temporary files are deleted to prevent clutter
            for temp_file in [temp_input, temp_output]:
                if temp_file and os.path.exists(temp_file):
                    os.remove(temp_file)
