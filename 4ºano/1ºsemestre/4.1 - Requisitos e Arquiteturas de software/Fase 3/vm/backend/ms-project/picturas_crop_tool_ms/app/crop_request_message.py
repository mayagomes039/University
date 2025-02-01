from pydantic import BaseModel, field_validator

from .core.messages.request_message import RequestMessage

class CropParameters(BaseModel):
    inputImageURI: str
    outputImageURI: str
    left: int
    upper: int
    right: int
    lower: int

    # Validator for right > left
    @field_validator("right")
    def validate_right(cls, right, info):
        left = info.data.get("left")  # Use `info.data` para acessar outros valores
        if left is not None and right <= left:
            raise ValueError("Right must be greater than left.")
        return right

    # Validator for lower > upper
    @field_validator("lower")
    def validate_lower(cls, lower, info):
        upper = info.data.get("upper")  # Use `info.data` para acessar outros valores
        if upper is not None and lower <= upper:
            raise ValueError("Lower must be greater than upper.")
        return lower

CropRequestMessage = RequestMessage[CropParameters]