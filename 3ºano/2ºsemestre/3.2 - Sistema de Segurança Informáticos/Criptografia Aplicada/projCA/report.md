# Relatório de SSI - TP1 

## Composição do grupo:

**Nome:** Gonçalo Araújo Brandão
**Número:** A100663
**E-mail:** a100663@alunos.uminho.pt

**Nome:** Mariana Morais
**Número:** A100662
**E-mail:** a100662@alunos.uminho.pt

**Nome:** Maya Gomes
**Número:** A100822
**E-mail:**  a100822@alunos.uminho.pt

## Introdução

Neste projeto pretende-se construir um serviço de *Message Relay* que permita aos membros de uma organização trocarem mensagens com garantias de autenticidade. 
O serviço será suportado por um servidor responsável por manter o estado da aplicação e interagir com os utilizadores do sistema. 

## Encriptação - Funcionalidade pretendida

A implementação do nosso sistema de mensagens seguro utiliza a criptografia AES-GCM para proteger a confidencialidade e a integridade das mensagens trocadas entre cliente e servidor. Também utilizamos o protocolo de acordo de chaves de Diffie-Hellman para estabelecer uma chave compartilhada de forma segura sem a necessidade de ser feita diretamente pela rede, mantendo a comunicação secreta. Por fim, de modo a garantir a autenticidade das keys trocadas entre cliente e servidor, utilizamos assinaturas digitais. Sendo que ambos os lados podem verificar a autenticidade das mesas com o certificados digitais também fornecidos para ambos os lados. 
Também realizamos este processo em trocas de mensagens, como no comando send, de modo a garantir a autenticidade das mensagens.  


## Comandos da aplicação cliente

## -user
O argumento opcional `-user <FNAME>` especifica o ficheiro com dados do utilizador. Por omissão, será assumido que esse ficheiro é `userdata.p12`. Em caso de uso deste argumento opcional, deve ser feito antes do comando que efetivamente queremos fazer, noutro caso dará erro e apresentará também a mensagem ativada pelo comando 'help'.


## send
O comando `send <UID> <SUBJECT>` envia uma mensagem com assunto `<SUBJECT>` destinada ao utilizador com identificador `<UID>`. O conteúdo da mensagem será lido do `stdin`, e o tamanho deve ser limitado a 1000 bytes.
Para tal, começamos por verificar se a mensagem começa por "send" e caso seja dividimos a mensagem em partes usando a função `split()`. Como se espera que a mensagem tenha pelo menos o remetente, o destinatário, o assunto e o conteúdo, verifica-se se há pelo menos 4 partes na respetiva mensagem dividida.
De seguida, obtemos o número de mensagens do destinatário de forma a atualizar o contador ao adicionar a nova mensagem. Para tal, aplicamos a função `len()` à queue de mensagens do respetivo cliente (usando o seu `user_UI`).
A mensagem é adicionada a respetiva queue e registamos a transação no log do servidor. Caso o destinatário não existe é retornada uma mensagem de erro.



## askqueue
O comando `askqueue` solicita ao servidor que lhe envie a lista de mensagens não lidas da queue do utilizador. Para cada mensagem na queue, é devolvida uma linha contendo: `<NUM>:<SENDER>:<TIME>:<SUBJECT>`, onde `<NUM>` é o número de ordem da mensagem na queue e `<TIME>` um timestamp adicionado pelo servidor que regista a altura em que a mensagem foi recebida. 
Para tal, começamos por verificar se a mensagem começa por "askqueue" e caso seja é chamada a auxiliar `get_unread_messages()`. Esta função permite nos obter as mensagens não lidas do utilizador. Sendo que possuímos uma queue com as mensagens todas do cliente e outra queue com as mensagens lidas (que passaram pelo getmsg), as mensagens não lidas resultam nas mensagens que estão na queue geral
e não estão na queue de mensagens lidas, isto é, os elementos diferentes entre as duas queues. De seguida, se efetivamente houver mensagens não lidas, convertemos a lista de mensagens não lidas numa string, codificamos a string em bytes, gera-se um *nounce* e encripta-se as mensagens não lidas usando o *algoritmo de criptografia AES-GCM*. Se não houver mensagens não lidas é enviado um aviso.


## getmsg
O comando `getmsg <NUM>` solicita ao servidor o envio da mensagem da sua queue com número `<NUM>`. No caso de sucesso, a mensagem será impressa no `stdout`. Uma vez enviada, essa mensagem será marcada como lida, pelo que não será listada no próximo comando `askqueue` (mas pode voltar a ser pedida pelo cliente).
Para tal, começamos por verificar se a mensagem começa por "getmsg", se sim, dividimos a mensagem em partes usando a função `split()`. De seguida, verificamos também se a mensagem contém exatamente duas partes, isto é, o comando e o número da mensagem que se pretende consultar. 
Obtemos a queue de mensagens do cliente em questão e o número da mensagem solicitada. Se o número da mensagem solicitada está no intervalo válido e se a mensagem realmente existe, marcamos essa como lida, ou seja, adicionamos a mensagem à queue de mensagens lidas do cliente (dicionário `read_messages`)em questão. De seguida, usamos a função de verificação do certificado fornecida no enunciado de forma a garantir a assinatura digital da mensagem usando a chave pública do utilizador. Finalmente, mais uma vez, usamos *AES-GCM* para encriptar a mensagem e adicionamos a transação no log do servidor.


