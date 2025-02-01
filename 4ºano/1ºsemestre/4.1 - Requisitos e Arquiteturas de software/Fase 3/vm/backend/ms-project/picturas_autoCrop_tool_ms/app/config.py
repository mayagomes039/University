import os

RABBITMQ_HOST = os.getenv("RABBITMQ_HOST", "localhost")
RABBITMQ_PORT = os.getenv("RABBITMQ_PORT", 5672)
RABBITMQ_USER = os.getenv("RABBITMQ_USER", "guest")
RABBITMQ_PASS = os.getenv("RABBITMQ_PASS", "guest")

RABBITMQ_REQUESTS_QUEUE_NAME = os.getenv("RABBITMQ_REQUESTS_QUEUE_NAME", "autocrop-requests")

RABBITMQ_RESULTS_EXCHANGE = os.getenv("RABBITMQ_RESULTS_EXCHANGE", "picturas.tools")
RABBITMQ_RESULTS_ROUTING_KEY = os.getenv("RABBITMQ_RESULTS_ROUTING_KEY", "results")

PICTURAS_LOG_LEVEL = os.getenv("PICTURAS_LOG_LEVEL", "INFO")
PICTURAS_MS_NAME = os.getenv("PICTURAS_MS_NAME", "picturas-autocrop-tool-ms")
PICTURAS_NUM_THREADS = os.getenv("PICTURAS_NUM_THREADS", 4)

S3_ENDPOINT_URL = os.getenv("S3_ENDPOINT_URL", "http://localhost:9000")
S3_ACCESS_KEY   = os.getenv("S3_ACCESS_KEY", "root")
S3_SECRET_KEY   = os.getenv("S3_SECRET_KEY", "minioadmin")
S3_USE_SSL      = os.getenv("S3_USE_SSL", "false").lower() == "true"
S3_BUCKET_NAME  = os.getenv("S3_BUCKET_NAME", "my-bucket")