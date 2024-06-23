import java.util.HashMap;
import java.util.Map;

public class InfoNode {
    private String nodeId;
    private String nodeName;
    private Map<String, Integer> filesAndBlocks;

    public InfoNode(String nodeId) {
        this.nodeId = nodeId;
        this.nodeName = null;
        this.filesAndBlocks = new HashMap<>();
    }

    public void addFile(String fileName, int numBlocos) {
        // Verifica se o arquivo já existe na lista
        if (!filesAndBlocks.containsKey(fileName)) {
            filesAndBlocks.put(fileName,numBlocos);
            CustomLogger.logInfo("Arquivo '" + fileName + " com " + numBlocos + " blocos " + "' adicionado ao nó '" + nodeId + "'.");
        } else {
            filesAndBlocks.put(fileName,Math.max(numBlocos, filesAndBlocks.get(fileName)));
            CustomLogger.logInfo("Arquivo '" + fileName + " com " + numBlocos + " blocos " + "' adicionado ao nó '" + nodeId + "'.");
        }
    }


    public int getNumBlocks(String fileName) {
        return filesAndBlocks.get(fileName);
    }


    public String getNodeId() {
        return nodeId;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public Map<String, Integer> getFilesAndBlocks() {
        return filesAndBlocks;
    }

    public void setFilesAndBlocks(Map<String, Integer> filesAndBlocks) {
        this.filesAndBlocks = filesAndBlocks;
    }
}


