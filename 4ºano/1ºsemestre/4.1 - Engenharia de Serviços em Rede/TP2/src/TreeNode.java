package src;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TreeNode {
    private String ip;
    private Map<String,TreeNode> children;

    public TreeNode(String ip) {
        this.ip = ip;
        this.children = new HashMap<>();
    }

    public String getIp() {
        return ip;
    }
    public Map<String,TreeNode> getChildren() {
        return children;
    }

    public void addChild(String childIP) {
        children.put(childIP, new TreeNode(childIP));
    }

    public List<String> getMyChildrenIPs() {
        return new ArrayList<>(children.keySet());
    }

    // Retorna as folhas da árvore
    public List<String> getLeafChildren() {
        List<String> leafChildren = new ArrayList<>();
        for (TreeNode child : children.values()) {
            //CustomLogger.logInfo("Este é o pai" + child.getIp());
            //CustomLogger.logInfo("Este são os filhos" + child.getChildren());
            if (child.getChildren().isEmpty()) {
                leafChildren.add(child.getIp());
            } else{
                leafChildren.addAll(child.getLeafChildren());
            }
        }
        return leafChildren;
    }

    public boolean hasChild(String ip) {
        return this.getChildren().containsKey(ip);
    }

    public TreeNode getChild(String ip) {
        return this.getChildren().get(ip); // retorna o filho com o ip passado
    }

    public String getFatherIp (String ip) {
        for (TreeNode child : this.getChildren().values()) {
            if (child.getChildren().containsKey(ip)) {
                return child.getIp();
            } else {
                return child.getFatherIp(ip);
            }
        }
        return null;
    }

    public void removeChild(String ip) {
        // remover o filho e os filhos do filho
        this.getChildren().remove(ip);
    }

    //obter os filhos (primeiro grau) de um nó
    public List<String> getChildrenIPs() {
        return new ArrayList<>(this.getChildren().keySet());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("IP: ").append(ip).append(" --- Filhos: ").append(this.getChildren().keySet().toString()).append("\n");
        for (TreeNode child : this.getChildren().values()) {
            sb.append(child.toString());
        }
        return sb.toString();
    }
}