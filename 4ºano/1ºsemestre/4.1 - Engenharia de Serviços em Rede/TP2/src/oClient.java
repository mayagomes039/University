package src;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import javax.swing.*;
import javax.swing.Timer;


public class oClient {
    private String chefIP;
    private int chefPort;
    private DatagramSocket socket;
    private final int BUFFER_SIZE = 30000;
    private final int TIMEOUT = 9000; // Tempo de espera para resposta em ms
    private List<String> pointsOfPresence;
    private Map<String, List<String>> rotas = new HashMap<>();
    private String titulo;

    //streaming
    private final int RTP_PORT = 2602;
    private final int CONNECT_PORT = 2602;
    //private DatagramSocket RTPsocket;
    private InetAddress bestPoPAddress;
    private Timer clientTimer;
    private byte[] cBuf = new byte[15000];
    private boolean recebido = false;

    // GUI components
    private JFrame f = new JFrame("Cliente - Teste Video");
    private JButton playButton = new JButton("Play");
    private JButton exitButton = new JButton("Exit");
    private JPanel mainPanel = new JPanel();
    private JPanel buttonPanel = new JPanel();
    private JLabel iconLabel = new JLabel();
    private ImageIcon icon;

    // Sistema de heartbeat
    private static int HEARTBEAT_INTERVAL = 5000;  
    private static int HEARTBEAT_TIMEOUT = 30000; 
    private ScheduledExecutorService heartbeatExecutor;
    private Map<String, Long> lastHeartbeatResponse = new HashMap<>();

    //delay
    private ConcurrentHashMap<String, Long> startTimeMap = new ConcurrentHashMap<>();

    public oClient(String chefIP, int chefPort, String titulo) {
        // Inicialização de variáveis
        this.chefIP = chefIP;
        this.chefPort = chefPort;
        this.pointsOfPresence = new ArrayList<>();
        this.titulo = titulo;

        try {
            this.socket = new DatagramSocket(CONNECT_PORT);
            this.socket.setSoTimeout(TIMEOUT); // Define o tempo de espera para a resposta

            CustomLogger.logInfo("Cliente: Socket RTP criado, aguardando pacotes na porta " + RTP_PORT);

        } catch (SocketException e) {
            CustomLogger.logError("Erro ao inicializar o socket UDP: " + e.getMessage());
        }
        heartbeatExecutor = Executors.newSingleThreadScheduledExecutor();
    }

    class playButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {

            CustomLogger.logInfo("Play Button pressionado!");

