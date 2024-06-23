

public class Main {
    public static void main(String[] args) {
        try {
            new Controler.Controler().menuCorre();
        }
        catch (Exception e) {
            System.out.println("Erro fatal: "+e.getMessage()+" ["+ e +"]");
        }
    }
}