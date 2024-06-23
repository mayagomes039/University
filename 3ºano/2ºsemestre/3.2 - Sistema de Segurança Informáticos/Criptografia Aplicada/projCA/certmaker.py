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

with open("certs/MSG_CA.crt", "rb") as ca_cert_file:
    ca_cert = x509.load_pem_x509_certificate(ca_cert_file.read(), default_backend())

one_day = datetime.timedelta(1, 0, 0)

# Gerando a chave privada e pública
private_key = rsa.generate_private_key(
    public_exponent=65537,
    key_size=2048,
)
public_key = private_key.public_key()

# Construindo o certificado
builder = x509.CertificateBuilder()

# Informações do assunto (Subject)
builder = builder.subject_name(x509.Name([
    x509.NameAttribute(NameOID.COUNTRY_NAME, 'PT'),
    x509.NameAttribute(NameOID.STATE_OR_PROVINCE_NAME, 'Minho'),
    x509.NameAttribute(NameOID.LOCALITY_NAME, 'Braga'),
    x509.NameAttribute(NameOID.ORGANIZATION_NAME, 'Universidade do Minho'),
    x509.NameAttribute(NameOID.ORGANIZATIONAL_UNIT_NAME, 'SSI MSG RELAY SERVICE'),
    x509.NameAttribute(NameOID.COMMON_NAME, 'User 5 (SSI MSG Relay Client 5)'),
    x509.NameAttribute(NameOID.PSEUDONYM, 'MSG_CLI5')  
]))

# Informações do emissor (Issuer)
builder = builder.issuer_name(x509.Name([
    x509.NameAttribute(NameOID.COUNTRY_NAME, 'PT'),
    x509.NameAttribute(NameOID.STATE_OR_PROVINCE_NAME, 'Minho'),
    x509.NameAttribute(NameOID.LOCALITY_NAME, 'Braga'),
    x509.NameAttribute(NameOID.ORGANIZATION_NAME, 'Universidade do Minho'),
    x509.NameAttribute(NameOID.ORGANIZATIONAL_UNIT_NAME, 'SSI MSG RELAY SERVICE'),
    x509.NameAttribute(NameOID.COMMON_NAME, 'MSG RELAY SERVICE CA'),
    x509.NameAttribute(NameOID.PSEUDONYM, 'MSG_CA') 
]))

builder = builder.not_valid_before(datetime.datetime.today() - one_day)
builder = builder.not_valid_after(datetime.datetime.today() + (one_day * 30))
builder = builder.serial_number(x509.random_serial_number())
builder = builder.public_key(public_key)
builder = builder.add_extension(
    x509.BasicConstraints(ca=False, path_length=None), critical=True,
)

builder = builder.add_extension(
    x509.KeyUsage(
        digital_signature=True,
        content_commitment=False,
        key_encipherment=False,
        data_encipherment=False,
        key_agreement=False,
        key_cert_sign=False,
        crl_sign=False,
        encipher_only=False,
        decipher_only=False
    ), critical=True,
)

builder = builder.add_extension(
    x509.ExtendedKeyUsage([
        x509.oid.ExtendedKeyUsageOID.CLIENT_AUTH,
    ]), critical=False,
)
# Salvar a chave privada em um arquivo .pem
with open("certs/private_key.pem", "wb") as key_file:
    key_file.write(private_key.private_bytes(
        encoding=serialization.Encoding.PEM,
        format=serialization.PrivateFormat.TraditionalOpenSSL,
        encryption_algorithm=serialization.NoEncryption()
    ))

with open("certs/CAkey.pem", "rb") as f: 
    key_raw = f.read()

key_ca = load_pem_private_key(key_raw, password=b"passphrase", backend=default_backend())


# Assinando o certificado com a chave privada
certificate = builder.sign(
    key_ca,  # Use a chave privada da CA para assinar
    algorithm=hashes.SHA256(),backend=default_backend()
)
    

# Salvar o certificado do cliente em um arquivo .pem
with open("certs/MSG_CLI5.crt", "wb") as cert_file:
    cert_file.write(certificate.public_bytes(serialization.Encoding.PEM))





with open("certs/private_key.pem", "rb") as key_file:
    private_key = serialization.load_pem_private_key(
        key_file.read(),
        password=None,  # Nenhuma senha para chave privada
        backend=default_backend()
    )

# Carregar o certificado do cliente
with open("certs/MSG_CLI5.crt", "rb") as cert_file:
    client_cert = x509.load_pem_x509_certificate(cert_file.read(), default_backend())



with open("certs/MY_MSG_CA.crt", "rb") as ca_cert_file:
    ca_cert = x509.load_pem_x509_certificate(ca_cert_file.read(), default_backend())

# Criar uma lista contendo o certificado do cliente e o certificado da CA
certificates = [client_cert, ca_cert]

# Criar um arquivo .p12
p12 = pkcs12.serialize_key_and_certificates(
    None,  # Nenhuma senha para proteger o arquivo .p12
    private_key,
    client_cert,
    certificates,
    serialization.NoEncryption()
)

# Salvar o arquivo .p12
with open("certs/MY_MSG_CLI5.p12", "wb") as p12_file:
    p12_file.write(p12)

