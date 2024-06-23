import asyncio
import socket
import os
import suportfuncs
from cryptography.hazmat.primitives.ciphers.aead import AESGCM
from cryptography.hazmat.primitives import hashes
from cryptography.hazmat.primitives.asymmetric import dh
from cryptography.hazmat.primitives.kdf.hkdf import HKDF
from cryptography.hazmat.primitives import serialization
from cryptography.hazmat.primitives.serialization import load_pem_public_key
from cryptography.hazmat.primitives.asymmetric import padding
from cryptography import x509
from cryptography.hazmat.backends import default_backend
from cryptography.hazmat.primitives.serialization import Encoding
import re
import sys
import argparse

# Connection Settings
conn_port = 8443
max_msg_size = 9999

# DH Parameters
p = 0xFFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD129024E088A67CC74020BBEA63B139B22514A08798E3404DDEF9519B3CD3A431B302B0A6DF25F14374FE1356D6D51C245E485B576625E7EC6F44C42E9A637ED6B0BFF5CB6F406B7EDEE386BFB5A899FA5AE9F24117C4B1FE649286651ECE45B3DC2007CB8A163BF0598DA48361C55D39A69163FA8FD24CF5F83655D23DCA3AD961C62F356208552BB9ED529077096966D670C354E4ABC9804F1746C08CA18217C32905E462E36CE3BE39E772C180E86039B2783A2EC07A28FB5C55DF06F4C52C9DE2BCBF6955817183995497CEA956AE515D2261898FA051015728E5A8AACAA68FFFFFFFFFFFFFFFF
g = 2
pn = dh.DHParameterNumbers(p, g)
parameters = pn.parameters()


def print_cert_details(cert_pem):
    """
    Imprime os detalhes relevantes de um certificado dado em formato PEM.
    
    Args:
        cert_pem (bytes): O certificado serializado em formato PEM.
    """
    cert = x509.load_pem_x509_certificate(cert_pem, default_backend())
    subject = cert.subject
    for attr in subject:
        if attr.oid == x509.NameOID.COMMON_NAME:
            CN = attr.value
        elif attr.oid == x509.NameOID.ORGANIZATIONAL_UNIT_NAME:
            OU = attr.value
        elif attr.oid == x509.ObjectIdentifier('2.5.4.65'):
            PSEUDONYM = attr.value
    
    print("USER DATA: \n")
    print("UID:", PSEUDONYM)
    print("CN:", CN)
    print("OU:", OU)
    print()


