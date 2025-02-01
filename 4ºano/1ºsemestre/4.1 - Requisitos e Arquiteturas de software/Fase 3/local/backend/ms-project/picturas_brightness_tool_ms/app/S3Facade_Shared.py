import boto3
from botocore.exceptions import NoCredentialsError, PartialCredentialsError
import mimetypes


class S3Facade:
    def __init__(self, endpoint_url, access_key, secret_key, use_ssl=False):
        """
        Initialize the S3Facade with a custom endpoint and credentials.

        Args:
            endpoint_url (str): The URL of the S3 endpoint.
            access_key (str): The access key for authentication.
            secret_key (str): The secret key for authentication.
            use_ssl (bool): Whether to use SSL.
        """
        try:
            self.s3_client = boto3.client(
                "s3",
                endpoint_url=endpoint_url,
                aws_access_key_id=access_key,
                aws_secret_access_key=secret_key,
                config=boto3.session.Config(signature_version='s3v4'),
                use_ssl=use_ssl
            )
        except ImportError as e:
            print("Failed to import SSL or related modules. Ensure compatibility with your urllib3 and boto3 versions.")
            raise e

    def create_bucket_if_not_exists(self, bucket_name):
        """
        Create a bucket if it does not already exist.

        Args:
            bucket_name (str): The name of the bucket to create.
        """
        try:
            if bucket_name not in [bucket["Name"] for bucket in self.s3_client.list_buckets()["Buckets"]]:
                self.s3_client.create_bucket(Bucket=bucket_name)
                print(f"Bucket '{bucket_name}' created.")
                
                self.s3_client.put_bucket_acl(
                    Bucket=bucket_name,
                    ACL="public-read"  # Sets the bucket to public
                )
                
                print(f"Bucket '{bucket_name}' is now public.")
            else:
                print(f"Bucket '{bucket_name}' already exists.")
        except Exception as e:
            print(f"An error occurred while creating bucket: {e}")

  


    def upload_file(self, bucket_name, local_file, target_file):
        """
        Upload a file to the specified bucket.
    
        Args:
            bucket_name (str): The name of the bucket.
            local_file (str): The path to the local file to upload.
            target_file (str): The name of the file in the bucket.
    
        Returns:
            str: The path of the uploaded file in the bucket.
        """
        try:
            # Determine the content type of the file
            content_type, _ = mimetypes.guess_type(local_file)
            if not content_type:
                content_type = "application/octet-stream"  # Default fallback
    
            # Upload the file with the specified content type
            self.s3_client.upload_file(
                local_file,
                bucket_name,
                target_file,
                ExtraArgs={"ContentType": content_type}
            )
            print(f"File '{local_file}' uploaded as '{target_file}' with content type '{content_type}'.")
            return f"{bucket_name}/{target_file}"
        except Exception as e:
            print(f"An error occurred while uploading the file: {e}")
            raise e

    def file_exists(self, bucket_name, target_file):
        """
        Check if a file exists in the specified bucket.

        Args:
            bucket_name (str): The name of the bucket.
            target_file (str): The name of the file to check.

        Returns:
            bool: True if the file exists, False otherwise.
        """
        try:
            self.s3_client.head_object(Bucket=bucket_name, Key=target_file)
            print(f"File '{target_file}' exists in bucket '{bucket_name}'.")
            return True
        except self.s3_client.exceptions.ClientError as e:
            if e.response['Error']['Code'] == "404":
                print(f"File '{target_file}' does not exist in bucket '{bucket_name}'.")
                return False
            else:
                print(f"An error occurred: {e}")
                return False

    def download_file(self, bucket_name, target_file, download_path):
        """
        Download a file from the specified bucket.

        Args:
            bucket_name (str): The name of the bucket.
            target_file (str): The name of the file in the bucket.
            download_path (str): The local path to save the downloaded file.

        Returns:
            str: The path to the downloaded file.
        """
        try:
            self.s3_client.download_file(bucket_name, target_file, download_path)
            print(f"File '{target_file}' downloaded as '{download_path}'.")
            return download_path
        except Exception as e:
            print(f"An error occurred while downloading the file: {e}")
            raise e

    def list_bucket_objects(self, bucket_name):
        """
        List all objects in the specified bucket.

        Args:
            bucket_name (str): The name of the bucket.

        Returns:
            list: A list of object keys in the bucket.
        """
        try:
            objects = self.s3_client.list_objects_v2(Bucket=bucket_name).get("Contents", [])
            object_keys = [obj["Key"] for obj in objects]
            print(f"Objects in bucket '{bucket_name}': {object_keys}")
            return object_keys
        except Exception as e:
            print(f"An error occurred while listing objects: {e}")
            raise e
        
    def delete_file(self, bucket_name, target_file):
        """
        Delete a file from the specified bucket.

        Args:
            bucket_name (str): The name of the bucket.
            target_file (str): The name of the file to delete.

        Returns:
            bool: True if the file was successfully deleted, False otherwise.
        """
        try:
            self.s3_client.delete_object(Bucket=bucket_name, Key=target_file)
            print(f"File '{target_file}' deleted from bucket '{bucket_name}'.")
            return True
        except self.s3_client.exceptions.ClientError as e:
            if e.response['Error']['Code'] == "404":
                print(f"File '{target_file}' not found in bucket '{bucket_name}'.")
                return False
            else:
                print(f"An error occurred while deleting the file: {e}")
                raise e


# Example usage
if __name__ == "__main__":
    # Initialize the facade
    s3_facade = S3Facade(endpoint_url="http://localhost:9000", access_key="minioadmin", secret_key="minioadmin", use_ssl=False)

    # Example bucket and file details
    bucket_name = "my-bucket"
    example_file = "example.txt"
    target_file = "uploaded_example.txt"
    download_path = "downloaded_example.txt"

    # Create an example file
    with open(example_file, "w") as f:
        f.write("This is an example file.")

    # Create bucket
    s3_facade.create_bucket_if_not_exists(bucket_name)

    # Upload file
    s3_facade.upload_file(bucket_name, example_file, target_file)

    # Check if file exists
    s3_facade.file_exists(bucket_name, target_file)

    # List objects in bucket
    s3_facade.list_bucket_objects(bucket_name)

    # Download file
    s3_facade.download_file(bucket_name, target_file, download_path)
