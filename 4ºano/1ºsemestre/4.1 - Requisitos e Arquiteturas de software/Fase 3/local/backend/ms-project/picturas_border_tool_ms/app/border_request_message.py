from typing import Tuple
from pydantic import BaseModel, field_validator, root_validator


from .core.messages.request_message import RequestMessage

class BorderParameters(BaseModel):
    inputImageURI: str
    outputImageURI: str
    border_width: int
    r: int
    g: int
    b: int
    border_color: Tuple[int, int, int] = None  

    # Validator para border_width
    @field_validator("border_width")
    def validate_border_width(cls, value: int):
        if value <= 0:
            raise ValueError("Border width must be greater than 0.")
        return value

    # Validator para os componentes de cor
    @field_validator("r", "g", "b")
    def validate_color_component(cls, value: int, info):
        if not (0 <= value <= 255):
            raise ValueError(f"{info.field_name} must be between 0 and 255.")
        return value

    # Root validator para montar border_color
    @root_validator(skip_on_failure=True)
    def assemble_border_color(cls, values):
        r, g, b = values.get("r"), values.get("g"), values.get("b")
        if r is not None and g is not None and b is not None:
            values["border_color"] = (r, g, b)
        return values


BorderRequestMessage = RequestMessage[BorderParameters]