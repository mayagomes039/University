package View;

import Model.Cliente;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class View {

    public static void cabecalho(List<String> strings) {
        int width = 150; // largura da janela do terminal
        int groupSize = 3; // número de strings por grupo

        // imprime as strings em grupos de três, centralizadas e com quebra de linha após cada grupo
        for (int i = 0; i < strings.size(); i += groupSize) {
            int endIndex = Math.min(i + groupSize, strings.size());
            List<String> group = strings.subList(i, endIndex);

            // calcula o número de espaços em branco antes e depois do grupo centralizado
            int groupWidth = group.stream().mapToInt(String::length).sum() + (group.size() - 1) * 2;
            int padding = Math.max(0,(width - groupWidth) / 2);

            // imprime o grupo centralizado com quebra de linha após cada grupo
            String first = "|" + " ".repeat(padding) + String.join("  ", group);
            System.out.println(first + " ".repeat(Math.max(0, width-first.length()-1)) + "|");
        }
        System.out.println("_".repeat(width));
        System.out.print(">> ");
    }

    public static void header(String input) {
        int width = 150; // largura da janela do terminal

        // calcula o número de espaços em branco para colocar antes e depois da string central
        int stringWidth = input.length() + 2; // adicione 2 para aspas
        int padding = (width - stringWidth) / 2;

        // imprime o cabeçalho
        System.out.println();
        System.out.println("+" + "-".repeat(width - 2) + "+");
        String first = "|" + " ".repeat(padding) + "\"" + input + "\"";
        System.out.println(first + " ".repeat(Math.max(0, width-first.length()-1)) + "|");
        first = "|" + " ".repeat(padding);
        System.out.println(first + " ".repeat(Math.max(0, width-first.length()-1)) + "|");
        System.out.println("+" + "-".repeat(width - 2) + "+");
    }

}
