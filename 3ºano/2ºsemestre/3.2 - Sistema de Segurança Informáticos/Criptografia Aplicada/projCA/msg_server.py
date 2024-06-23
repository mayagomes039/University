# Código baseado em https://docs.python.org/3.6/library/asyncio-stream.html#tcp-echo-client-using-streams
import asyncio
import queue
import os
import time
import json
import pickle
import sys
import hashlib
import suportfuncs
from pprint import pprint
from cryptography.hazmat.primitives.ciphers.aead import AESGCM
from cryptography.hazmat.primitives import hashes
from cryptography.hazmat.primitives.asymmetric import dh
from cryptography.hazmat.primitives.kdf.hkdf import HKDF
from cryptography.hazmat.primitives import serialization
from cryptography.hazmat.primitives.serialization import load_pem_public_key
from cryptography.hazmat.primitives import hashes
from cryptography.hazmat.primitives.asymmetric import padding
from cryptography import x509
from cryptography.hazmat.backends import default_backend

# Connection Settigns
import os

# print(os.getcwd())
conn_port = 8443
max_msg_size = 9999

conn_cnt = 0

# Parametros DH
p = 0xFFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD129024E088A67CC74020BBEA63B139B22514A08798E3404DDEF9519B3CD3A431B302B0A6DF25F14374FE1356D6D51C245E485B576625E7EC6F44C42E9A637ED6B0BFF5CB6F406B7EDEE386BFB5A899FA5AE9F24117C4B1FE649286651ECE45B3DC2007CB8A163BF0598DA48361C55D39A69163FA8FD24CF5F83655D23DCA3AD961C62F356208552BB9ED529077096966D670C354E4ABC9804F1746C08CA18217C32905E462E36CE3BE39E772C180E86039B2783A2EC07A28FB5C55DF06F4C52C9DE2BCBF6955817183995497CEA956AE515D2261898FA051015728E5A8AACAA68FFFFFFFFFFFFFFFF
g = 2
pn = dh.DHParameterNumbers(p, g)
parameters = pn.parameters()



with open("certs/MY_MSG_SERVER.key", "rb") as key_file:
    msg_server = serialization.load_pem_private_key(
        key_file.read(),
        password=None,
        backend=default_backend()
    )


class Server:
    def __init__(self):
        self.workers = {}
        self.message_queues = {}  # Dicionário para armazenar as filas de mensagens dos clientes
        self.read_messages = {}
        self.log_file = "server_log.txt"

    def add_worker(self, pseudonym, user_UID, worker):
        self.workers[pseudonym] = worker
        self.message_queues[user_UID] = queue.Queue()
        self.read_messages[user_UID] = queue.Queue()

    def get_worker_by_pseudonym(self, pseudonym):
        return self.workers.get(pseudonym)

    def get_message_queue_by_UID(self, user_UID):
        return self.message_queues.get(user_UID)

    def get_read_messages_by_UID(self, user_UID):
        return self.read_messages.get(user_UID)

    def log(self, typeColour, type, details):
        timestamp = time.strftime('%Y-%m-%d %H:%M:%S', time.localtime())
        log_message = f"{timestamp} - {type}: {details}"
        with open(self.log_file, "a") as f:
            f.write(log_message + "\n")

        log_message = f"\033[92m{timestamp}\033[97m - {typeColour}: {details}"
        print(log_message)

    def log_send(self, sender, recipient):
        self.log("\033[96mSEND MSG\033[97m", "SEND MSG", f"{sender} -> {recipient}")

    def log_askqueue(self, sender):
        self.log("\033[93mASKQUEUE\033[97m", "ASKQUEUE", f"{sender}")

    def log_get_msg(self, sender):
        self.log("\033[95mGET MSG\033[97m", "GET MSG", f"{sender}")

        
