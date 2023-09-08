package View;

import Model.Artigo;
import Model.Encomenda;
import Model.Transportadora;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class View {
    public static void imprime(String x) {
        System.out.println("-> " + x);
    }

    public static void sucesso(String x) {
        System.out.println("->\u001B[32m" + x + "\u001B[0m");
    }

    public static void erro(String x) {
        System.out.println("->\u001B[31m" + x + "\u001B[0m");
    }

    public static void header(String input, float saldo) {
        int width = 150; // largura da janela do terminal

        // calcula o número de espaços em branco para colocar antes e depois da string central
        int stringWidth = input.length() + 2; // adicione 2 para aspas
        int padding = (width - stringWidth) / 2;

        // imprime o cabeçalho
        System.out.println();
        System.out.println("+" + "-".repeat(width - 2) + "+");
        String first = "|" + " ".repeat(padding) + "\"" + input + "\"";
        System.out.println(first + " ".repeat(Math.max(0, width-first.length()-1)) + "|");
        first = "|" + " ".repeat(padding) + "Saldo: " + saldo;
        System.out.println(first + " ".repeat(Math.max(0, width-first.length()-1)) + "|");
        System.out.println("+" + "-".repeat(width - 2) + "+");
    }

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

    public static void menuPrincipal(float saldo) {
        View.header("Menu Principal", saldo);
        List<String> opcoes = Arrays.asList(
                "1 --- Artigos",
                "2 --- Meus Artigos",
                "3 --- Minhas Vendas",
                "4 --- Editar perfil",
                "5 --- As minhas Encomendas",
                "6 --- Criar um novo Artigo",
                "7 --- Criar uma nova transportadora",
                "8 --- Métricas",
                "9 --- Avançar no tempo",
                "10 --- Ver carrinho",
                "11 --- Editar transportadora",
                "0 --- LogOut");
        cabecalho(opcoes);
    }

    public static void menuMetricas(float saldo) {
        View.header("Metricas", saldo);
        List<String> opcoes = Arrays.asList(
                "1 --- Vendedor que mais faturou",
                "2 --- Vendedor que mais faturou num intervalo de tempo",
                "3 --- Transportadora que mais faturou",
                "4 --- Listar as encomendas emitidas por um vendedor",
                "5 --- Maior vendedor",
                "6 --- Maior comprador",
                "7 --- Lucro da Vintage no seu funcionamento",
                "0 --- Sair");
        cabecalho(opcoes);
    }

    public static void menuEditarPerfil(float saldo) {
        View.header("Editar perfil", saldo);
        List<String> opcoes = Arrays.asList(
                "1 --- Alterar palavra passe",
                "2 --- Alterar morada",
                "3 --- Editar saldo",
                "0 --- Sair");
        cabecalho(opcoes);
    }

    public static void paginador(List<String> options) {
        List<String> opcoes = new ArrayList<>(Arrays.asList(
                "1 --- Avancar",
                "2 --- Recuar"));
        opcoes.addAll(options);
        opcoes.add("0 --- Sair");
        System.out.println("+" + "-".repeat(150 - 2) + "+");
        cabecalho(opcoes);
    }


    public static void imprimeObj(List<Object> lista, int a) {

        int inicio = 10 * (a - 1);
        int fim = a * 10;

        int size = lista.size();
        if (size==0) {
            erro("Lista Vazia.");
            return;
        }
        if (size < fim) fim = lista.size();

        List<String> header = null;

        if (lista.get(0) instanceof Artigo)
            header = headerArtigo;
        else if (lista.get(0) instanceof Encomenda)
            header = headerEncomenda;
        else if (lista.get(0) instanceof Transportadora)
            header = headerTransportadora;

        int larguraColuna = 20;
        int numColunas = header!= null ? header.size(): 2;
        int larguraTotal = numColunas * larguraColuna;

        if (header != null)
            imprimeLinha(header, larguraColuna, larguraTotal);

        for (; inicio < fim; inicio++) {
            Object obj = lista.get(inicio);
            List<String> linha = new ArrayList<>();
            linha.add(Integer.toString(inicio));
            linha.addAll(List.of(obj.toString().split(";<>;")));
            imprimeLinha(linha, larguraColuna, larguraTotal);
        }
    }

    private static void imprimeLinha(List<String> linha, int larguraColuna, int larguraTotal) {
        StringBuilder sb = new StringBuilder();
        for (String dado : linha) {
            int larguraDado = dado.length();
            int espacoEsquerda = Integer.max((larguraColuna - larguraDado) / 2, 0);
            int espacoDireita = Integer.max(larguraColuna - larguraDado - espacoEsquerda, 0);
            sb.append(" ".repeat(espacoEsquerda))
                    .append(dado)
                    .append(" ".repeat(espacoDireita));
        }
        String linhaFormatada = sb.toString();
        int espacoEsquerda = Integer.max((larguraTotal - linhaFormatada.length()) / 2, 0);
        System.out.printf("%s%s%n", " ".repeat(espacoEsquerda), linhaFormatada);
    }

    public static List<String> headerArtigo = Arrays.asList(
            "Index", "Nome", "Usado", "Marca", "Tamanho", "Preço", "Estado", "Número de donos", "Vendido", "Transportadora", "Descrição"
    );
    public static List<String> headerEncomenda = Arrays.asList(
            "Index", "Dimensão", "Preço dos artigos", "Estado", "Data de criação", "Data de finalização", "Data de expedição"
    );
    public static List<String> headerTransportadora = Arrays.asList(
            "Index", "Nome", "Margem de lucro", "Encomenda Pequena (€)", "Encomenda Média (€)", "Encomenda Grande (€)", "Email do Criador"
    );

    public static void menuEditarTransp(float saldo){
        View.header("Editar Transportadora", saldo);
        List<String> opcoes = Arrays.asList(
                "1 --- Alterar margem de lucro",
                "2 --- Alterar valor pequeno",
                "3 --- Alterar valor médio",
                "4 --- Alterar valor grande",
                "0 --- Sair");
        cabecalho(opcoes);
    }

    public static void imprimeLista(Map<String, Integer> result) {
        List<String> toStr = new ArrayList<>();
        for (Map.Entry<String,Integer> i : result.entrySet()){
            toStr.add(i.getKey() + ";<>;" + i.getValue());
        }
        imprimeObj(new ArrayList<>(toStr), 1);
    }
}