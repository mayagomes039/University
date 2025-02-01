from typing import Optional
from pydantic import BaseModel, field_validator

from .core.messages.request_message import RequestMessage


class ContrastParameters(BaseModel):
    inputImageURI: str
    outputImageURI: str
    contrast_factor: float 

    # Validador para o contrast_factor 
    @field_validator("contrast_factor")
    def validate_contrast_factor(cls, value: float):
        if not (0.0 <= value <= 2.0):
            raise ValueError("Contrast factor must be between 0.0 and 2.0.")
        return value

ContrastRequestMessage = RequestMessage[ContrastParameters]
