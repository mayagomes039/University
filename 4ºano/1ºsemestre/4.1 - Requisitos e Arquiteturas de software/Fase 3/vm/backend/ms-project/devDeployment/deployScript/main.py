import logging
import os
import signal
import pika
from pika.exchange_type import ExchangeType

from S3Facade_Shared import S3Facade

RABBITMQ_HOST = os.getenv("RABBITMQ_HOST", "localhost")
PICTURAS_LOG_LEVEL = os.getenv("PICTURAS_LOG_LEVEL", "INFO")

LOG_FORMAT = '%(asctime)s [%(levelname)s] %(message)s'
logging.basicConfig(level=PICTURAS_LOG_LEVEL, format=LOG_FORMAT)

S3_ENDPOINT_URL = os.getenv("S3_ENDPOINT_URL", "http://localhost:9000")
S3_ACCESS_KEY   = os.getenv("S3_ACCESS_KEY", "root")
S3_SECRET_KEY   = os.getenv("S3_SECRET_KEY", "minioadmin")
S3_USE_SSL      = os.getenv("S3_USE_SSL", "false").lower() == "true"
S3_BUCKET_NAME  = os.getenv("S3_BUCKET_NAME", "my-bucket")

LOGGER = logging.getLogger(__name__)

def message_queue_connect():
    LOGGER.info("Connecting to RabbitMQ...")
    connection = pika.BlockingConnection(
        pika.ConnectionParameters(
            host=RABBITMQ_HOST,
            heartbeat=0,  # Disable heartbeat to prevent timeouts
            blocked_connection_timeout=None  # Prevent blocking timeouts
        )
    )
    channel = connection.channel()
    LOGGER.info("Connection established.")
    return connection, channel

def message_queue_setup(channel):
    LOGGER.info("Setting up queues and bindings...")
    channel.exchange_declare(
        exchange="picturas.tools",
        exchange_type=ExchangeType.direct,
        durable=True,
    )
    
    queues = [
        ("results", "results"),
        ("watermark-requests", "requests.watermark"),
        ("border-requests", "requests.border"),
        ("brightness-requests", "requests.brightness"),
        ("contrast-requests", "requests.contrast"),
        ("rotate-requests", "requests.rotate"),
        ("scale-requests", "requests.scale"),
        ("crop-requests", "requests.crop"),
        ("removebg-requests", "requests.removebg"),
        ("autocrop-requests", "requests.autocrop")
    ]
    
    for queue_name, routing_key in queues:
        channel.queue_declare(queue=queue_name)
        channel.queue_bind(
            queue=queue_name,
            exchange="picturas.tools",
            routing_key=routing_key,
        )
        LOGGER.info(f"Queue '{queue_name}' bound to routing key '{routing_key}'.")

def main():
    connection, channel = message_queue_connect()
    try:
        message_queue_setup(channel)
        
        s3_facade = S3Facade(endpoint_url=S3_ENDPOINT_URL, access_key=S3_ACCESS_KEY, secret_key=S3_SECRET_KEY, use_ssl=S3_USE_SSL)
    
        # Create bucket
        logging.info(f"Creating bucket '{S3_BUCKET_NAME}' if it does not exist...")
        s3_facade.create_bucket_if_not_exists(S3_BUCKET_NAME)
        logging.info(f"Bucket '{S3_BUCKET_NAME}' is ready.")
        LOGGER.info("Setup complete. Waiting indefinitely...")

        signal.pause()  # Aguarda sinal para continuar ou finalizar
    except KeyboardInterrupt:
        LOGGER.info("Shutdown requested by user.")
    finally:
        LOGGER.info("Closing connection...")
        connection.close()
        LOGGER.info("Connection closed.")

if __name__ == "__main__":
    main()
