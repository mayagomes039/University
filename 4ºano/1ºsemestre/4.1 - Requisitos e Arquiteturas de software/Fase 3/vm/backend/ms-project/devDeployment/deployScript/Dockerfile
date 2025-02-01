FROM python:3.13-slim

RUN pip install pika
RUN pip install boto3

COPY main.py ./
COPY S3Facade_Shared.py ./

ENTRYPOINT ["python", "main.py"]
