package src;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import javax.swing.Timer;

public class oNode {
    private String fatherIP;
    private int fatherPort;
    private boolean isChef = false;
    private int port;
    private String caminho_filmes;
    private static String fileMoviesName =null;
    private DatagramSocket datagramSocket;
    private volatile boolean isRunning;
    private final int BUFFER_SIZE = 30000;
    private TreeNode root; // Raiz da árvore
    private Boolean conectionssucess= false;
    private static final int TIMEOUT_SECONDS = 5;
    private List<String> pops = new ArrayList<>(); // Lista de PoPs
    private String origem = null;
    private Map<String, Long> lastHeartbeat = new HashMap<>(); 
    private ScheduledExecutorService heartbeatChecker;
    private static int HEARTBEAT_INTERVAL = 5000;  
    private static int HEARTBEAT_TIMEOUT = 30000; 
    private static int MONITORIZACAO_INTERVAL = 10000;  
    private ScheduledExecutorService heartbeatExecutor;
    private ConcurrentHashMap<String, Long> startTimeMap = new ConcurrentHashMap<>();
    private String chefIp = null;

    //lista dos ips dos clientes conectados a um oNode
    private List<String> clients = new ArrayList<>();

    //Streaming
    private boolean isStreaming = false;
    private DatagramSocket RTPsocket;
    private int RTP_dest_port = 2602;
    private Map<String,VideoStream> videos =  new ConcurrentHashMap<>();
    private Map<String, String> filmes = new HashMap<>(); // map dos filmes disponiveis
    private Map<String, List<String>> videoClientes = new HashMap<>(); // map com filme e os clientes que o querem ver
    private List<String> videos_em_stream = new ArrayList<>();
    private Map<String, Timer> timers = new HashMap<>();

    public void setFather(String fatherIP, int fatherPort) {
        this.fatherPort = fatherPort;
        this.fatherIP = fatherIP;
    }

