import java.io.FileOutputStream;
import java.io.IOException;

public class CriarArquivo {

    public static void main(String[] args) {
        String nomeDoArquivo = "arquivo.bin";
        int tamanhoDesejado = 1024;

        try (FileOutputStream fos = new FileOutputStream(nomeDoArquivo)) {
            // Preencha o arquivo com bytes. Neste exemplo, estamos preenchendo com zeros.
            for (int i = 0; i < tamanhoDesejado; i++) {
                fos.write('a');
            }

            for (int i = 0; i < 7; i++) {
                fos.write("Tem de estar a frase toda.".getBytes());
            }

            System.out.println("Arquivo criado com sucesso: " + nomeDoArquivo);
        } catch (IOException e) {
            System.err.println("Erro ao criar o arquivo: " + e.getMessage());
        }
    }
}