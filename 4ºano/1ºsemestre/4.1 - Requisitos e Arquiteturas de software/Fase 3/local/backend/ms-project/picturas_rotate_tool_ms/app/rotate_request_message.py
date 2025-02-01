from typing import Optional
from pydantic import BaseModel, field_validator

from .core.messages.request_message import RequestMessage


class RotateParameters(BaseModel):
    inputImageURI: str
    outputImageURI: str
    angle: int  

    # Validador para o campo "angle"
    @field_validator("angle")
    def validate_angle(cls, value: int):
        if not (0 <= value < 360):
            raise ValueError("Angle must be between 0 (inclusive) and 360 (exclusive).")
        return value


RotateRequestMessage = RequestMessage[RotateParameters]