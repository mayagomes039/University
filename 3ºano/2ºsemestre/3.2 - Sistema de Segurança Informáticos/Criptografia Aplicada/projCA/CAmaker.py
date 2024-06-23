from cryptography import x509
from cryptography.hazmat.primitives import hashes
from cryptography.hazmat.primitives.asymmetric import rsa
from cryptography.x509.oid import NameOID
from cryptography.hazmat.primitives import serialization
from cryptography.x509 import Extension
from cryptography.x509.oid import ObjectIdentifier
import subprocess
from cryptography import x509
from cryptography.hazmat.primitives.serialization import BestAvailableEncryption, load_pem_private_key, pkcs12
from cryptography.hazmat.backends import default_backend
import datetime
import binascii


# ? GERAR KEY 
#genarate key 
key = rsa.generate_private_key(

    public_exponent=65537,

    key_size=2048,

)
#
## Write our key to disk for safe keeping
#
with open("certs/CAkey.pem", "wb") as f:

    f.write(key.private_bytes(
        encoding=serialization.Encoding.PEM,
        format=serialization.PrivateFormat.TraditionalOpenSSL,
        encryption_algorithm=serialization.BestAvailableEncryption(b"passphrase"),
    ))

#
# ? Ler KEY 
with open("certs/CAkey.pem", "rb") as f: 
    key_raw = f.read()
key = load_pem_private_key(key_raw, password=b"passphrase", backend=default_backend())


# Various details about who we are. For a self-signed certificate the

# subject and issuer are always the same.

subject = issuer = x509.Name([

    x509.NameAttribute(NameOID.COUNTRY_NAME, 'PT'),
    x509.NameAttribute(NameOID.STATE_OR_PROVINCE_NAME, 'Minho'),
    x509.NameAttribute(NameOID.LOCALITY_NAME, 'Braga'),
    x509.NameAttribute(NameOID.ORGANIZATION_NAME, 'Universidade do Minho'),
    x509.NameAttribute(NameOID.ORGANIZATIONAL_UNIT_NAME, 'SSI MSG RELAY SERVICE'),
    x509.NameAttribute(NameOID.COMMON_NAME, 'MSG RELAY SERVICE CA'),
    x509.NameAttribute(NameOID.PSEUDONYM, 'MSG_CA') 

])

cert = x509.CertificateBuilder().subject_name(

    subject

).issuer_name(

    issuer

).public_key(

    key.public_key()

).serial_number(

    x509.random_serial_number()

).not_valid_before(

    datetime.datetime.now(datetime.timezone.utc)

).not_valid_after(

    # Our certificate will be valid for 10 days

    datetime.datetime.now(datetime.timezone.utc) + datetime.timedelta(days=10)

).add_extension(

    x509.SubjectAlternativeName([x509.DNSName("MSG_CA")]),

    critical=False,

# Sign our certificate with our private key

).sign(key, hashes.SHA256())

# Write our certificate out to disk.

with open("certs/MY_MSG_CA.crt", "wb") as f:

    f.write(cert.public_bytes(serialization.Encoding.PEM))
