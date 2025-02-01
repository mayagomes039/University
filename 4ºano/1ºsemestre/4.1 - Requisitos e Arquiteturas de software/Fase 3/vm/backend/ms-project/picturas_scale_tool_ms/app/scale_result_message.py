from typing import Any

from pydantic import BaseModel

from .core.messages.result_message import ResultMessage
from .scale_request_message import ScaleRequestMessage


class ScaleResultOutput(BaseModel):
    type: str
    imageURI: str


class ScaleResultMessage(ResultMessage[ScaleResultOutput]):

    def __init__(self, request: ScaleRequestMessage, tool_result: Any, exception: Exception, *args):
        super().__init__(request, tool_result, exception, *args)
        if exception is None:
            self.output = tool_result
