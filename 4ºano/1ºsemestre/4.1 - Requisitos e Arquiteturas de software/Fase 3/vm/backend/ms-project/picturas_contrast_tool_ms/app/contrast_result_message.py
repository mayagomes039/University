from typing import Any

from pydantic import BaseModel

from .core.messages.result_message import ResultMessage
from .contrast_request_message import ContrastRequestMessage


class ContrastResultOutput(BaseModel):
    type: str
    imageURI: str


class ContrastResultMessage(ResultMessage[ContrastResultOutput]):

    def __init__(self, request: ContrastRequestMessage, tool_result: Any, exception: Exception, *args):
        super().__init__(request, tool_result, exception, *args)
        if exception is None:
            self.output = tool_result
