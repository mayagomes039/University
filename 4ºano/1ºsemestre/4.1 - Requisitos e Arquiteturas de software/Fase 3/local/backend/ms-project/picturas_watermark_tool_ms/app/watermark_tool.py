from PIL import Image, ImageEnhance
from .core.tool import Tool
from .watermark_request_message import WatermarkParameters
from .watermark_result_message import WatermarkResultOutput

from .config import (S3_ENDPOINT_URL, S3_ACCESS_KEY, S3_SECRET_KEY, S3_USE_SSL, S3_BUCKET_NAME)
from .S3Facade_Shared import S3Facade
import os
import tempfile


class WatermarkTool(Tool):

    def apply(self, parameters: WatermarkParameters):
        """
        Apply the watermark with an overlay effect to the input image and save the result.

        Args:
            parameters (WatermarkParameters): Watermark parameters including input and output URIs.
        """
        # Validate parameters
        if not parameters.inputImageURI or '/' not in parameters.inputImageURI:
            raise ValueError("Invalid inputImageURI. Must be in 'bucketName/fileName' format.")
        if not parameters.watermarkImageURI:
            parameters.watermarkImageURI = parameters.inputImageURI
        if not (0 <= parameters.opacity <= 1):
            raise ValueError("Opacity must be between 0 and 1.")
        if parameters.scale_factor <= 0:
            raise ValueError("Watermark scale factor must be greater than 0.")
        if parameters.positionX < 0 or parameters.positionY < 0:
            raise ValueError("PositionX and PositionY must be non-negative.")

        # Parse S3 bucket and file names from URIs
        def parse_s3_uri(uri):
            parts = uri.split('/')
            if len(parts) < 2:
                raise ValueError(f"Invalid S3 URI format: {uri}")
            return parts[0].strip('{}'), "/".join(parts[1:]).strip("{}")

        s3BucketName, inputImageFileName = parse_s3_uri(parameters.inputImageURI)
        s3BucketNameWatermark, inputImageFileNameWatermark = parse_s3_uri(parameters.watermarkImageURI)

        # Initialize the S3Facade for interacting with S3
        s3_facade = S3Facade(endpoint_url=S3_ENDPOINT_URL, access_key=S3_ACCESS_KEY, 
                             secret_key=S3_SECRET_KEY, use_ssl=S3_USE_SSL)

        # Check if the input and watermark images exist in the S3 bucket
        for bucket, file_name in [(s3BucketName, inputImageFileName), (s3BucketNameWatermark, inputImageFileNameWatermark)]:
            if not s3_facade.file_exists(bucket, file_name):
                raise FileNotFoundError(f"File {file_name} not found in bucket {bucket}")

        # Temporary file paths
        temp_input = tempfile.NamedTemporaryFile(suffix=os.path.splitext(inputImageFileName)[1], delete=False).name
        temp_watermark = tempfile.NamedTemporaryFile(suffix=os.path.splitext(inputImageFileNameWatermark)[1], delete=False).name
        temp_output = tempfile.NamedTemporaryFile(suffix=os.path.splitext(inputImageFileName)[1], delete=False).name

        try:
            # Download the input and watermark images from S3
            s3_facade.download_file(s3BucketName, inputImageFileName, temp_input)
            s3_facade.download_file(s3BucketNameWatermark, inputImageFileNameWatermark, temp_watermark)

            # Open the input image
            with Image.open(temp_input).convert("RGBA") as input_image:
                # Open the watermark image
                watermark = Image.open(temp_watermark).convert("RGBA")

                # Scale the watermark
                smallest_dimension = min(input_image.size)
                scale_factor = smallest_dimension * parameters.scale_factor
                new_watermark_size = (
                    int(watermark.size[0] * scale_factor / watermark.size[0]),
                    int(watermark.size[1] * scale_factor / watermark.size[1]),
                )
                watermark = watermark.resize(new_watermark_size)

                # Apply opacity to the watermark
                watermark = self._apply_opacity(watermark, parameters.opacity)

                # Create a transparent overlay and paste the watermark
                overlay = Image.new("RGBA", input_image.size, (0, 0, 0, 0))
                watermark_position = (parameters.positionX, parameters.positionY)
                overlay.paste(watermark, watermark_position, mask=watermark)

                # Blend the overlay with the input image
                blended_image = Image.alpha_composite(input_image, overlay)

                # Convert the result to RGB and save
                final_image = blended_image.convert("RGB")
                final_image.save(temp_output)

            # Upload the processed image to S3
            s3_facade.upload_file(s3BucketName, temp_output, parameters.outputImageURI)

            # Return the result
            result = WatermarkResultOutput(type="image", imageURI=f"{s3BucketName}/{parameters.outputImageURI}")
            return result

        except Exception as e:
            print(f"Error while adding watermark: {e}")
            raise
        finally:
            # Clean up temporary files
            for temp_file in [temp_input, temp_watermark, temp_output]:
                if temp_file and os.path.exists(temp_file):
                    os.remove(temp_file)

    def _apply_opacity(self, image, opacity):
        """Apply opacity to an RGBA image."""
        if image.mode != "RGBA":
            raise ValueError("Image must be in RGBA mode to apply opacity.")

        alpha = image.split()[3]
        alpha = ImageEnhance.Brightness(alpha).enhance(opacity)
        image.putalpha(alpha)
        return image