import java.io.Serializable;

public class NodeTrackerMessage implements Serializable {
    private String action;
    private Object conteudo; // register: map<nomeficheiro, numblocos> ; getlocal: nomeficheiro


    public NodeTrackerMessage(String action, Object conteudo) {
        this.action = action;
        this.conteudo = conteudo;
    }

    public NodeTrackerMessage(){
        this.action = "";
        this.conteudo = "";
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Object getConteudo() {
        return conteudo;
    }

    public void setConteudo(Object conteudo) {
        this.conteudo = conteudo;
    }

}