class Client:
    """ Classe que implementa a funcionalidade de um CLIENTE. """
    def __init__(self, sckt,user_name=None):
        """ Construtor da classe. """
        self.sckt = sckt
        self.msg_cnt = 0
        self.private_key = parameters.generate_private_key()
        self.public_key = self.private_key.public_key()
        self.aesgcm = None
        self.flag = 0 
        self.executed = False

        self.user_file = user_name
        self.UID = None
        self.CN = None
        self.OU = None

    def process(self, msg=b"", newMSG=""):
        """ Processa uma mensagem (`bytestring`) enviada pelo SERVIDOR.
            Retorna a mensagem a transmitir como resposta (`None` para
            finalizar ligação) """
        self.msg_cnt +=1
        if self.msg_cnt == 1:
            pem = self.public_key.public_bytes(encoding=serialization.Encoding.PEM,format=serialization.PublicFormat.SubjectPublicKeyInfo)
            return pem
        
        if self.msg_cnt == 2:
            publicMIXsignature, ctrserver = suportfuncs.unpair(msg)
            server_public_key, server_signatura = suportfuncs.unpair(publicMIXsignature)
            pem = self.public_key.public_bytes(encoding=serialization.Encoding.PEM,format=serialization.PublicFormat.SubjectPublicKeyInfo)
            cert = x509.load_pem_x509_certificate(ctrserver, default_backend())
            is_valid = suportfuncs.valida_certCliente("certs/MY_MSG_CA.crt", cert)
            if not is_valid:
                sys.stderr.write("MSG RELAY SERVICE: verification error2!\n")
                sys.exit(1) 

            data = suportfuncs.mkpair(server_public_key, pem)
            server_cert = x509.load_pem_x509_certificate(ctrserver)
            server = server_cert.public_key()
            try:
                server.verify(
                    server_signatura,
                    data,
                    padding.PSS(
                        mgf=padding.MGF1(hashes.SHA256()),
                        salt_length=padding.PSS.MAX_LENGTH
                    ),
                    hashes.SHA256()
                )
            except: 
                sys.stderr.write("MSG RELAY SERVICE: verification error1!\n")
                sys.exit(1) 

            server_public_key_desserializado = load_pem_public_key(server_public_key)
            shared_key = self.private_key.exchange(server_public_key_desserializado)
            derived_key = HKDF(algorithm=hashes.SHA256(), length=32, salt=None, info=b'handshake data',).derive(shared_key)
            self.aesgcm = AESGCM(derived_key)

            message = suportfuncs.mkpair(pem, server_public_key)
            private_key, User_cert, _ = suportfuncs.get_userdata(self.user_file)

            user_cert_pem = User_cert.public_bytes(Encoding.PEM)
            signature = private_key.sign(message,padding.PSS(mgf=padding.MGF1(hashes.SHA256()),salt_length=padding.PSS.MAX_LENGTH),hashes.SHA256())
            final_pair = suportfuncs.mkpair(signature,user_cert_pem)
            print_cert_details(user_cert_pem)
          
            return final_pair

        if self.flag == 1 and msg != b'':
            nonce = msg[:12]
            ciphertext = msg[12:]
            msg = self.aesgcm.decrypt(nonce, ciphertext, None)
            
        if msg.startswith(b"["):
            msg = msg.strip(b"['']")
            mensagens = msg.split(b"), (")  
            rotulos = ["Msg number", "Sender", "Tempo", "Subject"]
        
            for idx, mensagem in enumerate(mensagens):
                if idx == 0:
                    mensagem = mensagem[1:]  
        
                campos = mensagem.split(b", ")
                num_iteracoes = min(len(campos), len(rotulos))
                for i in range(num_iteracoes):
                    print(f"{rotulos[i]} = {campos[i].decode()}")
                print()  

        else:
            print(f'Received ({self.msg_cnt - 2}):', msg.decode())

        self.flag = 1
        new_msg = newMSG.encode()
        
        if new_msg == b'' or self.executed == True: 
            return None
        
        if new_msg != b'':
            if new_msg.startswith(b"user"):
                    arguments = new_msg.split()[1:]
                    if arguments:
                        self.user_file = arguments[0]
                    else:
                        self.user_file = "userdata.p12"
    
                    _, User_cert,_=suportfuncs.get_userdata(self.user_file)
                    user_cert_pem = User_cert.public_bytes(Encoding.PEM)
                    print_cert_details(user_cert_pem)

            if new_msg.startswith(b"send"):
                        while True:
                            if new_msg.startswith(b"send"):
                                arguments = new_msg.split()[1:]

                                if len(arguments) == 2:
                                    user_UID = arguments[0]
                                    send_Subjet = arguments[1]
                                    print("Texto da mensagem:")
                                    text = input()[:1000].encode('utf-8')
                                    
                                    new_msg = new_msg + b" " +text


                                    private_key, User_cert, _ = suportfuncs.get_userdata(self.user_file)
                                    new_msgAssinada = private_key.sign(new_msg,padding.PSS(mgf=padding.MGF1(hashes.SHA256()),salt_length=padding.PSS.MAX_LENGTH),hashes.SHA256())
                                    sendRes = suportfuncs.mkpair(new_msgAssinada,new_msg)
                                    nonce = os.urandom(12)
                                    ciphertext = self.aesgcm.encrypt(nonce, sendRes, None )
                                    SendRes = (nonce + ciphertext)

                                    
                                    self.executed = True
                                    return SendRes
                                
                                else:
                                    print("Número de argumentos inválido. Use: send <UID> <Assunto>")
                                    new_msg = input().encode()
            if msg.startswith(b"["):
                msg = msg.strip(b"['']")
                nonce = msg[:12]
                ciphertext = msg[12:]
                decrypted_msg = self.aesgcm.decrypt(nonce, ciphertext, None).decode()
                received_tuple = eval(decrypted_msg)
                for item in received_tuple:
                    print(item)
                print()
            if new_msg.startswith(b"help"):
                help = "help"
                help_msg = "\n Comandos disponíveis:\n - send <user_UID> <subject> <message_content>: Envia uma mensagem ao usuário com o ID especificado.\n - askqueue: Verifica se há mensagens não lidas.\n - getmsg <message_number>: Obtém uma mensagem específica pelo número da mensagem.\n - help: Exibe esta mensagem de ajuda.\n"
                helper = suportfuncs.mkpair(help.encode(), help_msg.encode())
                nonce = os.urandom(12)
                ciphertext = self.aesgcm.encrypt(nonce, helper, None)
                self.executed = True
                return (nonce + ciphertext)

        nonce = os.urandom(12)
        new_msg2 = b"-_-"
        hec = suportfuncs.mkpair(new_msg2, new_msg)
        ciphertext = self.aesgcm.encrypt(nonce, hec, None )
        new_msg = (nonce + ciphertext)
        self.executed = True

        return new_msg if len(new_msg) > 0 else None

