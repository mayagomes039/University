from typing import Any

from pydantic import BaseModel

from .core.messages.result_message import ResultMessage
from .brightness_request_message import BrightnessRequestMessage


class BrightnessResultOutput(BaseModel):
    type: str
    imageURI: str


class BrightnessResultMessage(ResultMessage[BrightnessResultOutput]):

    def __init__(self, request: BrightnessRequestMessage, tool_result: Any, exception: Exception, *args):
        super().__init__(request, tool_result, exception, *args)
        if exception is None:
            self.output = tool_result
