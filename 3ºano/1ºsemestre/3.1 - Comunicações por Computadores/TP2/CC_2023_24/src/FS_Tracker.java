import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.*;
import java.util.stream.Collectors;

public class FS_Tracker {

    static List<InfoNode> registeredNodes = new ArrayList<>();


    public static void main(String[] args) {

        TcpServerThread tsth = new TcpServerThread();
        tsth.run();

    }

    static class TcpServerThread implements Runnable {

        @Override
        public void run() {

            int port = 9090; // Porta do servidor


            try (ServerSocket serverSocket = new ServerSocket(port)) {
                CustomLogger.logInfo("Servidor FS Track Protocol esperando por conexões...");

                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    CustomLogger.logInfo("Conexão estabelecida com cliente " + clientSocket.getInetAddress());

                    new Thread(() -> {
                        try {
                            handle(clientSocket);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }).start();

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // encontrar os nodes que têm o ficheiro (menos o node que o pediu)
    private static Map<String, Integer> findNodesByFile(String searchQuery, String requestingNodeId) {
        Map<String, Integer> nodesEBlocos = new HashMap<>();

        for (InfoNode node : registeredNodes) {
            // se n queremos com o node que o pediu (testar no intelij) :if (node.getFilesAndBlocks().containsKey(searchQuery)) {
            if (!node.getNodeId().equals(requestingNodeId) && node.getFilesAndBlocks().containsKey(searchQuery)) {
                nodesEBlocos.put(node.getNodeId(), node.getNumBlocks(searchQuery));
            }
        }
        return nodesEBlocos;
    }



    private static void handle(Socket clientSocket) throws IOException {
        try {
            // Configurar fluxos de entrada e saída
            ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
            NodeTrackerMessage message;

            while (true) {
                // Ler a mensagem do cliente
                message = (NodeTrackerMessage) in.readObject();
                CustomLogger.logInfo("Recebido: " + message.getAction() + " " + message.getConteudo().toString());

                // Lógica para processar a mensagem e registrar o nó
                if (message.getAction().equals("register_node")) {

                    // Registrar o node
                    InfoNode newNode = new InfoNode(clientSocket.getInetAddress().getHostAddress());
                    newNode.setNodeName(clientSocket.getInetAddress().getHostAddress());

                    Map<String, Integer> map = (Map<String, Integer>) message.getConteudo();
                    for (Map.Entry<String, Integer> entry : map.entrySet()) {
                        String nomeficheiro = entry.getKey();
                        Integer numblocos = entry.getValue();
                        newNode.addFile(nomeficheiro, numblocos);
                    }
                    registeredNodes.add(newNode);

                    // Enviar resposta ao cliente
                    NodeTrackerMessage response = new NodeTrackerMessage("success_register_node", null);
                    out.writeObject(response);
                } else if (message.getAction().equals("update")) {
                    String nodeAddress = clientSocket.getInetAddress().getHostAddress();
                    InfoNode existingNode = findNodeByAddress(nodeAddress);

                    // Atualizar as informações do nó existente com o novo ficheiro
                    Map.Entry<String, Integer> map = (Map.Entry<String, Integer>) message.getConteudo();
                    existingNode.addFile(map.getKey(), map.getValue());
                } else if (message.getAction().equals("get_local")) {
                    // Obter localização dos blocos e nós
                    String searchficheiro = (String) message.getConteudo();
                    String requestingNodeId = clientSocket.getInetAddress().getHostAddress();
                    Map<String, Integer> nodesAndBlocks = findNodesByFile(searchficheiro, requestingNodeId);

                    //ordenar o map por quem tem menos blocos até quem tem mais
                    List<Map.Entry<String, Integer>> lista = new ArrayList<>(nodesAndBlocks.entrySet());
                    Collections.sort(lista, Map.Entry.comparingByValue());
                    Map<String, Integer> nodesAndBlocksOrdenados = lista.stream()
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                                    (e1, e2) -> e1, LinkedHashMap::new));

                    NodeTrackerMessage response;
                    if(nodesAndBlocksOrdenados.isEmpty()){
                        response = new NodeTrackerMessage("local_not_found", nodesAndBlocksOrdenados);
                    }else {
                        response = new NodeTrackerMessage("success_get_local", nodesAndBlocksOrdenados);
                    }
                    try {
                        out.writeObject(response);
                        out.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (message.getAction().equals("exit")){
                    for (InfoNode node : registeredNodes) {
                        InetAddress clientAddress = clientSocket.getInetAddress();
                        String ipAddress = clientAddress.getHostAddress();
                        if (node.getNodeId().equals(ipAddress)){
                            registeredNodes.remove(node);
                        }
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (EOFException e){
            return;
        } catch(SocketException e){
            CustomLogger.logError("O cliente " + clientSocket.getInetAddress() + ":" + clientSocket.getPort() + " fechou a conexão");
        } catch(Exception e){
            e.printStackTrace();
        }finally {
            try {
                // Fechar a conexão com o cliente
                clientSocket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static InfoNode findNodeByAddress(String nodeAddress) {
        for (InfoNode node : registeredNodes) {
            if (node.getNodeName().equals(nodeAddress)) {
                return node;
            }
        }
        return null;
    }
}