            if (clientTimer != null) {
                CustomLogger.logInfo("Timer start!");
                if (clientTimer == null) {
                    CustomLogger.logError("Erro: clientTimer não foi inicializado.");
                }
                clientTimer.start(); // Inicia o temporizador para receber pacotes
            } else {
                CustomLogger.logError("Erro: clientTimer não foi inicializado.");
            }
        }
    }

    class exitButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {

            CustomLogger.logInfo("Exit Button pressionado!");

            if (clientTimer != null) {
                CustomLogger.logInfo("Timer stop!");
                clientTimer.stop(); // Para o temporizador
                System.exit(0);
            } else {
                CustomLogger.logError("Erro: clientTimer não foi inicializado.");
            }
        }
    }

    ///////////////////////////////////
    /// Méthodos para Heartbeats  /////        
    //////////////////////////////////


    // Envia heartbeats e monitora respostas
    private void startHeartbeatCheck() {
        // executa o envio de heartbeats a cada 5 segundos
        heartbeatExecutor.scheduleAtFixedRate(() -> {
            try {
                String myip = getLocalIPAddress();
                CustomLogger.logInfo("Enviando heartbeat da parte de: " + myip + " para o melhor PoP: " + bestPoPAddress.getHostAddress());
                // Envia heartbeat
                String heartbeatMessage = "HEARTBEAT:" + getLocalIPAddress();
                byte[] buffer = heartbeatMessage.getBytes();
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, bestPoPAddress, chefPort);
                //RTPsocket.send(packet);
                socket.send(packet);
                startTimeMap.put(bestPoPAddress.getHostAddress(), System.currentTimeMillis());
                CustomLogger.logInfo("Heartbeat enviado para o melhor PoP.");

                //enviar heartbeat para o chef tbm
                byte[] buffer2 = heartbeatMessage.getBytes();
                DatagramPacket packet2 = new DatagramPacket(buffer2, buffer2.length, InetAddress.getByName(chefIP), chefPort);
                //RTPsocket.send(packet2);
                socket.send(packet2);
                startTimeMap.put(chefIP, System.currentTimeMillis());
                CustomLogger.logInfo("Heartbeat enviado para o chef.");

                if (!lastHeartbeatResponse.isEmpty()){

                //percorrer o lastHeartbeatResponse para ver se a resposta é do chef ou pop
                for (Map.Entry<String, Long> entry : lastHeartbeatResponse.entrySet()) {
                    String key = entry.getKey();
                    Long value = entry.getValue();

                    if (Objects.equals(key, bestPoPAddress.getHostAddress())) {
                        // Verifica se o PoP respondeu dentro do intervalo de timeout
                        if (System.currentTimeMillis() - value > HEARTBEAT_TIMEOUT) {
                            CustomLogger.logError("Erro: Nenhuma resposta do melhor PoP dentro do timeout. Tentando ligar a outro pop.");
                            CustomLogger.logError("Estamos a ter perdas de pacotes.");
                            socket.close();
                            heartbeatExecutor.shutdownNow();

                            pointsOfPresence.clear();

                            String newPopMessage = "NEW_POP" + ";" + bestPoPAddress.getHostAddress();
                            byte[] newPopBuffer = newPopMessage.getBytes();
                            DatagramPacket newPopPacket = new DatagramPacket(newPopBuffer, newPopBuffer.length, InetAddress.getByName(chefIP), chefPort);
                            socket = new DatagramSocket(CONNECT_PORT);
                            socket.setSoTimeout(TIMEOUT);
                            socket.send(newPopPacket);
                            CustomLogger.logInfo("Mensagem NEW_POP enviada para o chef:" + chefIP);

                            startTimeMap.put(chefIP, System.currentTimeMillis());
                            CustomLogger.logInfo("Pedido de NEW PoPs enviado para o chef.");

                        }
                    } else if (Objects.equals(key, chefIP)) {
                        if (System.currentTimeMillis() - value > HEARTBEAT_TIMEOUT) {
                            CustomLogger.logWarning("Erro: Nenhuma resposta do chef. Não há mais pops disponiveis.");
                            shutdownClient();
                        }
                    }
                }
            }
            } catch (SocketTimeoutException e) {
                CustomLogger.logWarning("Erro: Nenhuma resposta do chef. Não há mais pops disponiveis.");
                shutdownClient();
            } catch (IOException e) {
                CustomLogger.logError("Erro ao enviar heartbeat: " + e.getMessage());
                shutdownClient();
            }
        }, 0, HEARTBEAT_INTERVAL, TimeUnit.MILLISECONDS);
    }

    // Recebe resposta de heartbeat
    private void handleHeartbeatResponse(DatagramPacket packet) {
        String message = new String(packet.getData(), 0, packet.getLength());
        String sender = packet.getAddress().toString().replace("/", "");
        if (message.startsWith("RESPONSE_HEARTBEAT")) {
            String sender2 = message.split(":")[1];

            if (sender.equals(chefIP)){
                lastHeartbeatResponse.put(sender, System.currentTimeMillis());
                if (startTimeMap.get(sender)!=null){
                    CustomLogger.logInfo("Resposta de heartbeat recebida do chef. Com delay: " + (System.currentTimeMillis() - startTimeMap.get(sender)) + "ms");
                }
            }else{
                lastHeartbeatResponse.put(sender2, System.currentTimeMillis());
                if (startTimeMap.get(sender2)!=null){
                    CustomLogger.logInfo("Resposta de heartbeat recebida do Pop. Com delay: " + (System.currentTimeMillis() - startTimeMap.get(sender2)) + "ms");
                }
            }
        }
    }

    // Método para desligar o cliente com uma mensagem de erro
    private void shutdownClient() {
        if (heartbeatExecutor != null && !heartbeatExecutor.isShutdown()) {
            heartbeatExecutor.shutdownNow();
        }
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
        if (clientTimer != null && clientTimer.isRunning()) {
            clientTimer.stop();
        }

        CustomLogger.logError("Cliente desligado devido à falha do servidor principal.");
        System.exit(1); // Encerra a aplicação com erro
    }

    // Envia pedido UDP de PoPs para o chef e recebe a lista de PoPs disponíveis
    public void requestPointsOfPresence() {
        try {
            byte[] sendData = "GET_POPS".getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(chefIP), chefPort);
            socket.send(sendPacket);

            startTimeMap.put(chefIP, System.currentTimeMillis());
            CustomLogger.logInfo("Pedido de PoPs enviado para o chef.");

            byte[] receiveData = new byte[BUFFER_SIZE];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

            // Aguarda resposta do chef
            socket.receive(receivePacket);
            String response = new String(receivePacket.getData(), 0, receivePacket.getLength());
            
            CustomLogger.logInfo("Resposta recebida do chef: " + response);

            // Processa a lista de PoPs recebida do chef
            parsePoPs(response);
        } catch (SocketTimeoutException e) {
            CustomLogger.logWarning("Tempo limite alcançado. Nenhuma resposta do chef.");
        } catch (IOException e) {
            CustomLogger.logError("Erro ao solicitar PoPs: " + e.getMessage());
        }
    }

    // Processa a resposta recebida com os IPs dos Points of Presence
    private void parsePoPs(String response) {
        String[] pops = response.split(",");
        for (String pop : pops) {
            pointsOfPresence.add(pop.trim());
        }

        // Calcular o delay da get_pops
        long endTime = System.currentTimeMillis();  
        long startTime = startTimeMap.get(chefIP);
        long delay = endTime - startTime;
        startTimeMap.remove(chefIP);
        CustomLogger.logInfo("Points of Presence disponíveis: " + pointsOfPresence + ". Resposta recebida com delay de " + delay + "ms");
    }

    // Escolher o melhor PoP (melhor caminho) baseado na latência
    public void chooseBestPoP() throws SocketException {
        ExecutorService executor = Executors.newFixedThreadPool(pointsOfPresence.size());
        Map<String, Long> responseTimes = new HashMap<>();
        CountDownLatch latch = new CountDownLatch(pointsOfPresence.size());

        for (String pop : pointsOfPresence) {
            executor.submit(() -> {

                try {
                    long startTime = System.nanoTime();

                    int index = 0;

                    String message = "PING " + index + "; " + getLocalIPAddress();

                    byte[] sendData = message.getBytes();

                    // Envio do pacote para o PoP
                    InetAddress popAddress = InetAddress.getByName(pop);
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, popAddress, 2602);
                    CustomLogger.logInfo("Enviando pacote para " + pop + " na porta 2602 com a mensagem: " + message);
                    socket.send(sendPacket);
                    CustomLogger.logInfo("Ping enviado para " + pop);

                    boolean recebido = false;

                    while (!recebido) {
                        try {
                            socket.setSoTimeout(20000); // Timeout de 20 segundos para receber
                            byte[] receiveData = new byte[BUFFER_SIZE];
                            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                            socket.receive(receivePacket);//tempSocket.receive(receivePacket);

                            recebido = true; // Se receber, sai do loop
                            long endTime = System.nanoTime();
                            long responseTime = (endTime - startTime) / 1_000_000;
                            responseTimes.put(pop, responseTime);

                            //extrair a lista de ips da mensagem 
                            String response = new String(receivePacket.getData(), 0, receivePacket.getLength());
                            List<String> parts = new ArrayList<>(Arrays.asList(response.split(";")));
                            parts = new ArrayList<>(parts.subList(1, parts.size()));

                            //extrair primeiro elemento da lista que é o pop
                            List<String> ips = new ArrayList<>(Arrays.asList(parts.get(0).split(", ")));
                            String popATUAL = ips.get(1);

                            //Guardar no map rotas de ips junto com o pop
                            rotas.put(popATUAL, ips);

                            CustomLogger.logInfo("Resposta recebida de " + pop + " em " + responseTime + " ms");
                        } catch (SocketTimeoutException e) {
                            CustomLogger.logWarning("Timeout ao esperar resposta de " + pop);
                        }
                    }
                } catch (Exception e) {
                    CustomLogger.logError("Erro ao enviar/receber para " + pop + ": " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            latch.await();
            executor.shutdown();

            // Encontrar o PoP com o menor tempo de resposta
            if (!responseTimes.isEmpty()) {
                String bestPoP = null;

                if (responseTimes.size()==1) {
                    bestPoP = responseTimes.keySet().iterator().next();
                } else {
                    bestPoP = Collections.min(responseTimes.entrySet(), Map.Entry.comparingByValue()).getKey();
                    // em caso de empate escolher o que tem menor numero de saltos
                    for (String pop : responseTimes.keySet()) {
                        if (responseTimes.get(pop) == responseTimes.get(bestPoP)) {
                            if (rotas.get(pop).size() < rotas.get(bestPoP).size()) {
                                bestPoP = pop;
                            } else if (rotas.get(pop).size() == rotas.get(bestPoP).size()) {
                                //randomizer
                                Random random = new Random();
                                int randomInt = random.nextInt(2);
                                if (randomInt == 1) {
                                    bestPoP = pop;
                                }
                            }
                        }
                    }
                }

                List<String> bestRota = rotas.get(bestPoP);
                int length = bestRota.size();

                //TABELA DE ROTEAMENTO
                    CustomLogger.logInfo("-----TABELA DE ROTEAMENTO------ " );
                    CustomLogger.logInfo("|  Melhor POP: " + bestPoP );
                    CustomLogger.logInfo("|  Tempo de resposta do POP: " + responseTimes.get(bestPoP) + " ms");
                    CustomLogger.logInfo("|  Origem: " + getLocalIPAddress() );
                    CustomLogger.logInfo("|  Destino (SERVIDOR): " + bestRota.get(length-1));
                    CustomLogger.logInfo("|  Rota: " + rotas.get(bestPoP));
                    CustomLogger.logInfo("|  Número de saltos: " + (length - 1) );
                    CustomLogger.logInfo("-------------------------------- " );

                try {
                    bestPoPAddress = InetAddress.getByName(bestPoP);
                    CustomLogger.logInfo("Conectando ao melhor PoP: " + bestPoP);

                    sendBestPopMessage();
                    startTimeMap.put(bestPoP, System.currentTimeMillis());

                    // Inicializa o temporizador para receber pacotes
                    clientTimer = new Timer(20, new clientTimerListener());
                    clientTimer.setInitialDelay(0);
                    clientTimer.setCoalesce(true);
                    openGUI();
                    
                    CustomLogger.logInfo("Iniciando o receber do vídeo pelo cliente.");
                } catch (UnknownHostException e) {
                    CustomLogger.logError("Erro ao resolver endereço do PoP: " + e.getMessage());
                }
            } else {
                CustomLogger.logWarning("Nenhum PoP respondeu.");
            }
        } catch (InterruptedException e) {
            CustomLogger.logError("Erro ao esperar as respostas dos PoPs: " + e.getMessage());
        }
    }

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

    private void openGUI(){
        // Construção da GUI
        buttonPanel.setLayout(new GridLayout(1, 0));
        buttonPanel.add(playButton);
        buttonPanel.add(exitButton);
        playButton.addActionListener(new playButtonListener());
        exitButton.addActionListener(new exitButtonListener());

        iconLabel.setIcon(null);
        mainPanel.setLayout(null);
        mainPanel.add(iconLabel);
        mainPanel.add(buttonPanel);
        iconLabel.setBounds(0, 0, 380, 280);
        buttonPanel.setBounds(0, 280, 380, 50);

        f.getContentPane().add(mainPanel, BorderLayout.CENTER);
        f.setSize(new Dimension(390, 370));
        f.setVisible(true);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    //função que envia uma mensagem a informar o pop que foi escolhido como melhor pop
    private void sendBestPopMessage() {
        try {
            // Enviar "BEST_POP" + ";" + titulo
            byte[] sendData = ("BEST_POP" + ";" + titulo).getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, bestPoPAddress, chefPort);
            socket.send(sendPacket);
            CustomLogger.logInfo("Mensagem BEST_POP enviada para o PoP.");
        } catch (IOException e) {
            CustomLogger.logError("Erro ao enviar mensagem BEST_POP: " + e.getMessage());
        }
    }

    // Retorna todos os PoPs
    public List<String> getPointsOfPresence() {
        return pointsOfPresence;
    }

    public static void main(String[] args) throws SocketException {
        if (args.length < 3) {
            CustomLogger.logWarning("Uso correto: java oClient <ChefIP> <ChefPort> <Titulo do filme que deseja assistir>");
            return;
        }

        String chefIP = args[0];
        int chefPort = Integer.parseInt(args[1]);

        //guardar o titulo do filme
        String titulo = args[2];

        oClient client = new oClient(chefIP, chefPort, titulo);
        client.requestPointsOfPresence();

        // Exibir todos os PoPs recebidos
        CustomLogger.logInfo("Lista completa de PoPs disponíveis: " + client.getPointsOfPresence());

        //Escolher o melhor caminho
        client.chooseBestPoP();

        client.startHeartbeatCheck();

    }

    class clientTimerListener implements ActionListener {
        private int packetCounter = 0;
        public void actionPerformed(ActionEvent e) {
            try {

                DatagramPacket rcvdp = new DatagramPacket(cBuf, cBuf.length);

                socket.receive(rcvdp);
                packetCounter++;

                CustomLogger.logInfo("Cliente: Pacote RTP recebido");

                // Verifica se a mensagem é uma resposta de heartbeat
                String message = new String(rcvdp.getData(), 0, rcvdp.getLength());
                if (message.startsWith("RESPONSE_HEARTBEAT")) {
                    handleHeartbeatResponse(rcvdp);
                    return; 
                } else if (message.startsWith("RESPONSE_NEW_POP")) {
                    CustomLogger.logInfo("Recebido pedido de mudança de PoP. Atualizando lista de PoPs.");
                    requestPointsOfPresence();
                    chooseBestPoP();
                    return;
                }

                RTPpacket rtp_packet = new RTPpacket(rcvdp.getData(), rcvdp.getLength());

                //CustomLogger.logInfo("Cliente: Pacote RTP com SeqNum # " + rtp_packet.getsequencenumber() +
                  //      " TimeStamp " + rtp_packet.gettimestamp() + " ms, Tipo " + rtp_packet.getpayloadtype());

                CustomLogger.logInfo("Cliente: Pacote RTP: " +
                        " TimeStamp " + rtp_packet.gettimestamp() + " ms, Tipo " + rtp_packet.getpayloadtype());

                int payload_length = rtp_packet.getpayload_length();
                byte[] payload = new byte[payload_length];
                rtp_packet.getpayload(payload);

                Toolkit toolkit = Toolkit.getDefaultToolkit();
                Image image = toolkit.createImage(payload, 0, payload_length);

                icon = new ImageIcon(image);
                iconLabel.setIcon(icon);


            } catch (SocketTimeoutException ste) {
                CustomLogger.logWarning("Cliente: Timeout - Nenhum pacote recebido no intervalo");
            } catch (IOException ioe) {
                CustomLogger.logError("Cliente: Exceção - " + ioe.getMessage());
            } catch (Exception ex) {
                CustomLogger.logError("Cliente: Exceção - " + ex.getMessage());
            }
        }
    }
}