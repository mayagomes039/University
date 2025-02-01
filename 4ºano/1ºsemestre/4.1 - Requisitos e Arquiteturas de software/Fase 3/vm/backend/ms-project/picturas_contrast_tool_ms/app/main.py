import logging

from .config import PICTURAS_LOG_LEVEL
from .core.message_processor import MessageProcessor
from .core.message_queue_setup import message_queue_connect
from .contrast_request_message import ContrastRequestMessage
from .contrast_result_message import ContrastResultMessage
from .contrast_tool import ContrastTool

# Logging setup
LOG_FORMAT = '%(asctime)s [%(levelname)s] %(message)s'
logging.basicConfig(level=PICTURAS_LOG_LEVEL, format=LOG_FORMAT)

LOGGER = logging.getLogger(__name__)

if __name__ == "__main__":
    connection, channel = message_queue_connect()


    # Instancie a ferramenta BorderTool com os parâmetros
    tool = ContrastTool()
    request_msg_class = ContrastRequestMessage
    result_msg_class = ContrastResultMessage

    message_processor = MessageProcessor(tool, request_msg_class, result_msg_class, channel)

    try:
        message_processor.start()
    except KeyboardInterrupt:
        message_processor.stop()

    connection.close()
