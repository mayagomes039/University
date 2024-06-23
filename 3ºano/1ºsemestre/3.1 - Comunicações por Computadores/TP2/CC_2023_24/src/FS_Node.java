import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.CRC32;


public class FS_Node {
    private static final int BLOCK_SIZE = 1024;
    private static final int PACKAGE_SIZE = 1097;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private List<Pedidos> pedidosNaoRespondidos;
    private String serverAddress;
    private Map<String, byte[][]> fileArraysMap = new HashMap<>();
    private ObjectOutputStream out;


    public FS_Node(String args) {
        this.serverAddress = args;
        this.pedidosNaoRespondidos = new ArrayList<>();
    }

    public static void main(String[] args) {
        if (args.length > 0){
            FS_Node instancia = new FS_Node(args[0]);
            //instancia.pedeFicheiroTESTE();
            instancia.abreUDP();
            instancia.abreTCP(args[0]);
        }else {
            CustomLogger.logError("Tem de colocar o IP do tracker!");
        }
    }

    //TCP --------------------------------------------------------------------------------------------------
    public void abreTCP(String serverAddress) {
        int serverPort = 9090; // Porta do servidor
        Scanner scan = new Scanner(System.in);
        String action;
        String conteudo;
        NodeTrackerMessage message;
        try (Socket socket = new Socket(serverAddress, serverPort)) {
            // Configurar fluxos de entrada e saída
            this.out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            //o nodo ve os ficheiros que tem e manda o nome dos ficheiros e blocos para o tracker
            Map<String, Integer> ficheirosNodo = getFicheiros();

            mandaFicheiros(ficheirosNodo, out);
            NodeTrackerMessage resp = (NodeTrackerMessage) in.readObject();
            CustomLogger.logInfo("Resposta do servidor: " + resp.getAction());

            while (true) {
                System.out.println("Introduza action:");
                action = scan.next();

                message = new NodeTrackerMessage();

                if (action.equals("get_local")) {

                    System.out.println("Introduza o nome do ficheiro que procura:");
                    conteudo = scan.next();

                    message.setAction(action);
                    message.setConteudo(conteudo);

                    try {
                        out.writeObject(message);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    try {
                        NodeTrackerMessage response = (NodeTrackerMessage) in.readObject();

                        if (response.getAction().equals("success_get_local")) {
                            CustomLogger.logInfo("Resposta do servidor: " + response.getAction());

                            Map<String, Integer> map = (Map<String, Integer>) response.getConteudo();

                            if (map.isEmpty()) {
                                CustomLogger.logWarning("Nenhum nó possui o ficheiro que procura.");
                                continue; // Pede ao usuário para inserir uma nova ação.
                            }

                            CustomLogger.logInfo("Nodos encontrados e respetivos blocos: " + map);
                            pedeBlocos(map, conteudo);
                        } else {
                            CustomLogger.logError("Ficheiro: " + conteudo + " não encontrado.");
                        }
                    } catch (ClassNotFoundException | IOException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
                if (action.equals("exit")) {
                    message.setAction(action);
                    out.writeObject(message);
                    in.close();
                    out.close();
                    System.exit(0);
                    break;
                }
                // Enviar a mensagem ao servidor
                try {
                    out.writeObject(message);
                    out.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        } catch (SocketException e) {
            CustomLogger.logError("O servidor TCP acabou.");
        } catch (ClassNotFoundException | IOException e) {
            CustomLogger.logError("Erro ao receber a mensagem do servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void pedeBlocos(Map<String, Integer> map, String conteudo) {
        //ir buscar o ultimo valor
        List<Map.Entry<String, Integer>> listaOrdenada = new ArrayList<>(map.entrySet());
        Map.Entry<String, Integer> ultimoEntry = listaOrdenada.get(listaOrdenada.size() - 1);
        Integer numTotalBlocos = ultimoEntry.getValue();

        //calcular o numero "suposto" de blocos que cada node vai enviar
        int numTotalNodes = map.size();
        int envioSuposto = numTotalBlocos / numTotalNodes;

        this.fileArraysMap.put(conteudo, new byte[numTotalBlocos][]);

        int count = 0;
        int x =0;
        int y = 0;
        int countSuposto = (numTotalBlocos % numTotalNodes == 0) ? 0:1;
        for (Map.Entry<String, Integer> entry : map.entrySet()){
            countSuposto += envioSuposto;
            String chave = entry.getKey();
            Integer valor = entry.getValue();
            if (valor<countSuposto){
                x = count;
                y = valor-1;
                count = valor;
            } else{
                x = count;
                y = countSuposto - 1;
                count = countSuposto;
            }
            //mandar mensagem
            byte[] buffer = new byte[69];
            buffer[0] = 0;  // Tipo de mensagem

            byte[] arrayNomeFicheiro = new byte[60];
            byte[] bytesDoNomeDoArquivo = conteudo.getBytes();
            System.arraycopy(bytesDoNomeDoArquivo, 0, arrayNomeFicheiro, 0, bytesDoNomeDoArquivo.length);
            System.arraycopy(arrayNomeFicheiro, 0,buffer,1,60);// Nome do ficheiro

            byte[] firstIndex = new byte[4];
            for (int i = 0; i < 4; i++) {
                firstIndex[3 - i] = (byte) (x >> (i * 8));
            }
            System.arraycopy(firstIndex, 0,buffer,61,4);

            byte[] lastIndex =  new byte[4];
            for (int i = 0; i < 4; i++) {
                lastIndex[3 - i] = (byte) (y >> (i * 8));
            }
            System.arraycopy(lastIndex, 0,buffer,65,4);

            try{
                DatagramPacket mensagem = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(chave), 9876);
                DatagramSocket serverSocket = new DatagramSocket();
                serverSocket.send(mensagem);

                for (int i = x; i<= y; i++) {
                    Pedidos pedido = new Pedidos(serverSocket, serverSocket.getInetAddress(), 9876, i, conteudo);
                    new Thread(() -> {
                        checkPedido(pedido);
                    }).start();
                }

            } catch (Exception e){
                CustomLogger.logError("Erro ao mandar pedido de ficheiro ou de thread!");
                e.printStackTrace();
            }
        }

    }

    //manda os nomes dos ficheiros e respetivo num de blocos
    private static void mandaFicheiros(Map<String, Integer> ficheiros, ObjectOutputStream out) {

        NodeTrackerMessage message = new NodeTrackerMessage("register_node", ficheiros);

        try {
            out.writeObject(message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //procura os nomes de ficheiros que tem e o numero de blocos que cada um tem
    private static Map<String, Integer> getFicheiros() {
        String diretorioPath = ".";
        Map<String, Integer> filesandBlocks = new HashMap<>();
        Path diretorio = Paths.get(diretorioPath);

        if (Files.isDirectory(diretorio)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(diretorio)) {
                for (Path arquivo : stream) {
                    // so vai buscar ficheiros (não pastas e outros)
                    if (Files.isRegularFile(arquivo)) {
                        int numblocos = divideFicheiroEmBlocos(String.valueOf(arquivo.getFileName()));
                        filesandBlocks.put(String.valueOf(arquivo.getFileName()), numblocos);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            CustomLogger.logError("O caminho especificado não é um diretório válido.");
        }
        return filesandBlocks;
    }

    private static int divideFicheiroEmBlocos(String fileName) {
        File file = new File(fileName);

        if (!file.exists() || !file.isFile()) {
            CustomLogger.logError("Ficheiro inválido");
            return 0;
        }

        long fileSize = file.length();
        int blockSize = BLOCK_SIZE; // Tamanho fixo dos blocos
        int numBlocksInFile = (int) Math.ceil((double) fileSize / blockSize); //num de blocos

        return numBlocksInFile;
    }


    // UDP -------------------------------------------------------------------------------------------------------

    private void abreUDP() {
        new Thread(() -> {
            try {
                int serverPort = 9876;
                DatagramSocket serverSocket = new DatagramSocket(serverPort);

                CustomLogger.logInfo("Servidor UDP esperando na porta " + serverPort);

                while (true) {
                    byte[] receiveData = new byte[PACKAGE_SIZE];
                    //alocar memoria
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    serverSocket.receive(receivePacket);
                    //System.out.println("Recebeu uma mensagem do cliente.");

                    byte[] receivedData = receivePacket.getData();
                    byte messageType = receivedData[0];
                    //System.out.println(Arrays.toString(receivedData));

                    if (messageType == 0) { // Se o primeiro byte for 0, é um pedido de ficheiro
                        String fileName = new String(receivedData, 1, 60).trim(); // o nome do ficheiro esta nos próximos 60 bytes
                        int x = ByteBuffer.wrap(receivedData, 61, 4).getInt();
                        int y = ByteBuffer.wrap(receivedData, 65, 4).getInt();
                        CustomLogger.logInfo("Estou a receber um pedido do bloco nº " + x + "até ao bloco " + y +" do ficheiro " + fileName );
                        sendBlock(fileName, x, y, receivePacket.getAddress(), serverSocket);
                    } else if (messageType == 1) { // Se o primeiro byte for 1, é uma transferência de ficheiro
                        aceitaFicheiros(serverSocket, receivePacket);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                shutDownScheduler();
            }
        }).start();

    }

    //Recebe o bloco e guarda-o
    private void aceitaFicheiros(DatagramSocket serverSocket, DatagramPacket receivePacket) {
        try {
            String fileName = new String(receivePacket.getData(), 1, 60).trim();
            int index = ByteBuffer.wrap(receivePacket.getData(), 61, 4).getInt();
            InetAddress clientAddress = receivePacket.getAddress();

            CustomLogger.logInfo("Recebi o bloco "+ index + " do ficheiro "+ fileName);

            // Calcular o CRC32 do bloco recebido
            CRC32 crc32 = new CRC32();
            crc32.update(receivePacket.getData(), 73, receivePacket.getLength() - 73);
            long receivedChecksum = crc32.getValue();

            // Comparar o checksum recebido com o checksum esperado
            long expectedChecksum = ByteBuffer.wrap(receivePacket.getData(), 65, 8).getLong();
            if (receivedChecksum == expectedChecksum) {
                Pedidos ans = null;
                for(int i = 0; i<this.pedidosNaoRespondidos.size(); i++){
                    if (this.pedidosNaoRespondidos.get(i).getFilename().equals(fileName) && this.pedidosNaoRespondidos.get(i).getNextBlockNumber() == index){
                        ans = this.pedidosNaoRespondidos.get(i);
                        break;
                    }
                }
                this.pedidosNaoRespondidos.remove(ans);

                //guardar o bloco em memoria
                byte[] content = new byte[receivePacket.getLength()-73];
                System.out.println(content.length);
                System.arraycopy(receivePacket.getData(), 73, content, 0, receivePacket.getLength()-73);

                byte [][] fileBlocos = this.fileArraysMap.get(fileName);
                fileBlocos [index] = content;
                fileArraysMap.put(fileName, fileBlocos);
                mandaACK(serverSocket, receivePacket.getAddress(), receivePacket.getPort(), index, fileName);

                //verifica se ja temos os blocos todos (ordenados)
                for(int i=0; i<fileBlocos.length; i++) {
                    if (fileBlocos[i] == null) return;
                }

                //escrever, copiar os blocos, caso o array ja esta com todos os blocos
                try (FileOutputStream fileOutputStream = new FileOutputStream(fileName, true)) {
                    for (int i=0; i<fileBlocos.length; i++) {
                        fileOutputStream.write(fileBlocos[i], 0, fileBlocos[i].length);
                        fileOutputStream.flush();
                    }
                    //update
                    Map.Entry<String, Integer> updatedFile = new AbstractMap.SimpleEntry<>(fileName, fileBlocos.length-1);
                    informaTrackerSobreAtualizacao(updatedFile);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else {
                //caso o bloco foi mal copiado
                CustomLogger.logError("Checksum inválido para o bloco " + index + " do ficheiro " + fileName + ". Vamos tentar de novo.");
                requestAgainBlock(serverSocket, receivePacket.getAddress(), receivePacket.getPort(), index, fileName);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void mandaACK(DatagramSocket serverSocket, InetAddress nextIp, int port, int index, String fileName) {
        try {
            byte[] buffer = new byte[PACKAGE_SIZE];
            buffer[0] = 2;  // Tipo de mensagem - ACK

            byte[] arrayNomeFicheiro = new byte[60];
            byte[] bytesDoNomeDoArquivo = fileName.getBytes();
            System.arraycopy(bytesDoNomeDoArquivo, 0, arrayNomeFicheiro, 0, bytesDoNomeDoArquivo.length);
            System.arraycopy(arrayNomeFicheiro, 0, buffer, 1, 60);// Nome do ficheiro


            byte[] indexBytes = ByteBuffer.allocate(4).putInt(index).array();
            System.arraycopy(indexBytes, 0, buffer, 61, 4);

            int packetSize = 65;
            byte[] finalBuffer = new byte[packetSize];
            System.arraycopy(buffer, 0, finalBuffer, 0, packetSize);

            serverSocket.send(new DatagramPacket(finalBuffer, packetSize, nextIp, port));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    //update do registo do node no tracker
    private void informaTrackerSobreAtualizacao(Map.Entry<String, Integer> updatedFiles) {
        try {
            NodeTrackerMessage message = new NodeTrackerMessage();
            message.setAction("update");
            message.setConteudo(updatedFiles);

            this.out.writeObject(message);
            this.out.flush();
            this.out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //pede de novo
    private void requestAgainBlock(DatagramSocket serverSocket, InetAddress clientAddress, int clientPort, int nextBlockNumber, String filename) {
        try {

            byte[] buffer = new byte[69];
            buffer[0] = 0;  // Tipo de mensagem

            byte[] arrayNomeFicheiro = new byte[60];
            byte[] bytesDoNomeDoArquivo = filename.getBytes();
            System.arraycopy(bytesDoNomeDoArquivo, 0, arrayNomeFicheiro, 0, bytesDoNomeDoArquivo.length);
            System.arraycopy(arrayNomeFicheiro, 0,buffer,1,60);// Nome do ficheiro


            byte[] byteArray = new byte[4];
            for (int i = 0; i < 4; i++) {
                byteArray[3 - i] = (byte) (nextBlockNumber >> (i * 8));
            }

            System.arraycopy(byteArray, 0,buffer,61,4);
            System.arraycopy(byteArray, 0,buffer,65,4);

            DatagramPacket mensagem = new DatagramPacket(buffer, buffer.length, clientAddress, clientPort);

            serverSocket.send(mensagem);

            Pedidos pedido = new Pedidos(serverSocket, clientAddress, clientPort, nextBlockNumber, filename);
            this.pedidosNaoRespondidos.add(pedido);

            new Thread(() -> {
                checkPedido(pedido);
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Função time, para caso demora mais que 20 segundos
    private void checkPedido(Pedidos pedido) {
        try {
            Thread.sleep(2000);
            for (Pedidos p: this.pedidosNaoRespondidos){
                if (pedido.getFilename().equals(p.getFilename()) && pedido.getNextBlockNumber() == p.getNextBlockNumber()){
                    CustomLogger.logError("O envio do ficheiro demorou demasiado tempo! Vamos pedir novamente.");
                    requestAgainBlock(pedido.getServerSocket(), pedido.getClientAddress(), pedido.getClientPort(),pedido.getNextBlockNumber(), pedido.getFilename());
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //Envio do bloco de index n
    public void sendBlock(String fileName, int firstIndex, int lastIndex,  InetAddress address, DatagramSocket serverSocket) throws IOException {
        try (FileInputStream fileInputStream = new FileInputStream(fileName)) {
            long offset = firstIndex * 1024L;
            fileInputStream.skip(offset);
            for (int i=firstIndex; i<=lastIndex; i++){

                byte[] buffer = new byte[PACKAGE_SIZE];
                buffer[0] = 1;  // Tipo de mensagem

                byte[] arrayNomeFicheiro = new byte[60];
                byte[] bytesDoNomeDoArquivo = fileName.getBytes();
                System.arraycopy(bytesDoNomeDoArquivo, 0, arrayNomeFicheiro, 0, bytesDoNomeDoArquivo.length);
                System.arraycopy(arrayNomeFicheiro, 0,buffer,1,60);// Nome do ficheiro


                byte[] indexBytes = ByteBuffer.allocate(4).putInt(i).array();
                System.arraycopy(indexBytes, 0, buffer, 61, 4);

                CustomLogger.logInfo("Vou mandar do blobo nº " + i + " do ficheiro " + fileName);

                int bytesRead = fileInputStream.read(buffer, 73, 1024);
                if (bytesRead == -1) return;
                CustomLogger.logInfo("Bytes lidos: "+ bytesRead);

                // Calcular o checksum do bloco antes de enviá-lo
                CRC32 crc32 = new CRC32();
                crc32.update(buffer, 73, bytesRead);
                long checksum = crc32.getValue();
                byte[] checksumBytes = ByteBuffer.allocate(8).putLong(checksum).array();
                System.arraycopy(checksumBytes, 0, buffer, 65, 8);

                int packetSize = 73 + bytesRead;
                byte[] finalBuffer = new byte[packetSize];
                System.arraycopy(buffer, 0, finalBuffer, 0, packetSize);

                serverSocket.send(new DatagramPacket(finalBuffer, packetSize, address, 9876));
                Pedidos pedido = new Pedidos(serverSocket, serverSocket.getInetAddress(), 9876, i, fileName);
                this.pedidosNaoRespondidos.add(pedido);

            }
            fileInputStream.close();
        }catch (IOException e) {
            return;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    //garantir que o scheduler será encerrado quando o servidor sair
    public void shutDownScheduler() {
        scheduler.shutdown();
    }

}