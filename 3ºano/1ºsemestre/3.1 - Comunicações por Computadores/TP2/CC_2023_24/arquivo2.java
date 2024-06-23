import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


public class FS_Node {
    private static final int BLOCK_SIZE = 1024;
    private static final int PACKAGE_SIZE = 1089;
    private Map<String, List<InetAddress>> ipNodeAndBlocks; //nome ficheiro, list<ips dos nodes que o têm n vezes>

    private static List<InfoNode> registeredNodes = new ArrayList<>();


    public FS_Node() {
        this.ipNodeAndBlocks = new HashMap<>();
    }

    public static void main(String[] args) {
        FS_Node instancia = new FS_Node();
        //instancia.pedeFicheiroTESTE();
        instancia.abreUDP();
        instancia.abreTCP();
    }

    private void pedeFicheiroTESTE() {
        try {

            try {
                String chave = "abc";
                InetAddress endereco = InetAddress.getByName("localhost");

                List<InetAddress> listaDeEnderecos = new ArrayList<>();
                for (int i = 0; i < 14; i++) {
                    listaDeEnderecos.add(endereco);
                }

                ipNodeAndBlocks.put(chave, listaDeEnderecos);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }

            InetAddress enderecoIP = InetAddress.getByName("localhost");

            DatagramSocket socket = new DatagramSocket();
            byte[] buffer = new byte[65];
            buffer[0] = 0;  // Tipo de mensagem

            byte[] arrayNomeFicheiro = new byte[60];
            String filename = "abc";
            byte[] bytesDoNomeDoArquivo = filename.getBytes();
            System.arraycopy(bytesDoNomeDoArquivo, 0, arrayNomeFicheiro, 0, bytesDoNomeDoArquivo.length);
            System.arraycopy(arrayNomeFicheiro, 0,buffer,1,60);// Nome do ficheiro

            int inteiro = 0;
            byte[] byteArray = {0, 0, 0, 0};

            System.arraycopy(byteArray, 0,buffer,61,4);

            DatagramPacket mensagem = new DatagramPacket(buffer, buffer.length, enderecoIP, 9876);

            socket.send(mensagem);
            System.out.println("quero o bloco " + Arrays.toString(byteArray) + " do ficheiro " + filename);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //TCP --------------------------------------------------------------------------------------------------
    public void abreTCP() {
        String serverAddress = "localhost"; // Endereço do servidor PC1
        int serverPort = 9090; // Porta do servidor
        Scanner scan = new Scanner(System.in);
        String action;
        String conteudo;
        NodeTrackerMessage message;
        try (Socket socket = new Socket(serverAddress, serverPort)) {
            // Configurar fluxos de entrada e saída
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
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

                if (action.equals("update")) {
                    //TODO O QUE POR AQUI?
                    //informaTrackerSobreAtualizacao();
                }

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

                            List<InetAddress> lista = new ArrayList<>();
                            int count = 0;
                            for (Map.Entry<String, Integer> entry : map.entrySet()) {
                                String chave = entry.getKey();
                                Integer valor = entry.getValue();
                                InetAddress endereco = InetAddress.getByName(chave);
                                for (int i = count; i < valor; i++) {
                                    lista.add(endereco);
                                }
                                count = valor;
                            }
                            this.ipNodeAndBlocks.put(conteudo, lista);

                            DatagramSocket serverSocket = new DatagramSocket();
                            requestNextBlock(serverSocket,lista.get(0),9876,0,conteudo); //pede primeiro bloco em udp

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

                byte[] receiveData = new byte[PACKAGE_SIZE];
                Map<String, Integer> updatedFiles = new HashMap<>();

                CustomLogger.logInfo("Servidor UDP esperando na porta " + serverPort);

                while (true) {
                    //alocar memoria
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    serverSocket.receive(receivePacket);
                    //System.out.println("Recebeu uma mensagem do cliente.");

                    byte[] receivedData = receivePacket.getData();
                    byte messageType = receivedData[0];
                    //System.out.println(Arrays.toString(receivedData));

                    if (messageType == 0) { // Se o primeiro byte for 0, é um pedido de ficheiro
                        String fileName = new String(receivedData, 1, 60).trim(); // o nome do ficheiro esta nos próximos 60 bytes
                        int index = ByteBuffer.wrap(receivedData, 61, 4).getInt();
                        CustomLogger.logInfo("Estou a receber um pedido do bloco nº " + index +" do ficheiro " + fileName );
                        sendBlock(fileName, index, receivePacket.getAddress(), serverSocket);
                    } else if (messageType == 1) { // Se o primeiro byte for 1, é uma transferência de ficheiro
                        aceitaFicheiros(serverSocket, receivePacket, updatedFiles);
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

    }

    //recebe bloco e guarda-o
    private void aceitaFicheiros(DatagramSocket serverSocket, DatagramPacket receivePacket,  Map<String, Integer> updatedFiles) {
        try {
            String fileName = new String(receivePacket.getData(), 1, 60).trim();
            int startIndex = ByteBuffer.wrap(receivePacket.getData(), 61, 4).getInt();

            CustomLogger.logInfo("Recebi o bloco "+ startIndex + " do ficheiro "+ fileName);
            try (FileOutputStream fileOutputStream = new FileOutputStream(fileName, true)) {
                fileOutputStream.write(receivePacket.getData(), 65, receivePacket.getLength()-65);
                fileOutputStream.flush();
                List<InetAddress> lista = this.ipNodeAndBlocks.get(fileName);
                if(startIndex+1 < lista.size()) {
                    InetAddress nextIp = lista.get(startIndex+1);

                    updatedFiles.put(fileName, startIndex + 1);

                    informaTrackerSobreAtualizacao(updatedFiles); //update
                    requestNextBlock(serverSocket, nextIp, receivePacket.getPort(), startIndex + 1, fileName);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void informaTrackerSobreAtualizacao(Map<String, Integer> updatedFiles) {
        try {
            String trackerAddress = "localhost";
            int trackerPort = 9090;

            Socket socket = new Socket(trackerAddress, trackerPort);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());

            NodeTrackerMessage message = new NodeTrackerMessage();
            message.setAction("update");
            message.setConteudo(updatedFiles);

            out.writeObject(message);
            out.flush();
            out.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //sendConfirmacão
    private static void requestNextBlock(DatagramSocket serverSocket, InetAddress clientAddress, int clientPort, int nextBlockNumber,String filename) {
        try {

            byte[] buffer = new byte[65];
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

            DatagramPacket mensagem = new DatagramPacket(buffer, buffer.length, clientAddress, clientPort);

            serverSocket.send(mensagem);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //manda bloco do index n
    public static void sendBlock(String fileName, int index, InetAddress address, DatagramSocket serverSocket) throws IOException {
        byte[] buffer = new byte[PACKAGE_SIZE];
        buffer[0] = 1;  // Tipo de mensagem

        byte[] arrayNomeFicheiro = new byte[60];
        byte[] bytesDoNomeDoArquivo = fileName.getBytes();
        System.arraycopy(bytesDoNomeDoArquivo, 0, arrayNomeFicheiro, 0, bytesDoNomeDoArquivo.length);
        System.arraycopy(arrayNomeFicheiro, 0,buffer,1,60);// Nome do ficheiro

        byte[] indexBytes = ByteBuffer.allocate(4).putInt(index).array();
        System.arraycopy(indexBytes, 0, buffer, 61, 4);

        CustomLogger.logInfo("Vou mandar o blobo nº " + index + " do ficheiro " + fileName);

        try (FileInputStream fileInputStream = new FileInputStream(fileName)) {

            long offset = index * 1024;
            fileInputStream.skip(offset);

            int bytesRead = fileInputStream.read(buffer, 65, 1024);
            CustomLogger.logInfo("Bytes lidos: "+ bytesRead);

            int packetSize = 65 + bytesRead;
            byte[] finalBuffer = new byte[packetSize];
            System.arraycopy(buffer, 0, finalBuffer, 0, packetSize);

            serverSocket.send(new DatagramPacket(finalBuffer, packetSize, address, 9876));
            //System.out.println(address.toString() + ": "+ "mandei pacote" + Arrays.toString(finalBuffer));
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}


/*
                    // Registrar o node
                    InfoNode newNode = new InfoNode(socket.getInetAddress().getHostAddress());
                    newNode.setNodeName(socket.getInetAddress().getHostAddress());

                    Map<String, Integer> map = (Map<String, Integer>) message.getConteudo();
                    for (Map.Entry<String, Integer> entry : map.entrySet()) {
                        String nomeficheiro = entry.getKey();
                        Integer numblocos = entry.getValue();
                        newNode.addFile(nomeficheiro, numblocos);
                    }
                    registeredNodes.add(newNode);

                    // Registrar o nó no serviço de resolução de nomes
                    Name_Resolution.registerNode(newNode.getNodeName(), socket.getInetAddress().getHostAddress());

                    // Enviar resposta ao cliente
                    NodeTrackerMessage response = new NodeTrackerMessage("success_update_list", null);
                    out.writeObject(response);

*/