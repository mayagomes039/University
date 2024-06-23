
from cryptography import x509
from cryptography.hazmat.primitives.serialization import pkcs12


import datetime


def get_public_key_from_p12(p12_fname):
    with open(p12_fname, "rb") as f:
        p12 = f.read()
    password = None  # Se o arquivo .p12 não estiver protegido por senha
    (private_key, user_cert, [ca_cert]) = pkcs12.load_key_and_certificates(p12, password)
    public_key = user_cert.public_key()
    return public_key

def mkpair(x, y):
    """produz uma byte-string contendo o tuplo '(x,y)' ('x' e 'y' são byte-strings)"""
    len_x = len(x)
    len_x_bytes = len_x.to_bytes(2, "little")
    return len_x_bytes + x + y

def unpair(xy):
    """extrai componentes de um par codificado com 'mkpair'"""
    len_x = int.from_bytes(xy[:2], "little")
    x = xy[2 : len_x + 2]
    y = xy[len_x + 2 :]
    return x, y

def get_userdata(p12_fname):
    with open(p12_fname, "rb") as f:
        p12 = f.read()
    password = None # p12 não está protegido...
    (private_key, user_cert, ca_certs) = pkcs12.load_key_and_certificates(p12, password)
    ca_cert = ca_certs[0] if ca_certs else None

    return (private_key, user_cert, ca_cert)

def cert_load(fname):
    """lê certificado de ficheiro"""
    with open(fname, "rb") as fcert:
        cert = x509.load_pem_x509_certificate(fcert.read())
    return cert


def cert_validtime(cert, now=None):
    """valida que 'now' se encontra no período
    de validade do certificado."""
    if now is None:
        now = datetime.datetime.now(tz=datetime.timezone.utc)
    if now < cert.not_valid_before_utc or now > cert.not_valid_after_utc:
        raise x509.verification.VerificationError(
            "Certificate is not valid at this time"
        )


def cert_validsubject(cert, attrs=[]):
    """verifica atributos do campo 'subject'. 'attrs'
    é uma lista de pares '(attr,value)' que condiciona
    os valores de 'attr' a 'value'."""
    #print(cert.subject)
    for attr in attrs:
        if cert.subject.get_attributes_for_oid(attr[0])[0].value != attr[1]:
            raise x509.verification.VerificationError(
                "Certificate subject does not match expected value"
            )


def cert_validexts(cert, policy=[]):
    """valida extensões do certificado.
    'policy' é uma lista de pares '(ext,pred)' onde 'ext' é o OID de uma extensão e 'pred'
    o predicado responsável por verificar o conteúdo dessa extensão."""
    for check in policy:
        ext = cert.extensions.get_extension_for_oid(check[0]).value
        if not check[1](ext):
            raise x509.verification.VerificationError(
                "Certificate extensions does not match expected value"
            )


def valida_certCliente(ca_cert, server_cert):
    return True
    try:
        #cert = server_cert
        # obs: pressupõe que a cadeia de certifica só contém 2 níveis
        #ca_cert1 = cert_load(ca_cert)
        #cert.verify_directly_issued_by(ca_cert1)

        # verificar período de validade...
        #cert_validtime(cert)

        # verificar identidade... (e.g.)
        #cert_validsubject(cert, [(x509.NameOID.COMMON_NAME, "SSI Message Relay Server")])

        # verificar aplicabilidade... (e.g.)       

        # cert_validexts(
        #     cert,
        #     [
        #         (
        #             x509.ExtensionOID.EXTENDED_KEY_USAGE,
        #             lambda e: x509.oid.ExtendedKeyUsageOID.CLIENT_AUTH in e,
        #         )
        #     ],
        # )
        #print("Certificate is valid!")
        return True
    except:
        #print("Certificate is invalid!")
        return False
    


def valida_certServer(ca_cert, client_cert):
    try:
    
        cert = client_cert
        # obs: pressupõe que a cadeia de certifica só contém 2 níveis
        ca_cert1 = cert_load(ca_cert)
        
        cert.verify_directly_issued_by(ca_cert1)
    
        # verificar período de validade...
        cert_validtime(cert)
        
        # verificar identidade... (e.g.)
        cert_validsubject(cert, [(x509.NameOID.ORGANIZATIONAL_UNIT_NAME, "SSI MSG RELAY SERVICE")])

        # verificar aplicabilidade... (e.g.)       

        # cert_validexts(
        #     cert,
        #     [
        #         (
        #             x509.ExtensionOID.EXTENDED_KEY_USAGE,
        #             lambda e: x509.oid.ExtendedKeyUsageOID.CLIENT_AUTH in e,
        #         )
        #     ],
        # )
        #print("Certificate is valid!")
        return True
    except:
        print("Certificate is invalid!")
        return False
    