## help
O comando `help` imprime instruções de uso do programa.
Para tal, do lado do cliente, verificamos se a mensagem recebida começa com 'help' e definimos uma variável `help_msg` que possui os comandos disponíveis: `send`, `askqueue`, `getmsg` e `help`. Para além disso,  decidimos formatar as instruções para facilitar a leitura e compreensão das mesmas. Finalmente, usamos *AES-GCM* para encriptar a mensagem.
Em caso de um comando ser digitado incorretamente, embora não invocado como comando, esta mensagem é apresentada ao cliente.

## Outras funcionalidades implementadas

### Componente de certificação - gerar os próprios certificados usados pela aplicação

De forma a gerarmos os nossos próprios certificados, decidimos começar por gerar o nosso próprio CA, de forma a garatir veracidade nas validações de certificado e assinaturas 
durante o projeto. Para gerar novos certificados, estes precisam de ser assinados com a chave privada do CA. Portanto, geramos um `self-signed certificate` como sendo o nosso CA e guardamos a chave privada deste em memória. Com esta, geramos os nossos novos MSG_CLI assinando os com a chave privada do CA e alteramos os nossos processos internos para serem compatíveis com os nomes dos novos certificados.  

### Sistema de Log

Com o objetivo de registar transações do servidor decidimos criar um sistema de *Log*. Para tal, foi feita uma função principal `log()`. Esta recebe diversos parâmetros: `typeColour, type, details`. Desta forma, o `type` representa o tipo da mensagem (askqueue, por exemplo) e `details` representa o sender da mensagem, e no caso do comando `send` o sender e o recipient da mesma. Também decidimos criar um timestamp de forma a registar quando são efetuados os comandos. De seguida, cria se um ficheiro `server_log.txt`onde guardamos as informações (para além do registo no terminal do servidor). Também foi criada uma função específica de cada comando de forma a especificar os parâmetros de cada uma e a respetiva cor escolhida. Essas são mais tarde chamadas na execução de cada comando.

## Ideias para funcionalidades não implementadas 

### Servidor "curioso"

Esta funcionalidade acabou por não ser implementada devido a um erro no comando askqueue que estava apenas a retornar uma mensagem e não todas as mensagens não lidas. Portanto, como isto punha em causa as funcionalidades básicas, decidimos colocar os nossos resultados numa pasta à parte denominada "servidorCurioso" apesar de não estar 100% funcional.

Contudo esta foi a sua implementação:
Iríamos cifrar o conteúdo do send (corpo e assunto da mensagem) com a chave pública do destinatário que está na pasta das chaves públicas dos nossos clientes. Após estar cifrado, com o criptograma obtido adicionaremos a este o remetente e o destinatário da mensagem em questão.
De seguida ciframos tudo, ou seja, o criptograma, o remetente e o destinatário com a chave combinada entre o remetente e o servidor.
Posteriormente, o criptograma seria enviado para o servidor.

O servidor decifra o criptograma recebido com a chave combinada entre ele e o remetente. Deste modo, o servidor consegue adicionar a mensagem à queue de cada cliente corretamente, mas não consegue ter acesso à informação sensível da mensagem, pois esta foi criptografada com uma chave pública do destinatário. Logo o servidor não consegue aceder.

Quando o destinatário pede a mensagem, o servidor cifra a informação necessária com a chave combinada entre ele e o destinatário e envia o criptograma obtido para o destinatário.

O destinatário decifra o criptograma obtido com a chave combinada entre ele o servidor, e para visualizar corretamente o conteúdo da mensagem necessita de decifrar a informação sensível com a sua chave privada.

## Conclusão e trabalhos futuros
De forma a concluir este primeiro projeto, a nossa apreciação crítica tem um balanço positivo. Dado que as funcionalidades pretendidas funcionam efetivamente e conseguimos ainda trabalhar nas possíveis valorizações, sentimo-nos satisfeitos com o nosso desempenho. Contudo, pretendíamos terminar todas as possíveis valorizações que nos foram desafiadas, embora sem sucesso. 
As expectativas que tínhamos para o trabalho foram preenchidas mesmo a nível do que um projeto pode exigir, uma vez que os guiões feitos em aula foram úteis e um grande motor de arranque para este. 



