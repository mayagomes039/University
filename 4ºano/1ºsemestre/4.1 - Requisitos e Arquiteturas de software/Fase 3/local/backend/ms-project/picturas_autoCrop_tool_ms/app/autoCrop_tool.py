from PIL import Image, ImageChops, ImageFilter, ImageOps
from .core.tool import Tool
from .autoCrop_request_message import AutoCropParameters
from .autoCrop_result_message import AutoCropResultOutput

from .config import (S3_ENDPOINT_URL, S3_ACCESS_KEY, S3_SECRET_KEY, S3_USE_SSL, S3_BUCKET_NAME)
from .S3Facade_Shared import S3Facade
import os
import tempfile


class AutoCropTool(Tool):

    def apply(self, parameters: AutoCropParameters):
        """
        Applies automatic cropping to an image based on its content.

        Args:
            parameters (AutoCropParameters): Parameters containing input and output paths.

        Returns:
            AutoCropResultOutput: Contains the type and URI of the processed image.

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

            # Generate temporary file paths
            temp_input = tempfile.NamedTemporaryFile(suffix=file_extension, delete=False).name
            temp_output = tempfile.NamedTemporaryFile(suffix=file_extension, delete=False).name

            print(f"Downloading file from S3: {inputImageFileName}")
            # Download the input image from S3 to a temporary file
            s3_facade.download_file(s3BucketName, inputImageFileName, temp_input)

            # Open the image for processing
            with Image.open(temp_input) as img:
                # Detect the original format
                original_format = img.format

                # Convert to grayscale for processing
                grayscale = img.convert("L")

                # Apply Gaussian Blur to reduce noise
                blurred = grayscale.filter(ImageFilter.GaussianBlur(radius=parameters.blur_radius))

                # Apply edge detection
                edges = blurred.filter(ImageFilter.FIND_EDGES)

                # Enhance edges to make them more prominent
                edges = ImageOps.autocontrast(edges)

                # Convert edges image to a list of pixel values
                edge_data = list(edges.getdata())

                # Identify non-zero regions (content regions)
                width, height = edges.size
                non_zero_indices = [
                    (i // width, i % width) for i, value in enumerate(edge_data) if value > parameters.edge_threshold
                ]

                if non_zero_indices:
                    # Calculate the bounding box of the content
                    top = max(0, min(idx[0] for idx in non_zero_indices) - parameters.padding)
                    left = max(0, min(idx[1] for idx in non_zero_indices) - parameters.padding)
                    bottom = min(height, max(idx[0] for idx in non_zero_indices) + parameters.padding)
                    right = min(width, max(idx[1] for idx in non_zero_indices) + parameters.padding)

                    # Crop the original image using the bounding box
                    cropped_image = img.crop((left, top, right, bottom))
                else:
                    # No significant content; use the original image
                    cropped_image = img

                # Save the cropped image in the original format
                cropped_image.save(temp_output, format=original_format)
                print(f"Cropped image saved to: {temp_output}")

            # Validate temp_output size before upload
            if os.path.exists(temp_output) and os.path.getsize(temp_output) > 0:
                print(f"Uploading file to S3: {parameters.outputImageURI}")
                s3_facade.upload_file(S3_BUCKET_NAME, temp_output, parameters.outputImageURI)
            else:
                raise RuntimeError("Generated output file is empty or missing.")

            # Return the result output
            result = AutoCropResultOutput(type="image", imageURI=f"my-bucket/{parameters.outputImageURI}")
            return result
        except Exception as e:
            # Log and re-raise any exceptions that occur
            print(f"Error while performing autocrop: {e}")
            raise e
        finally:
            # Ensure temporary files are deleted to prevent clutter
            for temp_file in [temp_input, temp_output]:
                if temp_file and os.path.exists(temp_file):
                    os.remove(temp_file)
