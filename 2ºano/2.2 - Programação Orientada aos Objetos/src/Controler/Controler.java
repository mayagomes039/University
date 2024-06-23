package Controler;

import Model.*;
import View.View;

import java.io.*;
import java.lang.reflect.Array;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class Controler implements Serializable {
    private String user_atual;
    private int diasFrente;
    private Map<String, Utilizador> user; //a string vai ser o email
    private Map<String, Encomenda> hash_encomenda; //a string vai ser o ID_E
    private Map<String, Transportadora> hash_transportadora;
    private Map<String, Artigo> hash_artigo; // a str e o codigo_alfanumerico
    private transient Scanner scanner = new Scanner(System.in);
    private Encomenda carrinho;
    private float imposto = 5;
    private float lucroVintage;


    public Controler() {
        this.user_atual = "";
        this.diasFrente = 0;
        this.user = new HashMap<>();
        this.hash_encomenda = new HashMap<>();
        this.hash_transportadora = new HashMap<>();
        this.hash_artigo = new HashMap<>();
        this.carrinho = new Encomenda();
        this.imposto = 0;
        this.lucroVintage = 0;
    }

    public float getImposto() {
        return imposto;
    }

    public void setImposto(float imposto) {
        this.imposto = imposto;
    }

    public String getUser_atual() {
        return user_atual;
    }

    public void setUser_atual(String user_atual) {
        this.user_atual = user_atual;
    }

    public Map<String, Utilizador> getUser() {
        return user;
    }

    public void setUser(HashMap<String, Utilizador> user) {
        this.user = user;
    }

    public Map<String, Encomenda> getHash_encomenda() {
        return hash_encomenda;
    }

    public void setHash_encomenda(HashMap<String, Encomenda> hash_encomenda) {
        this.hash_encomenda = hash_encomenda;
    }

    public Map<String, Transportadora> getHash_transportadora() {
        return hash_transportadora;
    }

    public void setHash_transportadora(HashMap<String, Transportadora> hash_transportadora) {
        this.hash_transportadora = hash_transportadora;
    }

    public Scanner getScanner() {
        return scanner;
    }

    public void setScanner(Scanner scanner) {
        this.scanner = scanner;
    }


    public Map<String, Artigo> getArtigo() {
        return hash_artigo;
    }

    public void setArtigo(Map<String, Artigo> artigo) {
        this.hash_artigo = artigo;
    }

    public float getLucroVintage() {
        return lucroVintage;
    }

    public int getDiasFrente() {
        return diasFrente;
    }

    public void setLucroVintage(float lucroVintage) {
        this.lucroVintage = lucroVintage;
    }

    public Encomenda getCarrinho() {
        return carrinho;
    }

    public void setCarrinho(Encomenda carrinho) {
        this.carrinho = carrinho;
    }

    public int veOpcao(int a, int b) {
        int numero = getNumero();
        while (numero < a || numero > b) {
            View.erro("Entrada inválida. Digite novamente.");
            numero = getNumero();
        }
        return numero;
    }

    public int getNumero() {
        int numero;
        try {
            numero = this.scanner.nextInt();
            this.scanner.nextLine();
        } catch (InputMismatchException e) {
            this.scanner.nextLine();
            return -1;
        }
        return numero;
    }

    public String getStr() {
        try {
            return this.scanner.nextLine();
        } catch (NoSuchElementException e) {
            return null;
        }
    }


    public void logIn() {
        Utilizador utilizador = null;
        String email;
        String palavra_passe;
        do {
            do {
                View.imprime("Coloca o seu email de utilizador: ");
                email = scanner.nextLine();
                if (user.containsKey(email)) {
                    utilizador = user.get(email);
                } else {
                    View.erro("Email de utilizador não encontrado.");
                }
            } while (utilizador == null); //(!user.containsKey(email));

            View.imprime("Digite a sua palavra passe: ");
            palavra_passe = scanner.nextLine();
            if (utilizador.getPalavra_passe().equals(palavra_passe)) {
                View.sucesso("Palavra-passe correta. Acesso concedido.");
            } else {
                View.erro("Palavra-passe incorreta. Acesso negado.");
            }
        } while (!utilizador.getPalavra_passe().equals(palavra_passe));
        //atualizar carrinho caso ele deu logout
        user_atual = email;
        Utilizador utilizador1 = user.get(user_atual);
        int size = utilizador1.getLista_encomenda().size();
        if (size > 0) {
            String idCarrinho = utilizador1.getLista_encomenda().get(size - 1);
            if (hash_encomenda.get(idCarrinho).getEstado() == EstadoEncomenda.PENDENTE) {
                carrinho = hash_encomenda.get(idCarrinho);
                List<String> artigos = carrinho.getLista_artigos();
                for (int i = 0; i < artigos.size(); i++) {
                    String id = artigos.get(i);
                    Artigo artigo = hash_artigo.get(id);
                    if (Objects.equals(artigo.getVendido(), "S")) {
                        float precoArtigo = artigo.getPreco_base();
                        carrinho.subPreco(precoArtigo);

                        carrinho.removeArtigo(i);

                        hash_encomenda.put(carrinho.getID_E(), carrinho);

                        View.imprime("Artigo " + id + " removido, pois foi comprado por outro utilizador");
                    }
                }
            }
        }
    }

    public void criarConta() {
        View.imprime("Digite o seu nome:");
        String nome = this.scanner.nextLine();
        String email;
        do {
            View.imprime("Digite o seu email:");
            email = this.scanner.nextLine();
            if (user.containsKey(email))
                View.erro("Este email pertence a outra conta, por favor digite outro");
        } while (user.containsKey(email));

        View.imprime("Digite a palavra-passe que deseja utilizar:");
        String palavra_passe = this.scanner.nextLine();
        View.imprime("Digite a sua morada:");
        String morada = this.scanner.nextLine();
        View.imprime("Digite o seu número fiscal:");
        String num_fiscal = this.scanner.nextLine();

        Utilizador utilizador = new Utilizador(email, nome, morada, num_fiscal, palavra_passe);
        user.put(utilizador.getEmail(), utilizador);
        View.sucesso("A sua conta foi criada com sucesso!");
    }

    public void criarTransportadora() {
        View.header("Criar Transportadora", user.get(user_atual).getSaldo());
        String nome;
        do {
            View.imprime("Digite o nome da sua transportadora:");
            nome = getStr();
            if (hash_transportadora.containsKey(nome))
                View.erro("Nome de transportadora já existente, por favor digite outro");
        } while (hash_transportadora.containsKey(nome) || nome == null);

        float margem_de_lucro;
        do {
            View.imprime("Digite a margem de lucro da sua transportadora:");
            margem_de_lucro = getFloat();
        } while (margem_de_lucro == -1);

        String criador = user_atual;

        View.imprime("Digite 1 se desja que a sua Transportadora seja premium e 0 se deseja que ela seja normal.");
        int a = veOpcao(0, 1);
        if (a == 0) {
            Transportadora novaTransportadora = new Transportadora(nome, margem_de_lucro, criador);
            String ID_T = novaTransportadora.getID_T();
            hash_transportadora.put(ID_T, novaTransportadora);
            View.sucesso("A sua transportadora foi criada com sucesso!");

        } else {
            PremiumTransportadora novaTransportadora = new PremiumTransportadora(nome, margem_de_lucro, criador);
            String ID_T = novaTransportadora.getID_T();
            hash_transportadora.put(ID_T, novaTransportadora);
            View.sucesso("A sua transportadora foi criada com sucesso!");
        }
    }

    public void criarArtigo() {
        View.header("Criar Artigos", user.get(user_atual).getSaldo());
        String nome;
        do {
            View.imprime("Digite o nome do seu artigo:");
            nome = getStr();
        } while (nome == null);

        String usado;
        int usadonum;
        View.imprime("Digite 0 se o seu artigo já foi usado e 1 se nunca foi usado:");
        usadonum = veOpcao(0,1);
        if (usadonum==0) usado ="S";
        else usado ="N";

        String descricao;
        do {
            View.imprime("Digite uma descrição para o seu artigo:");
            descricao = getStr();
        } while (descricao == null);

        String marca;
        do {
            View.imprime("Digite a marca do seu artigo:");
            marca = getStr();
        } while (marca == null);

        AvaliacaoEstado avaliacao_estado;
        View.imprime("Para a avaliação de estado do seu artigo digite 1 se for PESSIMO, 2 se for MAU, 3 se for MEDIO, 4 se for BOM ou 5 se for EXCELENTE");
        int num = veOpcao(1, 5);
        if (num == 1) avaliacao_estado = AvaliacaoEstado.PESSIMO;
        else if (num == 2) avaliacao_estado = AvaliacaoEstado.MAU;
        else if (num == 3) avaliacao_estado = AvaliacaoEstado.MEDIO;
        else if (num == 4) avaliacao_estado = AvaliacaoEstado.BOM;
        else avaliacao_estado = AvaliacaoEstado.EXCELENTE;

        int num_donos;
        do {
            View.imprime("Digite o número de donos que ja teve o seu artigo:");
            num_donos = getNumero();
        } while (num_donos == -1);


        String vendido = "N";
        String email = user_atual;

        View.imprime("Digite o número do seu artigo: ´1- T-shirt´, ´2- Mala´ou ´3- Sapatilha´");
        int tipo = veOpcao(1,3);

        switch (tipo) {
            case 1 -> {
                String tamanho;
                do {
                    View.imprime("Digite o tamanho da sua t-shirt (XS,S,M,L,XL,XXL):");
                    tamanho = getStr();
                } while (!Objects.equals(tamanho, "XS") && !Objects.equals(tamanho, "S") && !Objects.equals(tamanho, "M") && !Objects.equals(tamanho, "L") && !Objects.equals(tamanho, "XL") && !Objects.equals(tamanho, "XXL"));

                Padrao padrao;
                View.imprime("Para o padrao da sua t-shirt digite 1 se for LISO, 2 se for as RISCAS e 3 se for PALMEIRAS");
                int numero = veOpcao(1, 3);
                if (numero == 1) padrao = Padrao.LISO;
                else if (numero == 2) padrao = Padrao.RISCAS;
                else padrao = Padrao.PALMEIRAS;

                //escolha da transportadora

                int paginaAtual = 1;
                int size = hash_transportadora.size();
                int paginaLimite = (size / 10) + 1;

                List<Transportadora> listaTransportadoras = hash_transportadora.values().stream().filter(t->!(t instanceof PremiumTransportadora)).toList();
                String ID_T;
                while (true) {
                    View.imprimeObj(new ArrayList<>(listaTransportadoras), paginaAtual);
                    View.paginador(List.of("3 --- Selecionar número da transportadora"));
                    int numero2 = veOpcao(0, 3);
                    if (numero2 == 1) {
                        if (paginaAtual < paginaLimite) paginaAtual++;
                    } else if (numero2 == 2) {
                        if (paginaAtual > 1) paginaAtual--;
                    } else if (numero2 == 3){
                        View.imprime("Digite o número da transportadora que escolheu para o seu artigo:");
                        int min = Math.min(size-1, paginaAtual * 10);
                        int numID = veOpcao(10 * (paginaAtual - 1), min);
                        Transportadora elemento = listaTransportadoras.get(numID);
                        ID_T = elemento.getID_T();
                        break;
                    } else return;
                }

                float preco;
                do {
                    View.imprime("Digite o preço a que deseja vender o seu artigo:");
                    preco = getFloat();
                } while (preco == -1);

                float correcao = 0;
                Tshirt novoArtigo = new Tshirt(tamanho, padrao, nome, usado, descricao, marca, preco, correcao, avaliacao_estado, num_donos, vendido, ID_T, email);
                novoArtigo.CalculaCorrecao();
                View.imprime("De acordo com as políticas da Vintage o seu artigo terá o preço de " + novoArtigo.getPreco_base() + "€");
                Utilizador utilizador = user.get(user_atual);
                utilizador.getLista_artigos().add(novoArtigo.getCodigo_alfanumerico());
                hash_artigo.put(novoArtigo.getCodigo_alfanumerico(), novoArtigo);
                View.sucesso("O seu artigo foi criado com sucesso!");
            }
            case 2 -> {
                float largura;
                do {
                    View.imprime("Digite a largura da sua mala: ");
                    largura = getFloat();
                } while (largura == -1);

                float altura;
                do {
                    View.imprime("Digite a altura da sua mala: ");
                    altura = getFloat();
                } while (altura == -1);

                float comprimento;
                do {
                    View.imprime("Digite o comprimento da sua mala: ");
                    comprimento = getFloat();
                } while (comprimento == -1);
                Dimensao dimensao = new Dimensao(largura, altura, comprimento);

                String material;
                do {
                    View.imprime("Digite o material da sua mala: ");
                    material = getStr();
                } while (material == null);

                View.imprime("Digite o ano de coleção da sua mala: ");
                int ano_colecao = veOpcao(1900, LocalDate.now().getYear());

                MalaTipo malatipo;
                View.imprime("Para o tipo da sua mala digite 1 se for uma CARTEIRA, 2 se for um ESTOJO, 3 se for uma BOLSA, 4 se for uma MOCHILA ou 5 se for uma mala de VIAGEM");
                int x = veOpcao(1, 5);
                if (x == 1) malatipo = MalaTipo.CARTEIRA;
                else if (x == 2) malatipo = MalaTipo.ESTOJO;
                else if (x == 3) malatipo = MalaTipo.BOLSA;
                else if (x == 4) malatipo = MalaTipo.MOCHILA;
                else malatipo = MalaTipo.VIAGEM;

                float preco;
                do {
                    View.imprime("Digite o preço a que deseja vender o seu artigo:");
                    preco = getFloat();
                } while (preco == -1);

                float correcao = 0;

                View.imprime("Digite 1 se desja que a sua Mala seja premium e 0 se deseja que ela seja normal.");
                int a = veOpcao(0, 1);
                if (a == 0) {
                    //escolha da transportadora

                    int paginaAtual = 1;
                    int size = hash_transportadora.size();
                    int paginaLimite = (size / 10) + 1;

                    List<Transportadora> listaTransportadoras = hash_transportadora.values().stream().filter(t->!(t instanceof PremiumTransportadora)).toList();

                    String ID_T;
                    while (true) {
                        View.imprimeObj(new ArrayList<>(listaTransportadoras), paginaAtual);
                        View.paginador(List.of("3 --- Selecionar número da transportadora"));
                        int numero = veOpcao(0, 3);
                        if (numero == 1) {
                            if (paginaAtual < paginaLimite) paginaAtual++;
                        } else if (numero == 2) {
                            if (paginaAtual > 1) paginaAtual--;
                        } else if (numero == 3) {
                            View.imprime("Digite o número da transportadora que escolheu para o seu artigo:");
                            int min = Math.min(size-1, paginaAtual * 10);
                            int numID = veOpcao(10 * (paginaAtual - 1), min);
                            Transportadora elemento = listaTransportadoras.get(numID);
                            ID_T = elemento.getID_T();
                            break;
                        } else return;
                    }
                    Mala novoArtigo = new Mala(dimensao, material, ano_colecao, malatipo, nome, usado, descricao, marca, preco, correcao, avaliacao_estado, num_donos, vendido, ID_T, email);
                    novoArtigo.CalculaCorrecao();
                    View.imprime("De acordo com as políticas da Vintage o seu artigo terá o preço de " + novoArtigo.getPreco_base() + "€");
                    Utilizador utilizador = user.get(user_atual);
                    utilizador.getLista_artigos().add(novoArtigo.getCodigo_alfanumerico());
                    hash_artigo.put(novoArtigo.getCodigo_alfanumerico(), novoArtigo);
                    View.sucesso("O seu artigo foi criado com sucesso!");
                } else {
                    //escolha da transportadora

                    int paginaAtual = 1;
                    int size = hash_transportadora.size();
                    int paginaLimite = (size / 10) + 1;

                    List<Transportadora> listaTransportadoras = hash_transportadora.values().stream().filter(t->t instanceof PremiumTransportadora).toList();
                    String ID_T;
                    while (true) {
                        View.imprimeObj(new ArrayList<>(listaTransportadoras), paginaAtual);
                        View.paginador(List.of("3 --- Selecionar número da transportadora"));
                        int numero = veOpcao(0, 3);
                        if (numero == 1) {
                            if (paginaAtual < paginaLimite) paginaAtual++;
                        } else if (numero == 2) {
                            if (paginaAtual > 1) paginaAtual--;
                        } else if (numero == 3) {
                            View.imprime("Digite o número da transportadora que escolheu para o seu artigo:");
                            int min = Math.min(size-1, paginaAtual * 10);
                            int numID = veOpcao(10 * (paginaAtual - 1), min);
                            Transportadora elemento = listaTransportadoras.get(numID);
                            ID_T = elemento.getID_T();
                            break;
                        }else return;
                    }
                    PremiumMala novoArtigo = new PremiumMala(dimensao, material, ano_colecao, malatipo, nome, usado, descricao, marca, preco, correcao, avaliacao_estado, num_donos, vendido, ID_T, email);
                    novoArtigo.CalculaCorrecao();
                    View.imprime("De acordo com as políticas da Vintage o seu artigo terá o preço de " + novoArtigo.getPreco_base() + "€");
                    Utilizador utilizador = user.get(user_atual);
                    utilizador.getLista_artigos().add(novoArtigo.getCodigo_alfanumerico());
                    hash_artigo.put(novoArtigo.getCodigo_alfanumerico(), novoArtigo);
                    View.sucesso("O seu artigo foi criado com sucesso!");
                }
            }
            case 3 -> {
                int tamanho;
                do {
                    View.imprime("Digite o tamanho da sua sapatilha: ");
                    tamanho = getNumero();
                } while (tamanho == -1);

                String indicacao;
                int indic;
                View.imprime("Digite 1 se a sua sapatilha possui atacadores e 0 se não possui:");
                indic = veOpcao(0,1);
                if (indic == 1) indicacao="S";
                else indicacao="N"; //if (indic == 0)

                String cor;
                do {
                    View.imprime("Digite a cor da sua sapatilha: ");
                    cor = getStr();
                } while (cor == null);

                View.imprime("Digite o ano de lançamento da sua sapatilha: ");
                int lancamento = veOpcao(1900, LocalDate.now().getYear());

                float preco;
                do {
                    View.imprime("Digite o preço a que deseja vender o seu artigo:");
                    preco = getFloat();
                } while (preco == -1);

                float correcao = 0;

                View.imprime("Digite 1 se desja que a sua Sapatilha seja premium e 0 se deseja que ela seja normal.");
                int num2 = veOpcao(0, 1);
                if (num2 == 0) {

                    //escolha da transportadora

                    int paginaAtual = 1;
                    int size = hash_transportadora.size();
                    int paginaLimite = (size / 10) + 1;

                    List<Transportadora> listaTransportadoras = hash_transportadora.values().stream().filter(t->!(t instanceof PremiumTransportadora)).toList();

                    String ID_T;
                    while (true) {
                        View.imprimeObj(new ArrayList<>(listaTransportadoras), paginaAtual);
                        View.paginador(List.of("3 --- Selecionar número da transportadora"));
                        int numero = veOpcao(0, 3);
                        if (numero == 1) {
                            if (paginaAtual < paginaLimite) paginaAtual++;
                        } else if (numero == 2) {
                            if (paginaAtual > 1) paginaAtual--;
                        } else if (numero == 3){
                            View.imprime("Digite o número da transportadora que escolheu para o seu artigo:");
                            int min = Math.min(size-1, paginaAtual * 10);
                            int numID = veOpcao(10 * (paginaAtual - 1), min);
                            Transportadora elemento = listaTransportadoras.get(numID);
                            ID_T = elemento.getID_T();
                            break;
                        } else return;
                    }

                    Sapatilha novoArtigo = new Sapatilha(tamanho, indicacao, cor, lancamento, nome, usado, descricao, marca, preco, correcao, avaliacao_estado, num_donos, vendido, ID_T, email);
                    novoArtigo.CalculaCorrecao();
                    View.imprime("De acordo com as políticas da Vintage o seu artigo terá o preço de " + novoArtigo.getPreco_base() + "€");
                    Utilizador utilizador = user.get(user_atual);
                    utilizador.getLista_artigos().add(novoArtigo.getCodigo_alfanumerico());
                    hash_artigo.put(novoArtigo.getCodigo_alfanumerico(), novoArtigo);
                    View.sucesso("O seu artigo foi criado com sucesso!");
                } else {

                    //escolha da transportadora

                    int paginaAtual = 1;
                    int size = hash_transportadora.size();
                    int paginaLimite = (size / 10) + 1;

                    List<Transportadora> listaTransportadoras = hash_transportadora.values().stream().filter(t->t instanceof PremiumTransportadora).toList();
                    String ID_T;
                    while (true) {
                        View.imprimeObj(new ArrayList<>(listaTransportadoras), paginaAtual);
                        View.paginador(List.of("3 --- Selecionar número da transportadora"));
                        int numero = veOpcao(0, 3);
                        if (numero == 1) {
                            if (paginaAtual < paginaLimite) paginaAtual++;
                        } else if (numero == 2) {
                            if (paginaAtual > 1) paginaAtual--;
                        } else if (numero == 3){
                            View.imprime("Digite o número da transportadora que escolheu para o seu artigo:");
                            int min = Math.min(size-1, paginaAtual * 10);
                            int numID = veOpcao(10 * (paginaAtual - 1), min);
                            Transportadora elemento = listaTransportadoras.get(numID);
                            ID_T = elemento.getID_T();
                            break;
                        }else return;
                    }

                    View.imprime("Digite o autor da sua sapatilha premium: ");
                    String autor = getStr(); //this.scanner.nextLine();
                    PremiumSapatilha novoArtigo = new PremiumSapatilha(autor, tamanho, indicacao, cor, lancamento, nome, usado, descricao, marca, preco, correcao, avaliacao_estado, num_donos, vendido, ID_T, email);
                    novoArtigo.CalculaCorrecao();
                    View.imprime("De acordo com as políticas da Vintage o seu artigo terá o preço de " + novoArtigo.getPreco_base() + "€");
                    Utilizador utilizador = user.get(user_atual);
                    utilizador.getLista_artigos().add(novoArtigo.getCodigo_alfanumerico());
                    hash_artigo.put(novoArtigo.getCodigo_alfanumerico(), novoArtigo);
                    View.sucesso("O seu artigo foi criado com sucesso!");
                }
            }
        }
    }

    public void sair() {
        ficheiroObjeto();
    }

    public void corre() {
        lerFicheiroObjeto();
        int numero = 1;
        View.imprime("Bem vindo! Se deseja criar conta digite 1, se deseja fazer logIn digita 2 e digita 0 se deseja sair.");
        numero = veOpcao(0, 2);
        if (numero == 1) {
            criarConta();
            logIn();
        } else if (numero == 2) logIn();
        else if (numero == 0) sair();


        ficheiroObjeto();

        Utilizador utilizador = user.get(user_atual);

        tratarEncomendas();
        while (numero != 0) {
            View.menuPrincipal(utilizador.getSaldo());
            numero = veOpcao(0, 11);
            if (numero == 1) consultaArtigos();
            else if (numero == 2) meusArtigos();
            else if (numero == 3) minhasVendas();
            else if (numero == 4) editarPerfil();
            else if (numero == 5) minhasEncomendas();
            else if (numero == 6) criarArtigo();
            else if (numero == 7) criarTransportadora();
            else if (numero == 8) metricas();
            else if (numero ==9) avancarTempo();
            else if (numero == 10) concluirEncomenda();
            else if (numero == 11) editarTransportadora();
            ficheiroObjeto();
        }
        logOut();
    }


    public void consultaArtigos() {
        View.header("Consulta de Artigos", user.get(user_atual).getSaldo());
        int paginaAtual = 1;
        int size = hash_artigo.size();
        int paginaLimite = (size / 10) + 1;

        List<Artigo> listaArtigos = hash_artigo.values().stream().filter(artigo -> Objects.equals(artigo.getVendido() , "N") && !Objects.equals(artigo.getEmail_vendedor(), user_atual)).toList();

        if (listaArtigos.size() ==0){
            View.erro("A Vintage não tem artigos!");
            return;
        }

        while (true) {
            View.imprimeObj(new ArrayList<>(listaArtigos), paginaAtual);
            View.paginador(List.of("3 --- Comprar"));
            int numero = veOpcao(0, 3);
            switch (numero) {
                case 1 -> {
                    if (paginaAtual < paginaLimite) paginaAtual++;
                }
                case 2 -> {
                    if (paginaAtual > 1) paginaAtual--;
                }
                case 3 -> {
                    View.imprime("Digite o numero do artigo que pretende comprar:");
                    int min = Math.min(size, paginaAtual * 10);
                    int num = veOpcao(10 * (paginaAtual - 1), min);
                    Artigo elemento = listaArtigos.get(num);
                    if (!carrinho.getLista_artigos().contains(elemento.getCodigo_alfanumerico())) {
                        carrinho.addArtigo(elemento.getCodigo_alfanumerico());

                        float precoArtigo = elemento.getPreco_base();
                        carrinho.somaPreco(precoArtigo);

                        View.sucesso("Artigo adicionado a encomenda com sucesso!");
                        carrinho.calculaDimensao();
                        carrinho.setEstado(EstadoEncomenda.PENDENTE);
                        hash_encomenda.put(carrinho.getID_E(), carrinho);
                    } else {
                        View.erro("Produto já está no carrinho.");
                    }

                    View.imprime("1 --- Ver mais artigos");
                    View.imprime("2 --- Concluir a encomenda");
                    View.imprime("3 --- Editar a encomenda");
                    numero = veOpcao(1, 3);
                    if (numero == 2) {
                        concluirEncomenda();
                        return;
                    } else if (numero == 3) {
                        editarCarrinho();
                        return;
                    }
                }
                case 0 -> {
                    return;
                }
            }
        }
    }

    public void editarTransportadora() {
        View.header("Editar transportadora", user.get(user_atual).getSaldo());

        int paginaAtual = 1;
        int size = hash_transportadora.size();
        int paginaLimite = (size / 10) + 1;
        List<Transportadora> listaTransportadoras = hash_transportadora.values().stream().toList();
        if (listaTransportadoras.size() ==0){
            View.erro("A Vintage não tem artigos!");
            return;
        }

        while (true) {
            View.imprimeObj(new ArrayList<>(listaTransportadoras), paginaAtual);
            View.paginador(List.of("3 --- Escolher uma transportadora"));
            int numero = veOpcao(0, 3);
            switch (numero) {
                case 1 -> {
                    if (paginaAtual < paginaLimite) paginaAtual++;
                }
                case 2 -> {
                    if (paginaAtual > 1) paginaAtual--;
                }
                case 3 -> {
                    View.imprime("Digite o número da Transportadora que pretende modificar:");
                    int min = Math.min(size, paginaAtual * 10);
                    int num = veOpcao(10 * (paginaAtual - 1), min);
                    Transportadora transportadora = listaTransportadoras.get(num);
                    if (Objects.equals(transportadora.getCriador(), user_atual)) {
                        View.menuEditarTransp(user.get(user_atual).getSaldo());
                        int numb = veOpcao(0, 4);
                        switch (numb) {
                            case 1 -> {
                                View.imprime("Digita nova margem de lucro:");
                                float novaMargem = getFloat();
                                transportadora.setMargem_lucro(novaMargem);
                            }
                            case 2 -> {
                                View.imprime("Digita o novo valor pequeno:");
                                float novoValorP = getFloat();
                                transportadora.setValor_pequeno(novoValorP);
                            }
                            case 3 -> {
                                View.imprime("Digita o novo valor médio:");
                                float novoValorM = getFloat();
                                transportadora.setVelor_medio(novoValorM);
                            }
                            case 4 -> {
                                View.imprime("Digita o novo valor grande:");
                                float novoValorG = getFloat();
                                transportadora.setValor_grande(novoValorG);
                            }
                            case 0 -> {
                                return;
                            }
                        }
                        hash_transportadora.put(transportadora.getID_T(), transportadora);
                        View.sucesso(("Alteração realizada com sucesso!"));
                    } else {
                        View.erro("Acesso Negado");
                    }
                } case 0 -> {return;}
            }
        }
    }


    public void concluirEncomenda() {
        View.header("Concluir Encomenda ", user.get(user_atual).getSaldo());
        if (carrinho.getLista_artigos().isEmpty()) {
            View.erro("Não possui nenhum artigo no seu carrinho!");
            return;
        }
        int size = carrinho.getLista_artigos().size();
        List<Artigo> artigosCarrinho = carrinho.getLista_artigos().stream().map(a -> hash_artigo.get(a)).toList();

        int paginaAtual = 1;
        int paginaLimite = (size / 10) + 1;

        while (true) {
            View.imprimeObj(new ArrayList<>(artigosCarrinho), paginaAtual);
            View.paginador(List.of("3 --- Confirmar encomenda"));
            int numb = veOpcao(0, 3);
            switch (numb) {
                case 1 -> {
                    if (paginaAtual < paginaLimite) paginaAtual++;
                }
                case 2 -> {
                    if (paginaAtual > 1) paginaAtual--;
                }
                case 3 -> {
                    View.imprime("Se tem a certeza que deseja concluir a sua encomenda digite 1, caso contrário digite 0.");
                    int num = veOpcao(0, 1);
                    if (num == 0) return;
                    float precoArtigos = carrinho.getPreco_artigos();

                    List<Artigo> listaArtigos = new ArrayList<>();
                    Map<String, List<Artigo>> sublistas = new HashMap<>();
                    for (String codigo_alfanumerico : carrinho.getLista_artigos()) {
                        Artigo artigo = hash_artigo.get(codigo_alfanumerico);
                        listaArtigos.add(artigo);
                    }
                    // sublistas de artigos com base nas transportadoras comuns
                    for (Artigo artigo : listaArtigos) {
                        if (sublistas.containsKey(artigo.getID_T())) {
                            List<Artigo> aux = sublistas.get(artigo.getID_T());
                            aux.add(artigo);
                            sublistas.put(artigo.getID_T(), aux);
                        } else {
                            List<Artigo> novaLista = new ArrayList<>();
                            novaLista.add(artigo);
                            sublistas.put(artigo.getID_T(), novaLista);
                        }
                    }
                    float portes = 0;
                    Map<String, Float> valorFaturadoT = new HashMap<>();
                    for (Map.Entry<String, List<Artigo>> entrada : sublistas.entrySet()) {
                        //contar os elementos das sublistas
                        int numArtigos = entrada.getValue().size();
                        //ir somando os portes de cada pacote de transportadora
                        Transportadora transportadora = hash_transportadora.get(entrada.getKey());
                        portes += transportadora.calculaPortes(entrada.getValue(), numArtigos, imposto);
                        if (valorFaturadoT.containsKey(entrada.getKey())) {
                            float valor = valorFaturadoT.get(entrada.getKey());
                            valorFaturadoT.put(entrada.getKey(), valor + portes);
                        } else {
                            valorFaturadoT.put(entrada.getKey(), portes);
                        }
                    }

                    float taxaVintage = 0;
                    for (String codigo : carrinho.getLista_artigos()) {
                        Artigo artigo = hash_artigo.get(codigo);
                        if (Objects.equals(artigo.getUsado(), "N"))
                            taxaVintage += 0.5;
                        else taxaVintage += 0.25;
                    }

                    float precoFinal = precoArtigos + portes + taxaVintage;
                    carrinho.setPreco_final(precoFinal);

                    View.imprime("O preço final da sua encomenda é de:" + precoFinal + "€");

                    //atualizar saldo comprador
                    Utilizador comprador = user.get(user_atual);

                    if (comprador.compra(precoFinal, carrinho.getID_E())) {
                        user.put(user_atual, comprador);
                        lucroVintage += taxaVintage;
                        for (Map.Entry<String, Float> entry : valorFaturadoT.entrySet()) {
                            String key = entry.getKey();
                            Transportadora transportadora = hash_transportadora.get(key);
                            Float valor = transportadora.getValorFaturado();
                            transportadora.setValorFaturado(valor + entry.getValue());
                            hash_transportadora.put(key, transportadora);
                        }

                        for (String codigo_alfanumerico : carrinho.getLista_artigos()) {
                            //atualizar saldo vendedor
                            Artigo artigo = hash_artigo.get(codigo_alfanumerico);
                            String email_vendedor = artigo.getEmail_vendedor();
                            Utilizador vendedor = user.get(email_vendedor);
                            float preco = artigo.getPreco_base();
                            vendedor.setSaldo(vendedor.getSaldo() + preco);
                            user.put(email_vendedor, vendedor);
                            //str vendido do artigo atualizado
                            artigo.setVendido("S");
                            hash_artigo.put(codigo_alfanumerico, artigo);
                        }
                        // estado da encomenda atualizado
                        carrinho.setEstado(EstadoEncomenda.FINALIZADA);
                        carrinho.setDataFinalizacao(LocalDate.now());
                        hash_encomenda.put(carrinho.getID_E(), carrinho);

                        View.sucesso("Pagamento efetuado com sucesso!");
                        View.imprime("Deseja receber fatura? 0/1");
                        int numero = veOpcao(0, 1);
                        if (numero == 1) faturas();

                        //carrinho vai para encomenda e esvazia
                        carrinho = new Encomenda();

                    } else {
                        View.erro("Não tem saldo suficiente para comprar a sua encomenda");
                    }
                    return;

                }
                case 0 -> {return;}
            }
        }
    }

    public void editarCarrinho() {
        int size = carrinho.getLista_artigos().size();
        List<Artigo> artigosCarrinho = new ArrayList<>();
        if (carrinho.getLista_artigos().isEmpty()) {
            View.erro("Não possui nenhum artigo no seu carrinho!");
            return;
        }
        int paginaAtual = 1;
        int paginaLimite = (size / 10) + 1;
        for (int i = 0; i < size; i++) {
            String codigo = carrinho.getLista_artigos().get(i);
            Artigo artigo = hash_artigo.get(codigo);
            artigosCarrinho.add(artigo);
        }
        while (true) {
            View.imprimeObj(new ArrayList<>(artigosCarrinho), paginaAtual);
            View.paginador(List.of("3 --- Remover um artigo"));
            int numero = veOpcao(0, 3);
            switch (numero) {
                case 1 -> {
                    if (paginaAtual < paginaLimite) paginaAtual++;
                }
                case 2 -> {
                    if (paginaAtual > 1) paginaAtual--;
                }
                case 3 -> {
                    View.imprime("Digite o número do artigo que deseja remover da sua encomenda.");
                    int min = Math.min(size, paginaAtual * 10);
                    int num = veOpcao(10 * (paginaAtual - 1), min);

                    Artigo artigo = artigosCarrinho.get(num);
                    float precoArtigo = artigo.getPreco_base();
                    carrinho.subPreco(precoArtigo);

                    carrinho.removeArtigo(num);

                    hash_encomenda.put(carrinho.getID_E(), carrinho);

                    View.sucesso("Artigo " + num + " removido com sucesso!");
                    artigosCarrinho.remove(num);
                }
                case 0 -> {
                    return;
                }
            }
        }
    }

    public void meusArtigos() {
        View.header("Meus Artigos", user.get(user_atual).getSaldo());
        Utilizador utilizador = user.get(user_atual);
        if (utilizador.getLista_artigos().isEmpty()) {
            View.erro("Não possui nenhum artigo!");
            return;
        }
        int paginaAtual = 1;
        int size = utilizador.getLista_artigos().size();
        int paginaLimite = (size / 10) + 1;

        List<Artigo> meusArtigos = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            String codigo = utilizador.getLista_artigos().get(i);
            Artigo artigo = hash_artigo.get(codigo);
            meusArtigos.add(artigo);
        }

        while (true) {
            View.imprimeObj(new ArrayList<>(meusArtigos), paginaAtual);
            View.paginador(new ArrayList<>());
            int numero = veOpcao(0, 2);
            switch (numero) {
                case 1 -> {
                    if (paginaAtual < paginaLimite) paginaAtual++;
                }
                case 2 -> {
                    if (paginaAtual > 1) paginaAtual--;
                }
                case 0 -> {
                    return;
                }
            }
        }
    }

    private void minhasVendas() {
        View.header("Minhas Vendas", user.get(user_atual).getSaldo());
        Utilizador utilizador = user.get(user_atual);

        int paginaAtual = 1;
        int size = utilizador.getLista_artigos().size();
        int paginaLimite = (size / 10) + 1;

        List<Artigo> minhasVendas = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            String codigo = utilizador.getLista_artigos().get(i);
            Artigo artigo = hash_artigo.get(codigo);
            if (Objects.equals(artigo.getVendido(), "S"))
                minhasVendas.add(artigo);
        }

        if (minhasVendas.isEmpty()) {
            View.erro("Não possui nenhuma venda!");
            return;
        }

        while (true) {
            View.imprimeObj(new ArrayList<>(minhasVendas), paginaAtual);
            View.paginador(new ArrayList<>());
            int numero = veOpcao(0, 2);
            switch (numero) {
                case 1 -> {
                    if (paginaAtual < paginaLimite) paginaAtual++;
                }
                case 2 -> {
                    if (paginaAtual > 1) paginaAtual--;
                }
                case 0 -> {
                    return;
                }
            }
        }
    }

    public void editarPerfil() {
        Utilizador utilizador = user.get(user_atual);
        View.menuEditarPerfil(utilizador.getSaldo());
        int numero = veOpcao(0, 3);
        switch (numero) {
            case 1 -> {
                String antigaPalavraPasse;
                do {
                    View.imprime("Digite a sua palavra passe atual");
                    antigaPalavraPasse = scanner.nextLine();
                    if ((utilizador.getPalavra_passe().equals(antigaPalavraPasse))) {
                        View.imprime("Digite a sua nova palavra passe");
                        String novaPalavraPasse = scanner.nextLine();
                        utilizador.setPalavra_passe(novaPalavraPasse);
                        user.put(user_atual, utilizador);
                        View.sucesso("Palavra passe alterada com sucesso!");
                        return;
                    } else {
                        View.erro("Palavra passe incorreta.");
                    }
                } while (!(utilizador.getPalavra_passe().equals(antigaPalavraPasse)));
            }
            case 2 -> {
                View.imprime("Digite a sua nova morada");
                String novaMorada = scanner.nextLine();
                utilizador.setMorada(novaMorada);
                user.put(user_atual, utilizador);
                View.sucesso("Morada passe alterada com sucesso!");
            }
            case 3 -> {
                View.imprime("O seu saldo é de " + utilizador.getSaldo() + "€");
                editarSaldo();
            }
            case 0 -> {
            }
        }
    }


    public void minhasEncomendas() {
        View.header("Minhas Encomendas", user.get(user_atual).getSaldo());
        Utilizador utilizador = user.get(user_atual);
        if (utilizador.getLista_encomenda().isEmpty()) {
            View.erro("Não possui nenhuma encomenda!");
            return;
        }
        int paginaAtual = 1;
        int size = utilizador.getLista_encomenda().size();
        int paginaLimite = (size / 10) + 1;

        List<Encomenda> minhasEncomendas = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            String ID_E = utilizador.getLista_encomenda().get(i);
            Encomenda encomenda = hash_encomenda.get(ID_E);
            minhasEncomendas.add(encomenda);
        }

        while (true) {
            View.imprimeObj(new ArrayList<>(minhasEncomendas), paginaAtual);
            View.paginador(Arrays.asList("3 --- Devolver uma encomenda", "4 --- Ver detalhes de uma encomenda"));
            int numero = veOpcao(0, 4);
            switch (numero) {
                case 1 -> {
                    if (paginaAtual < paginaLimite) paginaAtual++;
                }
                case 2 -> {
                    if (paginaAtual > 1) paginaAtual--;
                }
                case 3 -> {
                    View.imprime("Digite o número da encomenda que deseja devolver.");
                    int min = Math.min(size, paginaAtual * 10);
                    int num = veOpcao(10 * (paginaAtual - 1), min);

                    Encomenda encomenda = minhasEncomendas.get(num);
                    String ID_E = encomenda.getID_E();
                    if (devolverEncomenda(ID_E)) {
                        View.sucesso("Encomenda devolvida com sucesso!");
                        minhasEncomendas.remove(num);
                        if (minhasEncomendas.size() == 0)
                            return;
                    }
                    else
                        View.erro("É impossível devolver a sua encomenda, pois já passaram 48h ou esta encomenda ainda não foi expedida.");
                }
                case 4 -> {
                    View.imprime("Digite o número da encomenda que deseja visualizar melhor.");
                    int min = Math.min(size, paginaAtual * 10);
                    int num = veOpcao(10 * (paginaAtual - 1), min);
                    Encomenda encomenda = minhasEncomendas.get(num);

                    visualizaEncomenda(encomenda);
                }
                case 0 -> {
                    return;
                }
            }
        }
    }

    private void visualizaEncomenda(Encomenda encomenda) {

        List <String> lista = encomenda.getLista_artigos();
        int paginaAtual = 1;
        int size = encomenda.getLista_artigos().size();
        int paginaLimite = (size / 10) + 1;

        List<Artigo> listaArtigos = lista.stream().map(id -> hash_artigo.get(id)).toList();

        while (true) {
            View.imprime("Artigos da sua encomenda:");
            View.imprimeObj(new ArrayList<>(listaArtigos), paginaAtual);
            View.paginador(new ArrayList<>());
            int numero2 = veOpcao(0, 4);
            switch (numero2) {
                case 1 -> {
                    if (paginaAtual < paginaLimite) paginaAtual++;
                }
                case 2 -> {
                    if (paginaAtual > 1) paginaAtual--;
                }
                case 0 -> {
                    return;
                }
            }
        }
    }

    public void editarSaldo() {
        View.header("Editar Saldo", user.get(user_atual).getSaldo());
        Utilizador utilizador = user.get(user_atual);
        float saldoAntigo = utilizador.getSaldo();
        float saldoAddicionado;
        do {
            View.imprime("Escreve quanto saldo quer carregar");
            saldoAddicionado = getFloat();
            if (saldoAddicionado == -1)
                View.erro("Erro ao colocar saldo.");
        } while (saldoAddicionado == -1);
        float saldoNovo = saldoAntigo + saldoAddicionado;
        utilizador.setSaldo(saldoNovo);
        user.put(user_atual, utilizador);
        View.sucesso("Saldo adicionado com sucesso! O seu saldo é de: " + saldoNovo);
    }

    public float getFloat() {
        float numero;
        try {
            numero = this.scanner.nextFloat();
            this.scanner.nextLine();
        } catch (InputMismatchException e) {
            this.scanner.nextLine();
            return -1;
        }
        return numero;
    }

    public boolean devolverEncomenda(String ID_E) {
        Utilizador comprador = user.get(user_atual);
        Encomenda encomenda = hash_encomenda.get(ID_E);
        if (encomenda.getEstado() == EstadoEncomenda.EXPEDIDA) {
            LocalDate dataExpedicao = encomenda.getDataExpedicao();
            LocalDate data = LocalDate.now().plusDays(diasFrente);
            Duration duracao = Duration.between(dataExpedicao.atStartOfDay(), data.atStartOfDay());
            long diferencaEmHoras = duracao.toHours();
            if (diferencaEmHoras < 48) {
                View.imprime("Se tem a certeza que quer devolver a sua encomenda digite 1 senão digite 0.");
                int num = veOpcao(0, 1);
                if (num == 1) {
                    //atualizar saldo comprador
                    float preco_devolvido = encomenda.getPreco_artigos();
                    comprador.setSaldo(comprador.getSaldo() + preco_devolvido);
                    user.put(user_atual, comprador);
                    View.imprime("Informamos que apenas será devolvido o valor total dos artigos da sua encomenda.");
                    View.sucesso("O seu saldo foi atualizado com sucesso!");

                    //atualizar os saldos dos vendedores
                    for (String codigo_alfanumerico : encomenda.getLista_artigos()) {
                        Artigo artigo = hash_artigo.get(codigo_alfanumerico);
                        String email_vendedor = artigo.getEmail_vendedor();
                        Utilizador vendedor = user.get(email_vendedor);
                        float precoRetirado = artigo.getPreco_base();
                        vendedor.setSaldo(vendedor.getSaldo() - precoRetirado); // saldo negativo é possivel?
                        user.put(email_vendedor, vendedor);
                        //str vendido do artigo atualizado
                        artigo.setVendido("N");
                        hash_artigo.put(codigo_alfanumerico, artigo);
                    }
                    //remover encomenda
                    comprador.getLista_encomenda().remove(encomenda.getID_E());
                    hash_encomenda.remove(encomenda.getID_E());
                    return true;
                }
            }
        }
        return false;
    }


    public void logOut() {
        user_atual = null;
    }

    public void tratarEncomendas() {
        for (Map.Entry<String, Encomenda> entrada : hash_encomenda.entrySet()) {
            Encomenda encomenda = entrada.getValue();
            if (encomenda.getEstado() == EstadoEncomenda.FINALIZADA) {
                LocalDate dataFinal = encomenda.getDataFinalizacao();
                LocalDate data = LocalDate.now().plusDays(diasFrente);
                Duration duracao = Duration.between(dataFinal.atStartOfDay(), data.atStartOfDay());
                long diferencaEmHoras = duracao.toHours();
                if (diferencaEmHoras >= 24) {
                    encomenda.setEstado(EstadoEncomenda.EXPEDIDA);
                    encomenda.setDataExpedicao(LocalDate.now());
                }
            }
        }
    }

    public void avancarTempo(){
        View.header("Avançar no tempo", user.get(user_atual).getSaldo());
        View.imprime("Digite de quantos dias deseja avançar:");
        this.diasFrente = veOpcao(0,365);
        tratarEncomendas();
    }

    public void faturas(){
        try {
            File myObj = new File("Fatura"+carrinho.getID_E()+".txt");
            if (myObj.createNewFile()) {
                try {
                    FileWriter myWriter = new FileWriter("Fatura"+carrinho.getID_E()+".txt");
                    myWriter.write("Fatura " + carrinho.getID_E() +"\n");
                    for (String id : carrinho.getLista_artigos()){
                        Artigo artigo = hash_artigo.get(id);
                        String nome = artigo.getNome();
                        float preco = artigo.getPreco_base();
                        myWriter.write(nome +": "+ preco +"€\n");
                    }
                    myWriter.write("Total: " + carrinho.getPreco_final() +"€\n");
                    myWriter.close();
                    View.sucesso("Fatura escrita com nome «Fatura"+carrinho.getID_E()+".txt»");
                } catch (IOException e) {
                    View.erro("Erro ao escrever fatura");
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            View.erro("Erro ao criar fatura");
            e.printStackTrace();
        }
    }


    // METRICAS

    public void metricas() {
        LocalDate data1;
        LocalDate data2;
        Utilizador utilizador = user.get(user_atual);
        View.menuMetricas(utilizador.getSaldo());
        int numero = veOpcao(0, 7);
        if (numero == 1) vendedorquemaisFaturou();
        else if (numero == 2) {
            do {
                View.imprime("Digite uma data (dd/MM/yyyy):");
                data1 = getDate();
            } while (data1 == null);
            do {
                View.imprime("Digite outra data (dd/MM/yyyy):");
                data2 = getDate();
            } while (data2 == null);

            if (data1.isAfter(data2)) {
                vendedorquemaisFaturou(data2, data1);
            } else vendedorquemaisFaturou(data1, data2);
        } else if (numero == 3) transportadoraMaisFaturou();
        else if (numero == 4) {
            View.imprime("Digite o email do vendedor que emitiu as encomendas");
            String email = getStr();
            listaEncomendas(email);
        } else if (numero == 5) {
            do {
                View.imprime("Digite uma data (dd/MM/yyyy):");
                data1 = getDate();
            } while (data1 == null);
            do {
                View.imprime("Digite outra data (dd/MM/yyyy):");
                data2 = getDate();
            } while (data2 == null);

            if (data1.isAfter(data2)) {
                maiorVendedor(data2, data1);
            } else maiorVendedor(data1, data2);
        }
        else if (numero == 6) {
            do {
                View.imprime("Digite uma data (dd/MM/yyyy):");
                data1 = getDate();
            } while (data1 == null);
            do {
                View.imprime("Digite outra data (dd/MM/yyyy):");
                data2 = getDate();
            } while (data2 == null);

            if (data1.isAfter(data2)) {
                maiorComprador(data2, data1);
            } else maiorComprador(data1, data2);
        }
        else if (numero == 7) showLucro();
    }

    public LocalDate getDate() {
        try {
            String dataString = scanner.nextLine();
            return LocalDate.parse(dataString, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (Exception e) {
            this.scanner.nextLine();
            return null;
        }
    }
    //1

    public void vendedorquemaisFaturou() {
        Map<String, Float> listapreco = new HashMap<>(); //preco do artigo + vendedor
        for (Map.Entry<String, Utilizador> entry : user.entrySet()) {
            float precoFaturado = 0;
            String chave = entry.getKey();
            Utilizador vendedor = entry.getValue();
            List<String> listaArtigos = vendedor.getLista_artigos();
            for (String codigo : listaArtigos) {
                Artigo artigo = hash_artigo.get(codigo);
                if (Objects.equals(artigo.getVendido(), "S")) {
                    precoFaturado += artigo.getPreco_base();
                }
            }
            listapreco.put(chave, precoFaturado);
        }

        float max = -1;
        String codigo_maisfaturou = null;
        for (Map.Entry<String, Float> entry : listapreco.entrySet()) {
            Float preco = entry.getValue();
            String codigo = entry.getKey();
            if (preco > max) {
                max = preco;
                codigo_maisfaturou = codigo;
            }
        }
        View.imprime("O vendedor que mais faturou é: " + codigo_maisfaturou);
        View.imprime("O vendedor faturou: " + max + "€");
    }


    public void vendedorquemaisFaturou(LocalDate data1, LocalDate data2) {
        if(data1.isAfter(data2)){
            LocalDate data3 = data1;
            data1 = data2;
            data2 = data3;
        }
        Map<String, Float> listapreco = new HashMap<>(); //preco do artigo + vendedor
        LocalDate finalData1 = data1;
        LocalDate finalData2 = data2;
        List<Encomenda> encRealizadas = hash_encomenda.values().stream().
                filter(e -> e.getEstado()!=EstadoEncomenda.PENDENTE && e.getDataFinalizacao().isAfter(finalData1) &&
                        e.getDataFinalizacao().isBefore(finalData2))
                .toList();
        for (Encomenda encomenda : encRealizadas) {
            for (String codigo : encomenda.getLista_artigos()) {
                Artigo artigo = hash_artigo.get(codigo);
                if (listapreco.containsKey(artigo.getEmail_vendedor())) {
                    float precoFaturado = listapreco.get(artigo.getEmail_vendedor());
                    precoFaturado += artigo.getPreco_base();
                    listapreco.put(artigo.getEmail_vendedor(), precoFaturado);
                } else {
                    listapreco.put(artigo.getEmail_vendedor(), artigo.getPreco_base());
                }
            }
        }
        float max = -1;
        String codigo_maisfaturou = null;
        for (Map.Entry<String, Float> entry : listapreco.entrySet()) {
            Float preco = entry.getValue();
            String email = entry.getKey();
            if (preco > max) {
                max = preco;
                codigo_maisfaturou = email;
            }
        }
        View.imprime("O vendedor que mais faturou entre " + data1 + " e " + data2 + " é: " + codigo_maisfaturou);
        View.imprime("O vendedor faturou: " + max + "€");
    }

    //2
    public void transportadoraMaisFaturou() {
        float max = -1;
        String ID_maisFaturou = null;
        for (Map.Entry<String, Transportadora> entry : hash_transportadora.entrySet()) {
            String ID_T = entry.getKey();
            Transportadora transportadora = entry.getValue();
            float precoFaturado = transportadora.getValorFaturado();
            if (precoFaturado > max) {
                max = precoFaturado;
                ID_maisFaturou = ID_T;
            }
        }
        View.imprime("A transportadora que mais faturou é: " + ID_maisFaturou);
        View.imprime("A transportadora faturou: " + max);
    }

    //3
    public void listaEncomendas(String email) {
        Utilizador utilizador = user.get(email);
        if (utilizador.getLista_artigos().isEmpty()) {
            View.erro("Este vendedor não possui nenhuma encomenda emitida!");
            return;
        }
        int paginaAtual = 1;
        int size = utilizador.getLista_artigos().size();
        int paginaLimite = (size / 10) + 1;

        List<Artigo> vendas = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            String codigo = utilizador.getLista_artigos().get(i);
            Artigo artigo = hash_artigo.get(codigo);
            if (Objects.equals(artigo.getVendido(), "S")) vendas.add(artigo);
        }

        while (true) {
            View.imprimeObj(new ArrayList<>(vendas), paginaAtual);
            View.paginador(new ArrayList<>());
            int numero = veOpcao(0, 2);
            switch (numero) {
                case 1 -> {
                    if (paginaAtual < paginaLimite) paginaAtual++;
                }
                case 2 -> {
                    if (paginaAtual > 1) paginaAtual--;
                }
                case 0 -> {
                    return;
                }
            }
        }
    }

    //4
    public void maiorVendedor(LocalDate data1, LocalDate data2) {
        Map<String, Integer> listaVendidos = new HashMap<>();
        for (String username : user.keySet()) {
            List<Artigo> artigos = user.get(username).getLista_artigos().stream()
                    .map(a -> hash_artigo.get(a)).filter(a -> Objects.equals("S", a.getVendido())).toList();
            List<Encomenda> encRealizadas = hash_encomenda.values().stream().
                    filter(e -> e.getEstado() != EstadoEncomenda.PENDENTE && e.getDataFinalizacao().isAfter(data1) &&
                            e.getDataFinalizacao().isBefore(data2))
                    .toList();
            int conta = 0;

            for (Artigo a : artigos) {
                for (Encomenda enc : encRealizadas) {
                    if (enc.getLista_artigos().contains(a.getCodigo_alfanumerico()))
                        conta++;
                }
            }
            listaVendidos.put(username, conta);
        }
        Map<String, Integer> result = listaVendidos.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (v1, v2) -> v1, LinkedHashMap::new));
        View.imprimeLista(result);
    }
    //4

    public void maiorComprador(LocalDate data1, LocalDate data2) {
        int tamanho = 0;
        Map<String, Integer> listaComprados = new HashMap<>();
        for (Map.Entry<String, Utilizador> entry : user.entrySet()) {
            String email = entry.getKey();
            Utilizador utilizador = entry.getValue();
            for (String ID_E : utilizador.getLista_encomenda()) {
                Encomenda encomenda = hash_encomenda.get(ID_E);
                if (encomenda.getEstado() != EstadoEncomenda.PENDENTE && encomenda.getDataFinalizacao().isAfter(data1) && encomenda.getDataFinalizacao().isBefore(data2))
                    tamanho += encomenda.getLista_artigos().size();
            }
            listaComprados.put(email,tamanho);
        }
        Map<String, Integer> result = listaComprados.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (v1, v2) -> v1, LinkedHashMap::new));
        View.imprimeLista(result);
    }

    //5
    public void showLucro() {
        View.imprime("O lucro da Vintage é de " + this.lucroVintage + "€");
    }


    public void ficheiroObjeto() {
        try {
            FileOutputStream fos = new FileOutputStream("MarketplaceVintage.obj");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(this);
            View.sucesso("MarketplaceVintage gravado em arquivo com sucesso!");
        } catch (IOException e) {
            View.erro("Erro ao gravar o MarketplaceVintage em arquivo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void lerFicheiroObjeto() {
        File file = new File("MarketplaceVintage.obj");
        if (!(file.exists() && file.isFile())) return;
        try {
            FileInputStream fis = new FileInputStream("MarketplaceVintage.obj");
            ObjectInputStream ois = new ObjectInputStream(fis);
            Controler controlerLido = (Controler) ois.readObject();
            this.user_atual = controlerLido.getUser_atual();
            this.user = controlerLido.getUser();
            this.hash_encomenda = controlerLido.getHash_encomenda();
            this.hash_transportadora = controlerLido.getHash_transportadora();
            this.hash_artigo = controlerLido.getArtigo();
            this.carrinho = controlerLido.getCarrinho();
            this.lucroVintage = controlerLido.getLucroVintage();
            View.sucesso("MarketplaceVintage lido do arquivo com sucesso!");
        } catch (IOException | ClassNotFoundException e) {
            View.erro("Erro ao ler o MarketplaceVintage do arquivo: " + e.getMessage());
            e.printStackTrace();
        }
    }

}