class ServerWorker(object):
    """ Classe que implementa a funcionalidade do SERVIDOR. """

    def __init__(self, cnt, addr=None, server=None, sender_public_key=None):
        """ Construtor da classe. """
        self.id = cnt
        self.addr = addr
        self.msg_cnt = 0
        self.msg_number = 0
        self.private_key = parameters.generate_private_key()
        self.public_key = self.private_key.public_key()
        self.peer_public_key1 = None
        self.peer_public_key = None
        self.public_encode_key = None
        self.aesgcm = None
        self.pseudonym = None
        self.signature_message = None
        self.server = server
        self.sender_public_key = sender_public_key

    def add_message_to_queue(self, sender, user_UID, msg_number, timestamp, send_subject, message_content, assinatura,txt):
        server_message_queue = self.server.get_message_queue_by_UID(user_UID.decode())
        if server_message_queue:
            server_message_queue.put((msg_number, sender, timestamp, send_subject, message_content, assinatura, txt))

    def get_messages(self):
        messages = self.server.get_message_queue_by_UID(self.pseudonym)
        return messages

    def get_unread_messages(self):
        # copia para nao eliminar da queue ao dar get:
        deque1 = self.get_messages().queue
        deque2 = self.server.get_read_messages_by_UID(self.pseudonym).queue
        #deque2 = self.read_messages.get(self.pseudonym, queue.Queue()).queue
        diferentes = [elemento for elemento in deque1 if elemento not in deque2]

        return diferentes

    def verify_message(self, msg):
        # Verificar autenticidade da mensagem utilizando hash
        hash_object = hashlib.sha256()
        hash_object.update(msg.encode())
        calculated_hash = hash_object.digest()

        return calculated_hash == self.sender_key

    def prepare_message(self, msg):
        # Preparar mensagem para envio ao cliente
        msg_to_send = str(msg)
        nonce = os.urandom(12)
        ciphertext = self.aesgcm.encrypt(nonce, msg_to_send.encode(), None)
        msg_to_send = nonce + ciphertext
        return msg_to_send



    def process(self, msg):
        """ Processa uma mensagem (`bytestring`) enviada pelo CLIENTE.
            Retorna a mensagem a transmitir como resposta (`None` para
            finalizar ligação) """
        self.msg_cnt += 1
        #
        # ALTERAR AQUI COMPORTAMENTO DO SERVIDOR
        #
        #
        if self.msg_cnt == 1:
            self.peer_public_key1 = msg
            # deserilização de gx  gx
            self.peer_public_key = load_pem_public_key(msg)
            # gy
            self.public_encode_key = self.public_key.public_bytes(encoding=serialization.Encoding.PEM,
                                                                format=serialization.PublicFormat.SubjectPublicKeyInfo)
            # (gy,gx)
            message = suportfuncs.mkpair(self.public_encode_key, msg)
            # Signatura (public key server, publickey Cliente) == S(gx,gy)
            signature = msg_server.sign(message, padding.PSS(mgf=padding.MGF1(hashes.SHA256()),
                                                            salt_length=padding.PSS.MAX_LENGTH), hashes.SHA256())

            first_pair = suportfuncs.mkpair(self.public_encode_key, signature)
            # chave pubica do sertificado
            with open("certs/MY_MSG_SERVER.crt", "rb") as f:
                server_crt_raw = f.read()
            # pair
            final_pair = suportfuncs.mkpair(first_pair, server_crt_raw)
            return final_pair

        if self.msg_cnt == 2:

            client_signatura, client_crt = suportfuncs.unpair(msg)

            # client public
            client = x509.load_pem_x509_certificate(client_crt).public_key()
            cert = x509.load_pem_x509_certificate(client_crt, default_backend())
            subject = cert.subject

            for attr in subject:
                if attr.oid == x509.ObjectIdentifier('2.5.4.65'):
                    self.pseudonym = attr.value

            self.msg_number = len(self.server.get_message_queue_by_UID(self.pseudonym).queue)

            # (gy, gx)
            data = suportfuncs.mkpair(self.peer_public_key1, self.public_encode_key)

            is_valid = suportfuncs.valida_certServer("certs/MY_MSG_CA.crt", cert)

            if not is_valid:
                timestamp = time.strftime('%Y-%m-%d %H:%M:%S', time.localtime())
                sys.stderr.write(f" {timestamp} - MSG RELAY SERVICE: verification error!\n")
                sys.exit(1)

            try:
                client.verify(
                    # S(gy,gx)
                    client_signatura,
                    # (gy,gx)
                    data,
                    padding.PSS(
                        mgf=padding.MGF1(hashes.SHA256()),
                        salt_length=padding.PSS.MAX_LENGTH
                    ),
                    hashes.SHA256()
                )
            except:
                timestamp = time.strftime('%Y-%m-%d %H:%M:%S', time.localtime())
                sys.stderr.write(f"{timestamp} - MSG RELAY SERVICE: verification error!\n")
                sys.exit(1)
            shared_key = self.private_key.exchange(self.peer_public_key)
            derived_key = HKDF(algorithm=hashes.SHA256(), length=32, salt=None, info=b'handshake data', ).derive(
                shared_key)
            self.aesgcm = AESGCM(derived_key)
            return b' '

        if self.msg_cnt >= 3:
            nonce = msg[:12]
            ciphertext = msg[12:]
            msg = self.aesgcm.decrypt(nonce, ciphertext, None)


            msg_Assinada, msgTotal = suportfuncs.unpair(msg)
            self.signature_message = msg_Assinada
            txt = bytes(msgTotal)

            new_msg = txt.upper()
            #print(self.pseudonym, ":", msgTotal.decode())


            if txt.startswith(b"send"):
                parts = txt.split()
                if len(parts) >= 4:
                    timestamp = time.strftime('%Y-%m-%d %H:%M:%S', time.localtime())

                    user_UID = parts[1]
                    self.msg_number = len(self.server.get_message_queue_by_UID(user_UID.decode()).queue)

                    if user_UID:
                        self.msg_number += 1
                        send_subject = parts[2]
                        message_content = b' '.join(parts[3:])
                        # Adiciona a mensagem à fila do destinatário correto
                        destination_worker = self.server.get_worker_by_pseudonym(user_UID.decode())
                        if destination_worker:
                            destination_worker.add_message_to_queue(self.pseudonym, user_UID, self.msg_number,
                                                                    timestamp, send_subject.decode(),
                                                                    message_content.decode(),
                                                                    self.signature_message,
                                                                    txt)
                            new_msg = f"Mensagem adicionada à fila de {user_UID.decode()}"
                            new_msg = new_msg.encode()

                            # Registro de transação - log
                            self.server.log_send(self.pseudonym, user_UID.decode())

                        else:
                            new_msg = (
                                f"Erro: Não foi possível encontrar o destinatário {user_UID.decode()} para adicionar a mensagem.")
                            new_msg = new_msg.encode()
                else:
                    new_msg = "Erro: O ID do destinatário está ausente na mensagem 'send'."
                    new_msg = new_msg.encode()

            if txt.startswith(b"askqueue"):
                # Chamada da função para obter mensagens não lidas e preparar para enviar de volta ao cliente
                unread_messages = self.get_unread_messages()
                if unread_messages:
                    unread_messages_str = str(unread_messages)
                    unread_messages_str = unread_messages_str.encode()
                    nonce = os.urandom(12)
                    ciphertext = self.aesgcm.encrypt(nonce, unread_messages_str, None)
                    unread_messages_str1 = (nonce + ciphertext)

                    return unread_messages_str1
                else:
                    new_msg = b"No unread messages"

                # Registro de transação - log
                self.server.log_askqueue(self.pseudonym)

            if txt.startswith(b"getmsg"):
                parts = txt.split()
                if len(parts) == 2:
                    get_messages = self.get_messages().queue
                    msg_number = int(parts[1])

                    if get_messages is not None and msg_number <= len(get_messages) and get_messages[msg_number - 1]:
                        userqueue = self.server.get_read_messages_by_UID(self.pseudonym)
                        message = get_messages[msg_number - 1]
                        userqueue.put(message)
                        self.server.get_read_messages_by_UID(self.pseudonym).put(userqueue)

                        #print(f"Mensagem {msg_number} marcada como lida.") debug

                        #obter o nome do ficheiro
                        filename = f"certs/MY_{message[1]}.p12"
                        private_key, user_cert, ca_cert = suportfuncs.get_userdata(filename)

                        cert_data = user_cert.public_bytes(serialization.Encoding.PEM)
                        cert = x509.load_pem_x509_certificate(cert_data)
                        public_key = cert.public_key()
                        self.sender_public_key = public_key

                        try:
                            public_key.verify(
                                message[5],
                                message[6],
                                padding.PSS(
                                    mgf=padding.MGF1(hashes.SHA256()),
                                    salt_length=padding.PSS.MAX_LENGTH
                                ),
                                hashes.SHA256()
                            )
                            # enviar para o cliente
                            # message = str(message[4])
                            message = {
                                "sender": message[1],
                                "subject": message[3],
                                "content": str(message[4])
                            }
                            msg_to_send_json = json.dumps(message)
                            nonce = os.urandom(12)
                            ciphertext = self.aesgcm.encrypt(nonce,  msg_to_send_json.encode(), None)
                            msg_to_send = nonce + ciphertext

                            # Registro de transação - log
                            self.server.log_get_msg(self.pseudonym)

                            return msg_to_send
                        except:
                            timestamp = time.strftime('%Y-%m-%d %H:%M:%S', time.localtime())
                            sys.stderr.write(f"{timestamp} - MSG RELAY SERVICE: verification error!\n")

                    else:
                        timestamp = time.strftime('%Y-%m-%d %H:%M:%S', time.localtime())
                        sys.stderr.write(f"{timestamp} - MSG RELAY SERVICE: unknown message!\n")
                        return None

        nonce = os.urandom(12)
        ciphertext = self.aesgcm.encrypt(nonce, new_msg, None)
        new_msg = (nonce + ciphertext)

        #

        return new_msg if len(new_msg) > 0 else None


