from typing import Any

from pydantic import BaseModel

from .core.messages.result_message import ResultMessage
from .autoCrop_request_message import AutoCropRequestMessage

class AutoCropResultOutput(BaseModel):
    type: str
    imageURI: str

class AutoCropResultMessage(ResultMessage[AutoCropResultOutput]):

    def __init__(self, request: AutoCropRequestMessage, tool_result: Any, exception: Exception, *args):
        super().__init__(request, tool_result, exception, *args)
        if exception is None:
            self.output = tool_result
