from typing import Optional
from pydantic import BaseModel, field_validator


from .core.messages.request_message import RequestMessage

class AutoCropParameters(BaseModel):
    inputImageURI: str
    outputImageURI: str
    blur_radius: Optional[int] = 5      # Default noise reduction level
    edge_threshold: Optional[int] = 130 # Default minimum edge intensity to consider
    padding: Optional[int] = 10         # Default extra space around the cropped content

    # Validator for blur_radius
    @field_validator("blur_radius")
    def validate_blur_radius(cls, value: int):
        if value is not None and not (0 <= value <= 10):
            raise ValueError("blur_radius must be between 0 and 10.")
        return value

    # Validator for edge_threshold
    @field_validator("edge_threshold")
    def validate_edge_threshold(cls, value: int):
        if value is not None and not (0 <= value <= 255):
            raise ValueError("edge_threshold must be between 0 and 255.")
        return value

    # Validator for padding
    @field_validator("padding")
    def validate_padding(cls, value: int):
        if value is not None and value < 0:
            raise ValueError("padding must be non-negative.")
        return value

    
AutoCropRequestMessage = RequestMessage[AutoCropParameters]