#
#
# Funcionalidade Cliente/Servidor
#
# obs: não deverá ser necessário alterar o que se segue
#


async def handle_echo(reader, writer, server_instance):
    global conn_cnt
    conn_cnt += 1
    addr = writer.get_extra_info('peername')
    srvwrk = ServerWorker(conn_cnt, addr, server_instance)
    server_instance.add_worker(srvwrk.pseudonym, srvwrk.id, srvwrk)
    data = await reader.read(max_msg_size)
    while True:
        if not data:
            continue
        if data[:1] == b'\n':
            break
        data = srvwrk.process(data)
        if not data:
            break
        writer.write(data)
        await writer.drain()
        data = await reader.read(max_msg_size)
    #print("[%r]" % srvwrk.pseudonym)
    writer.close()


def run_server():
    server_instance = Server()
    loop = asyncio.new_event_loop()
    coro = asyncio.start_server(lambda r, w: handle_echo(r, w, server_instance), '127.0.0.1', conn_port)
    server = loop.run_until_complete(coro)

    # Adicionando workers
    for pseudonym in ['MSG_CLI1', 'MSG_CLI2', 'MSG_CLI3', 'MSG_CLI4', 'MSG_CLI5']:
        server_instance.add_worker(pseudonym, f"{pseudonym}", ServerWorker(0, server=server_instance))

    # Serve requests until Ctrl+C is pressed
    print('Serving on {}'.format(server.sockets[0].getsockname()))
    print('  (type ^C to finish)\n')
    try:
        loop.run_forever()
    except KeyboardInterrupt:
        pass
    # Close the server
    server.close()
    loop.run_until_complete(server.wait_closed())
    loop.close()
    print('\nFINISHED!')


run_server()