async def tcp_echo_client(user_file, command, aux):
    reader, writer = await asyncio.open_connection('127.0.0.1', conn_port)
    addr = writer.get_extra_info('peername')
    client = Client(addr,user_file)
    msg = client.process()
    newMSG = command + " " + " ".join(aux)
    while msg:
        writer.write(msg)
        msg = await reader.read(max_msg_size)
        if msg :
            msg = client.process(msg, newMSG)
        else:
            break

    writer.write(b'\n')
    print('Socket closed!')
    writer.close()

def run_client(user_file, command, aux):
    loop = asyncio.get_event_loop()
    loop.run_until_complete(tcp_echo_client(user_file, command, aux))

def printHelp():
    #print("Usage: python3 msg_client.py [-user <user_config_file>] <send <UID> <SUBJECT> | askqueue | getmsg <NUM>") 
    #msg que tinhamos feito    
    print("\n Comandos disponíveis:\n - send <user_UID> <subject>: Envia uma mensagem ao usuário com o ID especificado.\n - askqueue: Verifica se há mensagens não lidas.\n - getmsg <message_number>: Obtém uma mensagem específica pelo número da mensagem.\n - help: Exibe esta mensagem de ajuda.\n")

def parse_args(args):
    if len(args) == 0:
        sys.stderr.write("MSG RELAY SERVICE: command error!\n")
        printHelp()
        sys.exit(1)

    if args[0] == "help":
        printHelp()
        sys.exit(0)

    try: 
        idx = args.index("-user") 
    except:
        idx = -1
    args_ = {}
    args_["userConfigfile"] = "certs/userdata.p12"
    if idx != -1:
        if idx == 0 and len(args) >= 3:
            args_["userConfigfile"] = args[1]
            args = args[2:]
        else:
            sys.stderr.write("MSG RELAY SERVICE: command error!\n") 
            printHelp()
            sys.exit(1)
    
    if args[0] == "send":
        if len(args) == 3:
            args_["command"] = "send"
            args_["aux"] = args[1:]
        else:
            sys.stderr.write("MSG RELAY SERVICE: command error!\n")
            printHelp()
            sys.exit(1)
    elif args[0] == "askqueue":
        if len(args) == 1:
            args_["command"] = "askqueue"
            args_["aux"] = []
        else:
            sys.stderr.write("MSG RELAY SERVICE: command error!\n")
            printHelp()
            sys.exit(1)
    elif args[0] == "getmsg":
        if len(args)==2:
            args_["command"] = "getmsg"
            args_["aux"]= args[1:]
        else:
            sys.stderr.write("MSG RELAY SERVICE: command error!\n")
            printHelp()
            sys.exit(1)
    else:
        sys.stderr.write("MSG RELAY SERVICE: command error!\n")
        printHelp()
        sys.exit(1)
    return args_


if __name__ == "__main__":
    args_ = parse_args(sys.argv[1:])

    run_client(args_.get("userConfigfile"), args_.get("command"), args_.get("aux"))
