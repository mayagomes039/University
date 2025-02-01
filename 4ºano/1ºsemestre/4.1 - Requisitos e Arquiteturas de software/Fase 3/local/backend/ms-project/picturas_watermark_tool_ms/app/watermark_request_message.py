from typing import Optional
from pydantic import BaseModel, field_validator

from .core.messages.request_message import RequestMessage


class WatermarkParameters(BaseModel):
    inputImageURI: str
    outputImageURI: str
    watermarkImageURI: str
    scale_factor: float
    opacity: float
    positionX: int
    positionY: int

    # Validador para scale_factor
    @field_validator("scale_factor")
    def validate_scale_factor(cls, value: float):
        if value <= 0:
            raise ValueError("Scale factor must be greater than 0.")
        return value

    # Validador para opacity
    @field_validator("opacity")
    def validate_opacity(cls, value: float):
        if not (0.0 <= value <= 1.0):
            raise ValueError("Opacity must be between 0.0 and 1.0.")
        return value

    # Validador para positionX
    @field_validator("positionX")
    def validate_positionX(cls, value: int):
        if value < 0:
            raise ValueError("PositionX must be greater than or equal to 0.")
        return value

    # Validador para positionY
    @field_validator("positionY")
    def validate_positionY(cls, value: int):
        if value < 0:
            raise ValueError("PositionY must be greater than or equal to 0.")
        return value


WatermarkRequestMessage = RequestMessage[WatermarkParameters]
