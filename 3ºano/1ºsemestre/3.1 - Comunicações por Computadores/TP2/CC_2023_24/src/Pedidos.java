import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Pedidos {
    private DatagramSocket serverSocket;
    private InetAddress clientAddress;
    private int clientPort;
    private int nextBlockNumber;
    private String filename;


    public Pedidos(DatagramSocket serverSocket, InetAddress clientAddress, int clientPort, int nextBlockNumber, String filename) {
        this.serverSocket = serverSocket;
        this.clientAddress = clientAddress;
        this.clientPort = clientPort;
        this.nextBlockNumber = nextBlockNumber;
        this.filename = filename;
    }

    public DatagramSocket getServerSocket() {
        return serverSocket;
    }

    public void setServerSocket(DatagramSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public InetAddress getClientAddress() {
        return clientAddress;
    }

    public void setClientAddress(InetAddress clientAddress) {
        this.clientAddress = clientAddress;
    }

    public int getClientPort() {
        return clientPort;
    }

    public void setClientPort(int clientPort) {
        this.clientPort = clientPort;
    }

    public int getNextBlockNumber() {
        return nextBlockNumber;
    }

    public void setNextBlockNumber(int nextBlockNumber) {
        this.nextBlockNumber = nextBlockNumber;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
}

