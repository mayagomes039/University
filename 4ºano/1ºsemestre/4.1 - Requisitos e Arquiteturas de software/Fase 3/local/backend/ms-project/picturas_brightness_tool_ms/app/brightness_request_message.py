from typing import Optional
from pydantic import BaseModel, field_validator

from .core.messages.request_message import RequestMessage


class BrightnessParameters(BaseModel):
    inputImageURI: str
    outputImageURI: str
    brightness_factor: float

    # Validator para o  brightness_factor
    @field_validator("brightness_factor")
    def validate_brightness_factor(cls, value: float):
        if not (0.0 <= value <= 2.0):
            raise ValueError("Brightness factor must be between 0.0 and 2.0.")
        return value

BrightnessRequestMessage = RequestMessage[BrightnessParameters]