    public oNode(int port, String[] neighbors, boolean isChef, String caminho_filmes) {
        this.port = port;
        this.root = new TreeNode(getLocalIPAddress());
        this.isChef = isChef;
        this.heartbeatExecutor = Executors.newSingleThreadScheduledExecutor();
        
        try {
            this.datagramSocket = new DatagramSocket(port);
            this.RTPsocket = new DatagramSocket();
        } catch (Exception e) {
            CustomLogger.logInfo("Erro na inicialização do socket UDP - " + e.getMessage());
            e.printStackTrace();
        }

        if (this.isChef) {
            this.caminho_filmes = caminho_filmes;

            try {
                BufferedReader reader = new BufferedReader(new FileReader(caminho_filmes));
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(";");
                    filmes.put(parts[0], parts[1]);
                }

                CustomLogger.logInfo("Ficheiro de filmes lido com sucesso: " + filmes);
            } catch (IOException e) {
                CustomLogger.logError("Erro ao ler o ficheiro de filmes: " + e.getMessage());
            }

            try {

                for (Map.Entry<String,String> entry : filmes.entrySet()){
                    String titulo = entry.getKey();
                    String videoFilePath = entry.getValue();
                    VideoStream video = new VideoStream(videoFilePath);
                    videos.put(titulo, video);

                    Timer sTimer = new Timer(100, new serverTimerListener(titulo));
                    sTimer.setInitialDelay(0);
                    sTimer.setCoalesce(true);
                    timers.put(titulo, sTimer);
                }

                CustomLogger.logInfo("Este nó é o chef da rede overlay.");
                this.fatherIP = null;

            } catch (Exception e1) {
                CustomLogger.logError("Erro ao iniciar o stream de vídeo: " + e1.getMessage());
            }
        }
    }

    ///////////////////////////////////////
    /// Métodos para controlo de fluxo ///
    ///////////////////////////////////////

    private void checkHeartbeats() {
        String myip = getLocalIPAddress();

        if (!lastHeartbeat.isEmpty()) {


        lastHeartbeat.entrySet().removeIf(entry -> {
            long lastTime = entry.getValue(); 
            String sender = entry.getKey();
            List<String> childs = new ArrayList<>(root.getLeafChildren());

            long currentTime = System.currentTimeMillis();
            if ((currentTime - lastTime) > HEARTBEAT_TIMEOUT) {
                CustomLogger.logError("Heartbeat de " + sender + " perdido. Considerando o node desconectado.");
                CustomLogger.logError("Estamos a ter perdas de pacotes. Recuperando...");

                if (sender.equals(fatherIP)) {

                    // verificar se o father é chef
                    if (sender.equals(chefIp)){
                        // se for chef, dar shutdown() pois sem chef o oNode n pode estar on
                        CustomLogger.logError("Father chef (" + sender + ") desconectado. Encerrando o nó.");
                        shutdown();
                    }
                    else if (!childs.contains(sender)){
                        // pedir ao chef o pai do pai
                        String message = "GET_NEW_FATHER"+ ";" + myip + ";" + sender;
                        //enviar ao chef
                        sendMessageUDPToNeighbor(message, chefIp, fatherPort);
                        CustomLogger.logError("Mandamos getnewfather para: " + chefIp);
                    }
                    //else { // se for folha
                        //nao faz nada, pq vai ser tratado pelo client e chef se tiver alguem ligado

                }else if (childs.contains(sender)){
                    //um filho desligou-se, parar envio do stream para o filho 
                    for (Map.Entry<String, List<String>> entrada : videoClientes.entrySet()) {
                        List<String> clientsList = entrada.getValue();
                        if (clientsList.contains(sender)) {
                            clientsList.remove(sender);
                        }
                    }
                    CustomLogger.logError("Filho (" + sender + ") desconectado. Encerrando o streaming deste filho.");


                    // mandar ao servidor chef remover o filho sender da arvore
                    String message = "REMOVE_CHILD" + ";" + sender;
                    sendMessageUDPToNeighbor(message, chefIp, fatherPort);
                    
                } else if (clients.contains(sender)){
                    //um cliente desligou-se, parar envio do stream para o cliente
                    for (Map.Entry<String, List<String>> entrada : videoClientes.entrySet()) {
                        List<String> clientsList = entrada.getValue();
                        if (clientsList.contains(sender)) {
                            clientsList.remove(sender);
                        }
                    }
                    clients.remove(sender);
                    if (clients.isEmpty()){
                        //ja n precisa do stream 
                        // envia END_STREAM + titulo para o pai
                        CustomLogger.logWarning("filmes: " + videos_em_stream);
                        for (String filme : videos_em_stream){
                            String message = "END_STREAM" + ";" + filme + ";" + myip;
                            sendMessageUDPToFather(message);
                            videos_em_stream.remove(filme);
                        }
                    }
                    CustomLogger.logWarning("Cliente (" + sender + ") desconectado. Encerrando o streaming deste cliente.");
                }
                
                return true;
            }
            return false;
        });
        }
    }

    private void sendHeartbeatToFather() {
        heartbeatExecutor.scheduleAtFixedRate(() -> {
            try {
                String message = "HEARTBEAT:" + getLocalIPAddress();
                byte[] buffer = message.getBytes();
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(fatherIP), port);
                datagramSocket.send(packet);
                //startTimeMapHeartBeats.put(fatherIP, System.currentTimeMillis());
                CustomLogger.logInfo("Heartbeat enviado para o father " + fatherIP + ":" + port);
            } catch (IOException e) {
                CustomLogger.logError("Erro ao enviar heartbeat para o chef: " + e.getMessage());
            }
        }, 0, HEARTBEAT_INTERVAL, TimeUnit.MILLISECONDS);
    }

    private void sendHeartbeatToChilds(){
        heartbeatExecutor.scheduleAtFixedRate(() -> {
            try {
                List<String> childs = new ArrayList<>(root.getLeafChildren());
                if (childs.isEmpty()) {
                    return;
                }
                for (String child : childs) {
                    String message = "HEARTBEAT:" + getLocalIPAddress();
                    byte[] buffer = message.getBytes();
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(child), port);
                    datagramSocket.send(packet);
                    //startTimeMapHeartBeats.put(child, System.currentTimeMillis());
                    CustomLogger.logInfo("Heartbeat enviado para o filho em " + child + ":" + port);
                }
            } catch (IOException e) {
                CustomLogger.logError("Erro ao enviar heartbeat para os filhos: " + e.getMessage());
            }
        }, 0, HEARTBEAT_INTERVAL, TimeUnit.MILLISECONDS);
    }

    private void sendHeartbeatToClients(){
        heartbeatExecutor.scheduleAtFixedRate(() -> {
            try {
                if (clients.isEmpty()) {
                    return;
                }
                for (String client : clients) {
                    String message = "HEARTBEAT:" + getLocalIPAddress();
                    byte[] buffer = message.getBytes();
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(client), port);
                    datagramSocket.send(packet);
                    //startTimeMapHeartBeats.put(client, System.currentTimeMillis());
                    CustomLogger.logInfo("Heartbeat enviado para o cliente em " + client + ":" + port);
                }
            } catch (IOException e) {
                CustomLogger.logError("Erro ao enviar heartbeat para os clientes: " + e.getMessage());
            }
        }, 0, HEARTBEAT_INTERVAL, TimeUnit.MILLISECONDS);
    }

    private void sendMonitorizacaoToChilds(){
        heartbeatExecutor.scheduleAtFixedRate(() -> {
            try {
                List<String> childs = new ArrayList<>(root.getChildrenIPs());
                if (childs.isEmpty()) {
                    return;
                }
                for (String child : childs) {
                    int saltos = 1;

                    Long startTime = System.currentTimeMillis();
                    List<String> times = new ArrayList<>();
                    times.add(startTime.toString());

                    List<String> path = new ArrayList<>();
                    path.add(getLocalIPAddress());

                    String message = "MONITORIZACAO" +";" + path +";"+ times +";" + saltos;
                    byte[] buffer = message.getBytes();
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(child), port);
                    datagramSocket.send(packet);
                    CustomLogger.logInfo("Monitorização enviada para o filho em " + child + ":" + port);
                }
            } catch (IOException e) {
                CustomLogger.logError("Erro ao enviar monitorização para os filhos: " + e.getMessage());
            }
        }, 0, MONITORIZACAO_INTERVAL, TimeUnit.MILLISECONDS);
    }

    public void shutdown() {
        CustomLogger.logInfo("Encerrando sockets e recursos do nó...");
    
        // Fecha o socket principal
        if (datagramSocket != null && !datagramSocket.isClosed()) {
            datagramSocket.close();
        }
    
        // Fecha o socket RTP
        if (RTPsocket != null && !RTPsocket.isClosed()) {
            RTPsocket.close();
        }
    
        // Para os timers em execução
        for (Timer timer : timers.values()) {
            timer.stop();
        }
    
        isRunning = false;
        
        CustomLogger.logInfo("Nó desligado com sucesso.");

        System.exit(0);
    }

    ///////////////////////////////////////
    /// Métodos para comunicação UDP  /////
    ///////////////////////////////////////
    
    public void startServer() {
        isRunning = true;

        // Thread para lidar com mensagens UDP
        new Thread(() -> {
            try {
                //this.datagramSocket = new DatagramSocket(port);
                CustomLogger.logInfo("Servidor UDP iniciado na porta " + port);

                while (isRunning) {
                    byte[] receiveData = new byte[BUFFER_SIZE];
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    datagramSocket.receive(receivePacket);
                    handleUdpPacket(receivePacket);
                }
            } catch (IOException e) {
                CustomLogger.logError("Erro no servidor UDP: " + e.getMessage());
            
            } finally {
                if (datagramSocket != null && !datagramSocket.isClosed()) {
                    datagramSocket.close();
                }
            }
        }).start();
    }

    private void handleUdpPacket(DatagramPacket packet) {
        String message = new String(packet.getData(), 0, packet.getLength());
        String myip = getLocalIPAddress();
        String sender = packet.getAddress().toString().replace("/", "");
        int port = packet.getPort();
        if (message.startsWith("HEARTBEAT")) {
            if (sender.equals(chefIp)){
                lastHeartbeat.put(sender, System.currentTimeMillis());
                CustomLogger.logInfo("Heartbeat recebido de " + sender );
            } else {
                String sender2 = message.split(":")[1];
                lastHeartbeat.put(sender2, System.currentTimeMillis());
                CustomLogger.logInfo("Heartbeat recebido de " + sender2 );
            }
                try {
                    String response = "RESPONSE_HEARTBEAT:" + getLocalIPAddress();
                    byte[] buffer = response.getBytes();
                    DatagramPacket heart_packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(sender), port);
                    datagramSocket.send(heart_packet);
                    CustomLogger.logInfo("Resposta de heartbeat enviada para " + sender);
                } catch (IOException e) {
                    CustomLogger.logError("Erro ao enviar resposta de heartbeat: " + e.getMessage());
                }
            
        }else if (message.startsWith("RESPONSE_HEARTBEAT:")) {
            lastHeartbeat.put(sender, System.currentTimeMillis());
            CustomLogger.logInfo("Resposta de heartbeat recebida de " + sender + "com delay: " + (System.currentTimeMillis() - lastHeartbeat.get(sender)) + "ms");
            
        } else if (message.startsWith("MONITORIZACAO")){
            // extrair a lista de ips
            String[] parts = message.split(";");
            String pathPart = parts[1];
            pathPart = pathPart.substring(1, pathPart.length() - 1);
            List<String> path = new ArrayList<>(Arrays.asList(pathPart.split(", ")));

            // extrair a lista dos tempos
            String timesPart = parts[2];
            timesPart = timesPart.substring(1, timesPart.length() - 1);
            List<String> times = new ArrayList<>(Arrays.asList(timesPart.split(", ")));
            Long startTime = Long.parseLong(times.get(0));
            long actualTime = System.currentTimeMillis();

            int saltos = Integer.parseInt(parts[3]);

            printTabelaMonitorizacao(path, times, actualTime,startTime, saltos);
            
            saltos++;

            // adicionar o ip do oNode atual a lista de ips
            path.add(myip);

            times.add(actualTime + "");

            // reencaminhar mensagem para os filhos
            String message2 = "MONITORIZACAO" +";" + path +";"+ times +";" + saltos;
            // extrair os filhos
            List<String> childs = new ArrayList<>(root.getChildrenIPs());
            if (childs.isEmpty()) {
                return;
            }
            for (String child : childs) {
                if (!path.contains(child)){
                    sendMessageUDPToChild(message2, child, port);           
                    CustomLogger.logInfo("Monitorizacao enviada para o filho: " + child );
                }
            }

        }else if (message.startsWith("END_STREAM")){

            String titulo = message.split(";")[1];

            String senderEnd = message.split(";")[2];
            
            //o cliente já não quer ver o filme
            if (videoClientes.containsKey(titulo)){
                List<String> clientsList = videoClientes.get(titulo);

                if (clientsList.contains(senderEnd)) {
                    clientsList.remove(senderEnd);
                }
                CustomLogger.logWarning("clientlist: " + clientsList);
                if (clientsList.isEmpty() ){
                    if (!isChef){
                            String message2 = "END_STREAM" + ";" + titulo + ";" + myip;
                            sendMessageUDPToFather(message2);
                            this.videos_em_stream.remove(titulo);
                    } else {
                        //para o temporizador
                        Timer sTimer = timers.get(titulo);
                        sTimer.stop();
                        CustomLogger.logInfo("Servidor: temporizador de envio terminado.");
                        videos_em_stream.remove(titulo);
                    }
                }
            }
            CustomLogger.logInfo("Servidor: streaming do filme " + titulo + " terminado.");

        }else if (message.startsWith("NEW_NODE")) {
            handleNewNodeMessage(message, sender);

            try {
                // Se for o primeiro nó a conectar-se (chef)
                if (this.isChef && !this.isStreaming) {
                    this.isStreaming = true;
                }
            } catch (Exception e) {
                CustomLogger.logError("Servidor: erro - " + e.getMessage());
                e.printStackTrace();
            }

        } else if (message.startsWith("NODE_SUCESS")) {
            conectionssucess = true;
            
            // se a mensagem tiver 2 partes, é porque o pai é o chef
            if (message.split(";").length == 2){
                this.chefIp = sender;
                CustomLogger.logInfo("Conexão bem sucedida com o pai: " + this.chefIp);
            } else {
                this.chefIp = message.split(";")[1];
                String ip = message.split(";")[2];
                CustomLogger.logInfo("Conexão bem sucedida com o pai: " + ip);
            }

            //calcula o delay da resposta NODE_SUCESS o pedido NEW_NODE
            if (startTimeMap.containsKey(sender)){
                long endTime = System.currentTimeMillis();  
                long startTime = startTimeMap.get(sender);
                long delay = endTime - startTime;
                startTimeMap.remove(sender);
                CustomLogger.logInfo("Conexão bem sucedida com o pai: " + sender + " resposta com delay de: " + delay + "ms");
            }


        } else if (message.startsWith("HELLO")) {
            String[] parts = message.split(" ");
            CustomLogger.logInfo("Recebida mensagem " + parts[0] + " de " + parts[1]);
        } else if (message.equals("GET_POPS")) {
            handleGetPoPsUdp(packet);
        } else if (message.startsWith("PING")) {
            // Se o oNode for o chef, adiciona o seu ip a lista rota e reencaminha a mensagem para o index-1 da lista de ips
            //marca time de inicio
            startTimeMap.put(sender, System.currentTimeMillis());

            if (isChef) {
                List<String> parts = new ArrayList<>(Arrays.asList(message.split(";")));

                    int index = 0;
                    String firstPart = parts.get(0).trim();
                    if (firstPart.startsWith("PING")) {
                        String numberPart = firstPart.substring(4).trim(); // Remove "PING" e espaços

                        try {
                            index = Integer.parseInt(numberPart);
                        } catch (NumberFormatException e) {
                            CustomLogger.logError("Erro: valor '" + numberPart + "' não é um número válido.");
                        }
                    }

                    if (index == 0) {
                        // Se o oNode chef for o unico node até ao cliente 

                        parts = new ArrayList<>(parts.subList(1, parts.size()));
                        parts = new ArrayList<>(Arrays.asList(parts.get(0).split(", ")));

                        // adiciona o ip do oNode a lista de ips
                        if (!parts.contains(myip)) {
                            parts.add(myip);
                        }

                        //inverter a lista para ter a rota contraria
                        List<String> reverse = new ArrayList<>(parts);
                        Collections.reverse(reverse);
                        printTabelaRoteamento(myip, reverse);

                        String message2 = "PONG " + (index) + ";" + String.join(", ", parts);

                        if (index < parts.size()) {
                            // responde ao cliente com pong
                            sendMessageUDPToNeighbor(message2, sender, 2602);
                        } else {
                            CustomLogger.logError("Erro: Índice " + (index) + " está fora do limite da lista de IPs: " + parts.size());
                        }
                        
                    } else{

                        parts = new ArrayList<>(parts.subList(1, parts.size()));
                        parts = new ArrayList<>(Arrays.asList(parts.get(0).split(", ")));

                        // adiciona o ip do oNode a lista de ips
                        if (!parts.contains(myip)) {
                            parts.add(myip);
                        }

                        //inverter a lista para ter a rota contraria
                        List<String> reverse = new ArrayList<>(parts);
                        Collections.reverse(reverse);
                        printTabelaRoteamento(myip, reverse);

                        String message2 = "PONG " + (index) + ";" + String.join(", ", parts);

                        if (index < parts.size()) {
                            //envia ao filho do qual recebeu a mensagem
                            sendMessageUDPToChild(message2, parts.get(index), 2602);
                        } else {
                            CustomLogger.logError("Erro: Índice " + (index) + " está fora do limite da lista de IPs: " + parts.size());
                        }
                    }
            }
            
            // Se o oNode recebe de um filho
            else {
                List<String> parts = new ArrayList<>(Arrays.asList(message.split(";")));

                // sender do pacote
                String packetAddress = packet.getAddress().toString().replace("/", "");
                // se o sender nao for um filho, ele é o cliente origem
                if (!root.hasChild(packetAddress)) {
                    this.origem = packetAddress;
                    CustomLogger.logInfo("ORIGEM:" + origem);
                }

                int index = 0;
                String firstPart = parts.get(0).trim();
                if (firstPart.startsWith("PING")) {
                    String numberPart = firstPart.substring(4).trim();

                    try {
                        index = Integer.parseInt(numberPart);
                    } catch (NumberFormatException e) {
                        CustomLogger.logError("Erro: valor '" + numberPart + "' não é um número válido.");
                    }
                }

                //extrair a lista de ips
                parts = new ArrayList<>(parts.subList(1, parts.size()));
                parts = new ArrayList<>(Arrays.asList(parts.get(0).split(", ")));

                if (!parts.contains(myip)) {
                    parts.add(myip);
                }

                //inverter a lista para ter a rota contraria
                List<String> reverse = new ArrayList<>(parts);
                Collections.reverse(reverse);
                printTabelaRoteamento(myip, reverse);

                String message2 = "PING " + (index + 1) + ";" + String.join(", ", parts);
                // reencaminha a mensagem para o pai
                sendMessageUDPToChild(message2, fatherIP, fatherPort);
            }

        } else if (message.startsWith("PONG")) {
            List<String> parts = new ArrayList<>(Arrays.asList(message.split(";")));

            if (startTimeMap.containsKey(sender)){
                long endTime = System.currentTimeMillis();  
                long startTime = startTimeMap.get(sender);
                long delay = endTime - startTime;
                startTimeMap.remove(sender);
                CustomLogger.logInfo("Resposta PONG recebida de " + sender + " com delay de: " + delay + "ms");
            }

            int index = 0;
            String firstPart = parts.get(0).trim();
            if (firstPart.startsWith("PONG")) {
                String numberPart = firstPart.substring(4).trim();

                try {
                    index = Integer.parseInt(numberPart);
                } catch (NumberFormatException e) {
                    CustomLogger.logError("Erro: valor '" + numberPart + "' não é um número válido.");
                }
            }

            parts = new ArrayList<>(parts.subList(1, parts.size()));
            List<String> ips = new ArrayList<>(Arrays.asList(parts.get(0).split(", ")));
            int length = ips.size();

            // Se o oNode recebe de um pai e for o último nó da rota, o POP (ip do oNode == primeiro ip da lista)
            if (ips.contains(myip)) {
                //se o oNode é o POP
                if (myip.equals(ips.get(1))) {

                    printTabelaRoteamento(myip, ips);

                    String message2 = "PONG " + (index - 1) + ";" + String.join(", ", ips);
                    sendMessageUDPToClient(message2, origem, 2602);

                } else {

                    printTabelaRoteamento(myip, ips);

                    String message2 = "PONG " + (index - 1) + ";" + String.join(", ", ips);
                    // reencaminha a mensagem para o index-1 da lista de ips
                    sendMessageUDPToChild(message2, ips.get(index - 1), 2602);

                }
            }



        } else if (message.startsWith("BEST_POP")) {
            CustomLogger.logInfo("Recebida mensagem BEST_POP de " + packet.getAddress());
            clients.add(packet.getAddress().toString().replace("/", ""));
            // extrair o titulo do filme e adicionar ao map videoClients
            String[] parts = message.split(";");
            String titulo = parts[1];

            //add o client a lista de clientes do video
            String clientAddress = packet.getAddress().toString().replace("/", "");

            if (parts.length > 1) {
                if (videoClientes.containsKey(titulo)){
                    List<String> clientsList = videoClientes.get(titulo);
                    if (!clientsList.contains(clientAddress)) {
                        clientsList.add(clientAddress);
                    }
                } else {
                    List<String> clientsList = new ArrayList<>();
                    clientsList.add(clientAddress);
                    videoClientes.put(titulo, clientsList);
                }
            } else {
                CustomLogger.logWarning("Mensagem BEST_POP recebida sem título");
            }

            if (!videos_em_stream.contains(titulo) && !isChef) {
                String message2 = "START_STREAM" + ";" + titulo + ";" + myip;
                sendMessageUDPToFather(message2);
                this.videos_em_stream.add(titulo);

                //marcar startime
                startTimeMap.put(fatherIP, System.currentTimeMillis());
            }
            

        } else if (message.startsWith("START_STREAM")){
            
            if (isChef){
                String titulo = message.split(";")[1];
                String ip_filho = message.split(";")[2];
                
                if (videoClientes.containsKey(titulo)){
                    List<String> clientsList = videoClientes.get(titulo);
                    if (!clientsList.contains(ip_filho)) {
                        clientsList.add(ip_filho);
                    }
                } else {
                    List<String> clientsList = new ArrayList<>();
                    clientsList.add(ip_filho);
                    videoClientes.put(titulo, clientsList);
                }

                if (!videos_em_stream.contains(titulo)) {
                    Timer sTimer = timers.get(titulo);
                    sTimer.start();
                    CustomLogger.logInfo("Servidor: temporizador de envio iniciado.");
                    videos_em_stream.add(titulo);
                }
            }else{
                String titulo = message.split(";")[1];
                String ip_filho = message.split(";")[2];
                
                if (videoClientes.containsKey(titulo)){
                    List<String> clientsList = videoClientes.get(titulo);
                    if (!clientsList.contains(ip_filho)) {
                        clientsList.add(ip_filho);
                    }
                } else {
                    List<String> clientsList = new ArrayList<>();
                    clientsList.add(ip_filho);
                    videoClientes.put(titulo, clientsList);
                }

                if (!videos_em_stream.contains(titulo)) {
                    String message2 = "START_STREAM" + ";" + titulo + ";" + myip;
                    sendMessageUDPToFather(message2);
                    videos_em_stream.add(titulo);

                    //marcar startime
                    startTimeMap.put(fatherIP, System.currentTimeMillis());
                }
            }

        } else if (message.startsWith("GET_NEW_FATHER")) {
            String ipsender = message.split(";")[1];
            String node = message.split(";")[2];

            // percorrer a arvore guardada no chef e procorar o pai do node
            TreeNode aux = root;

            // a cada onode de aux ver se node tem filho
            while (aux != null){
                if (aux.hasChild(node)){ // se tiver filho
                    TreeNode nodeTree = aux.getChild(node); 
                    List<String> childs = nodeTree.getChildrenIPs();

                    for (String child : childs){ 
                        String message2 = "NEW_FATHER" + ";" + aux.getIp(); 
                        sendMessageUDPToNeighbor(message2, child, port);
                        CustomLogger.logWarning("mandamos new pai para" + child);

                        String message3 = "NEW_CHILD" + ";" + child;
                        sendMessageUDPToNeighbor(message3, aux.getIp(), port);
                        CustomLogger.logWarning("mandamos newchild para " + aux.getIp());
                    }

                    break;
                } else {
                    //ver se aux tem filhos
                    if (!aux.getChildren().isEmpty()){
                    }
                    //obter os filhos de aux
                    List<String> childs = aux.getChildrenIPs();

                    // para cada filho
                    for (String child : childs){
                        TreeNode childTree = aux.getChild(child);
                        //chamar a recursiva
                        aux = childTree;
                    }
                }
            }

        } else if (message.startsWith("NEW_FATHER")) {
            // estabelecer conexão com o novo pai
            String newFather = message.split(";")[1];
            setFather(newFather, 2602);
            CustomLogger.logWarning("Recebi que tenho un novo pai: " + newFather);

            //enviar ao novo pai a lista de filmes que quer
            if (!videoClientes.isEmpty()){
                String message2 = "FILMES" + ";" + videoClientes.keySet() + ";" + myip;
                sendMessageUDPToNeighbor(message2, newFather, port);
            }

        }else if (message.startsWith("NEW_CHILD")) {
            String newChild = message.split(";")[1];
            TreeNode newChildTree = new TreeNode(newChild);
            CustomLogger.logWarning("Recebi que tenho un novo filho: " + newChild);

            //adicionar o novo filho
            root.addChild(newChild);

            //obter o Node do novo filho
            TreeNode newChildNode = root.getChild(newChild);

            //obter o ex pai do novo filho
            String father = root.getFatherIp(newChild);

            // retirar o ex pai da arvore
            if (father != null){
                // descer para o filho do father
                TreeNode fatherTree = root.getChild(father);
                // verificar se o filho do father tem filhos
                if (fatherTree.getChildren().size() >= 1){

                    List<String> childs = fatherTree.getChildrenIPs();

                    for (String child : childs){
                        // ver se tem childs
                        TreeNode childTree = fatherTree.getChild(child);
                        List<String> childsChild = childTree.getChildrenIPs();

                        for (String childChild : childsChild){
                            newChildNode.addChild(childChild);
                            // adicionar abaixo do newChild
                            newChildTree.addChild(childChild);
                        }
                    }
                }
                root.removeChild(father);
            }

            CustomLogger.logInfo("A arvore foi atualizada: " + root.toString());

            //Remover o ex pai da lista de videoClientes
            for (Map.Entry<String, List<String>> entrada : videoClientes.entrySet()) {
                List<String> clientsList = entrada.getValue();
                if (clientsList.contains(father)) {
                    clientsList.remove(father);
                    if (clientsList.isEmpty()){
                        videos_em_stream.remove(entrada.getKey());
                    }
                }
            }


        }else if (message.startsWith("FILMES")) {
            String[] parts = message.split(";");
            String ipSender = parts[2];
            
            String filmesPart = parts[1];  
            filmesPart = filmesPart.substring(1, filmesPart.length() - 1);
            List<String> filmes = Arrays.asList(filmesPart.split(","));

            for (String filme : filmes){
                if (this.videoClientes.containsKey(filme)){
                    // adicionar o novo child aos clientes que querem este filme
                    List<String> clientsList = videoClientes.get(filme);
                    clientsList.add(ipSender);
                    this.videoClientes.put(filme, clientsList);
                }else{
                    List<String> clientsList = new ArrayList<>();
                    clientsList.add(ipSender);
                    this.videoClientes.put(filme, clientsList);
                }
            }
        }else if (message.startsWith("REMOVE_CHILD")) {
            String child = message.split(";")[1];
            removeNodeRecursively(root, child);
            CustomLogger.logWarning("Removido o filho " + child + " da árvore.");
            CustomLogger.logInfo("A arvore foi atualizada: " + root.toString());
        }else if (message.startsWith("NEW_POP")){
            //extrair ip do pop que foi abaixo
            String lastPop = message.split(";")[1];
            //remover o pop da arvore
            removeNodeRecursively(root, lastPop);

            //remover o pop da lista de pops
            pops.remove(lastPop);
            
            // percorrer videoClientes e remover o pop da lista de clientes
            for (Map.Entry<String, List<String>> entrada : videoClientes.entrySet()) {
                List<String> clientsList = entrada.getValue();
                if (clientsList.contains(lastPop)) {
                    clientsList.remove(lastPop);
                    if (clientsList.isEmpty()){
                        videos_em_stream.remove(entrada.getKey());
                    }
                }
            }

            String message2 = "RESPONSE_NEW_POP" + ";" + myip;
            sendMessageUDPToNeighbor(message2, sender, port);

        }else {
            if (startTimeMap.containsKey(sender)){
                long endTime = System.currentTimeMillis();
                long startTime = startTimeMap.get(sender);
                long delay = endTime - startTime;
                startTimeMap.remove(sender);
                CustomLogger.logInfo("Pacote RTP recebido de " + sender + " com delay de: " + delay + "ms");
            }
            
            try {
                RTPpacket rtp_packet = new RTPpacket(packet.getData(), packet.getLength());
                String titleVideoPacket = rtp_packet.getTitulo();

                for (String client : videoClientes.get(titleVideoPacket)) {
                    DatagramPacket senddp = new DatagramPacket(packet.getData(), packet.getLength(), InetAddress.getByName(client), RTP_dest_port);
                    RTPsocket.send(senddp);
                }

            } catch (Exception e) {
                CustomLogger.logError("Erro ao reencaminhar pacote: " + message + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // Método para remover um nó específico de qualquer parte da árvore
    public void removeNodeRecursively(TreeNode root, String targetNode) {
        if (root == null) return;

        // Remover o nó diretamente dos filhos do nó atual, se existir
        if (root.hasChild(targetNode)) {
            root.removeChild(targetNode);
        }

        // Percorrer recursivamente todos os filhos do nó atual
        for (Map.Entry<String, TreeNode> entry : root.getChildren().entrySet()) {
            TreeNode childNode = entry.getValue();
            removeNodeRecursively(childNode, targetNode);
        }
    }


    public void sendMessageUDPToClient(String message, String clientIP, int clientPort) {
        try {
            byte[] sendData = message.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(
                    sendData, sendData.length, InetAddress.getByName(clientIP), clientPort
            );
            datagramSocket.send(sendPacket);
            CustomLogger.logInfo("Mensagem UDP enviada para " + clientIP + ": " + message);
        } catch (IOException e) {
            CustomLogger.logError("Erro ao enviar mensagem UDP para o cliente: " + e.getMessage());
        }
    }

    public void sendMessageUDPToFather(String message) {
        try {
            byte[] sendData = message.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(
                    sendData, sendData.length, InetAddress.getByName(fatherIP), fatherPort
            );
            datagramSocket.send(sendPacket);
            CustomLogger.logInfo("Mensagem UDP enviada para " + fatherIP + ": " + message);
        } catch (IOException e) {
            CustomLogger.logError("Erro ao enviar mensagem UDP para o pai: " + e.getMessage());
        }
    }

    public void sendMessageUDPToChild(String message, String childIP, int childPort) {
        try {
            byte[] sendData = message.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(
                    sendData, sendData.length, InetAddress.getByName(childIP), childPort
            );
            datagramSocket.send(sendPacket);
            CustomLogger.logInfo("Mensagem UDP enviada para " + childIP + ": " + message);
        } catch (IOException e) {
            CustomLogger.logError("Erro ao enviar mensagem UDP para o filho: " + childIP + ": " + childPort);
            CustomLogger.logError("Erro ao enviar mensagem UDP para o filho: " + e.getMessage());
        }
    }

    private void sendMessageUDPToNeighbor(String message, String neighborIP, int neighborPort) {
        try {
            byte[] sendData = message.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(
                    sendData, sendData.length, InetAddress.getByName(neighborIP), neighborPort
            );
            datagramSocket.send(sendPacket);
            CustomLogger.logInfo("Mensagem UDP enviada para " + neighborIP + ": " + message);
            
        } catch (IOException e) {
            CustomLogger.logError("Erro ao enviar mensagem UDP para o vizinho: " + e.getMessage());
        }
    }


    ///////////////////////////////////////
    /// Métodos para comunicação RTP  /////
    ///////////////////////////////////////

    public void printTabelaRoteamento(String myip, List<String> ips){
        int length = ips.size();
        CustomLogger.logInfo("-----TABELA DE ROTEAMENTO------ " );
        CustomLogger.logInfo("|  Origem: " + myip );
        CustomLogger.logInfo("|  Destino: " + ips.get(length-1) );

        //guardar a lista dos ips de myip para a frente
        int indexMyip = 0;
            for (String ip : ips){
                if (ip.equals(myip)){
                    indexMyip = ips.indexOf(ip);
                }
            }

        List <String> newRota = new ArrayList<>(ips.subList(indexMyip, length));

        CustomLogger.logInfo("|  Rota: " + String.join(", ", newRota) );
        CustomLogger.logInfo("|  Número de saltos: " + (newRota.size() - 1) );
        CustomLogger.logInfo("-------------------------------- " );
    }


    public void printTabelaMonitorizacao(List<String> path, List<String> times, long actualTime,long startTime, int saltos){
        CustomLogger.logInfo("-----TABELA DE MONITORIZAÇÃO------ " );

        CustomLogger.logInfo("MONITORIZAÇÃO RECEBIDA: ");
            CustomLogger.logInfo("| IP do server: " + path.get(0));
            CustomLogger.logInfo("| Path: " + path);
            CustomLogger.logInfo("| Times: " + times);
            CustomLogger.logInfo("| Atraso: " + (actualTime - startTime) + "ms");
            CustomLogger.logInfo("| Saltos: " + saltos);
        CustomLogger.logInfo("-------------------------------- " );
    }

    ///////////////////////////////////////
    /// Méthodos para POPs            /////
    ///////////////////////////////////////

    public List<String> getPoPs() {
        if (root == null) {
            return new ArrayList<>(); // Retorna uma lista vazia se não houver nós
        }

        List<String> leafIPs = new ArrayList<>();
        List<String> leafNodes = root.getLeafChildren();
        for (String leaf : leafNodes) {
            leafIPs.add(leaf);
        }
        if (leafIPs.isEmpty()) {
            leafIPs.add(root.getIp());
        }
        return leafIPs;
    }

    private void handleGetPoPsUdp(DatagramPacket packet) {
        try {
            this.pops = getPoPs();
            CustomLogger.logInfo("POPs: " + pops);
            String response = String.join(",", pops);
            byte[] sendData = response.getBytes();

            DatagramPacket sendPacket = new DatagramPacket(
                    sendData, sendData.length, packet.getAddress(), packet.getPort()
            );
            datagramSocket.send(sendPacket);
            CustomLogger.logInfo("Resposta UDP enviada: " + response);
        } catch (IOException e) {
            CustomLogger.logError("Erro ao enviar resposta UDP: " + e.getMessage());
        }
    }

    ///////////////////////////////////////////////////
    /// Méthodos para Entrada de nodes na arvore  /////        
    //////////////////////////////////////////////////
    
    public void joinNetwork(String neighborIP, int neighborPort) {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        final boolean[] connectionSuccessful = {false};
        String myip = getLocalIPAddress();

        Future<?> connectionAttempt = scheduler.submit(() -> {
            String joinMessage = "NEW_NODE " + myip;
            try {
                sendMessageUDPToNeighbor(joinMessage, neighborIP, neighborPort);

                //marca o tempo de inicio
                startTimeMap.put(neighborIP, System.currentTimeMillis());

                // Aguarda a resposta do vizinho
                Thread.sleep(1000);
                if (conectionssucess) {
                    connectionSuccessful[0] = true;
                    CustomLogger.logInfo("Conectado ao nó chef com sucesso!" );
                } else {
                    CustomLogger.logError("Falha na conexão. ");
                    CustomLogger.logWarning("Não há servidor principal.");
                    connectionSuccessful[0] = false;
                }
            } catch (Exception e) {
                CustomLogger.logError("Falha ao conectar ao nó chef: " + e.getMessage());
                e.printStackTrace();
            }
        });

        scheduler.schedule(() -> {
            if (!connectionSuccessful[0]) {
                CustomLogger.logError("Falha ao conectar ao servidor principal (chef).");
                connectionAttempt.cancel(true);
                System.exit(0);
            }
            scheduler.shutdown();
        }, TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    private void handleNewNodeMessage(String message, String sender) {

        String[] parts = message.split(" ");

        if (parts.length != 2) {
            CustomLogger.logWarning("A mensagem recebida não está no formato esperado: " + message);
            return;
        }

        String caminhoIPs = parts[1];

        String[] caminhosIPsLista = caminhoIPs.split(";");

        TreeNode aux = root;
        int length = caminhosIPsLista.length;

        for (int i = 0; i < length; i++) {
            if (!aux.hasChild(caminhosIPsLista[i])) {
                aux.addChild(caminhosIPsLista[i]);
            }
            aux = aux.getChild(caminhosIPsLista[i]);
        }

        CustomLogger.logInfo("ARVORE IMPRESSA: " + root.toString());

        if (fatherIP != null) {
            String myip = getLocalIPAddress();
            String message2 = parts[0] + " " + myip + ";" + caminhoIPs;
            sendMessageUDPToFather(message2);
        }

        if (isChef) {
            // Envia ao sender que a conexão foi bem sucedida 
            String mess = "NODE_SUCESS " +";" + getLocalIPAddress();
            sendMessageUDPToChild(mess, sender, port);
        } else {
            // Envia ao sender que a conexão foi bem sucedida 
            String mess = "NODE_SUCESS " + ";" + this.chefIp + ";" +getLocalIPAddress();
            sendMessageUDPToChild(mess, sender, port);
        }
        


    }

    ////////////////////////////////////////////////////////  
    /// Méthodo para Hello message, conection control  /////        
    ///////////////////////////////////////////////////////  

    public void sendHelloMessage() {
        if (fatherIP != null && fatherPort > 0) {
            String message = "HELLO " + getLocalIPAddress();
            sendMessageUDPToFather(message);
        } else {
            CustomLogger.logWarning("Este nó não possui um vizinho para enviar a mensagem HELLO.");
        }
    }


    //////////////////////////////////////////////
    /// Méthodo para obter ip de um oNode   /////        
    /////////////////////////////////////////////
    
    private String getLocalIPAddress() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();

                if (networkInterface.isLoopback() || !networkInterface.isUp()) continue;

                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();

                    if (addr instanceof Inet4Address) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            CustomLogger.logError("Erro ao obter o endereço IP local: " + e.getMessage());
        }
        return "unknown";
    }

    public static void main(String[] args) {
        boolean isChef = args.length == 2;
        oNode node;
        
        if (!isChef) {
            // Para nós não-chef, solicita conexão ao vizinho especificado
            String[] fatherInfo = args[0].split(":");
            String fatherIP = fatherInfo[0];
            int fatherPort = Integer.parseInt(fatherInfo[1]);

            node = new oNode(fatherPort, args, isChef, null);
            node.startServer();
            node.setFather(fatherIP, fatherPort);
            node.joinNetwork(fatherIP, fatherPort);

            node.sendHeartbeatToFather();
            node.sendHeartbeatToChilds();
            node.sendHeartbeatToClients();

            ScheduledExecutorService heartbeatExecutor = Executors.newSingleThreadScheduledExecutor();
            heartbeatExecutor.scheduleAtFixedRate(() -> {
                try {
                    node.checkHeartbeats();
                } catch (Exception e) {
                    e.printStackTrace();
                    CustomLogger.logError("Erro ao verificar heartbeats: " + e.getMessage());
                }
            }, 0, 5, TimeUnit.SECONDS); // Executa a cada 5 segundos

        }

        else if (isChef) {
            int port = Integer.parseInt(args[0]);
            String caminho = args[1];

            node = new oNode(port, args, isChef, caminho);
            node.startServer();

            //manda heartbeats para os filhos e clientes
            node.sendHeartbeatToChilds();
            node.sendHeartbeatToClients();

            node.sendMonitorizacaoToChilds();
        } else {
            node = null;
        }

        new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                String input = scanner.nextLine();
                if (input.equalsIgnoreCase("HELLO")) {
                    node.sendHelloMessage();
                }
            }
        }).start();
    }
    
    ///////////////////////////////////
    /// Méthodo para Streaming   /////        
    //////////////////////////////////

    class serverTimerListener implements ActionListener {
        private String titulo_filme;

        public serverTimerListener(String titulo_filme) {
            this.titulo_filme = titulo_filme;
        }

        public void actionPerformed(ActionEvent e) {
            try {
                new Thread(() -> {
                    try {
                        VideoStream video = videos.get(this.titulo_filme);

                        int imagenb = video.getFrameNumber();

                        byte[] sBuf = new byte[15000];

                        int image_length = video.getnextframe(sBuf);
                        RTPpacket rtp_packet = new RTPpacket(26, imagenb, imagenb * 100, sBuf, image_length, this.titulo_filme);

                        // Pacote de bytes para enviar
                        int packet_length = rtp_packet.getlength();
                        byte[] packet_bits = new byte[packet_length];
                        rtp_packet.getpacket(packet_bits);

                        int packetCounter = 0; 

                        for (String client : videoClientes.get(this.titulo_filme)) {

                            // Envia o pacote como DatagramPacket
                            DatagramPacket senddp = new DatagramPacket(packet_bits, packet_length, InetAddress.getByName(client), RTP_dest_port);

                            RTPsocket.send(senddp);

                            CustomLogger.logInfo("Servidor: enviando quadro #" + packetCounter + " do video:" + this.titulo_filme + "para o ip: " + client);
                            packetCounter ++;
                        }

                    } catch (Exception ex) {
                        CustomLogger.logError("Servidor: erro ao enviar quadro: " + ex.getMessage());
                        ex.printStackTrace();
                        timers.get(this.titulo_filme).stop(); // Para o temporizador em caso de erro
                    }
                }).start();
            } catch (Exception ex){
                ex.printStackTrace();
            }
        }

    }
}