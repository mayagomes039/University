from typing import Optional
from pydantic import BaseModel, field_validator

from .core.messages.request_message import RequestMessage


class ScaleParameters(BaseModel):
    inputImageURI: str
    outputImageURI: str
    new_width: int
    new_height: int

    # Validador para new_width
    @field_validator("new_width")
    def validate_new_width(cls, value: int):
        if value <= 0:
            raise ValueError("New width must be a positive integer greater than 0.")
        return value

    # Validador para new_height
    @field_validator("new_height")
    def validate_new_height(cls, value: int):
        if value <= 0:
            raise ValueError("New height must be a positive integer greater than 0.")
        return value


ScaleRequestMessage = RequestMessage[ScaleParameters]
