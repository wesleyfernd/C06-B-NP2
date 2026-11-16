package estoque;

import estoque.excecoes.EstoqueException;
import estoque.modelo.Estoque;
import estoque.modelo.Produto;
import estoque.monitoramento.MonitorEstoque;
import estoque.monitoramento.Notificavel;
import estoque.monitoramento.NotificadorConsole;
import estoque.movimentacoes.Movimentacao;
import estoque.movimentacoes.MovimentacaoService;
import estoque.persistencia.RepositorioArquivo;
import estoque.persistencia.RepositorioDados;
import java.util.InputMismatchException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

// ponto de entrada: monta os objetos, executa o menu e salva os dados ao sair
public class Main {

    private static final int OPCAO_SAIR = 0;
    private static final int OPCAO_INVALIDA = -1;

    public static void main(String[] args) {
        RepositorioDados repositorio = new RepositorioArquivo();
        Estoque estoque = new Estoque();
        MovimentacaoService servico;

        // sem os dados carregados o programa nao continua
        try {
            carregarProdutos(repositorio, estoque);
            servico = new MovimentacaoService(estoque, repositorio);
        } catch (RuntimeException e) {
            System.out.println("Erro ao carregar os dados: " + e.getMessage());
            System.out.println("Execute o programa a partir da pasta raiz do projeto (onde fica a pasta dados).");
            return;
        }

        Notificavel notificador = new NotificadorConsole();
        MonitorEstoque monitor = new MonitorEstoque(estoque, notificador);

        verificarAlertas(monitor);

        try (Scanner scanner = new Scanner(System.in)) {
            executarMenu(scanner, estoque, servico, monitor);
        } catch (NoSuchElementException e) {
            // entrada encerrada (fim do arquivo): segue para salvar
            System.out.println("\nEntrada encerrada.");
        }

        salvarProdutos(repositorio, estoque);
    }

    // repete o menu ate o usuario escolher sair
    private static void executarMenu(Scanner scanner, Estoque estoque,
                                     MovimentacaoService servico, MonitorEstoque monitor) {
        int opcao;
        do {
            exibirMenu();
            try {
                opcao = lerInteiro(scanner, "Escolha uma opcao: ");
            } catch (IllegalArgumentException e) {
                System.out.println("Erro: " + e.getMessage());
                opcao = OPCAO_INVALIDA;
                continue;
            }

            switch (opcao) {
                case 1:
                    registrarEntrada(scanner, servico, monitor);
                    break;
                case 2:
                    registrarSaida(scanner, servico, monitor);
                    break;
                case 3:
                    consultarHistorico(servico);
                    break;
                case 4:
                    consultarProdutos(estoque);
                    break;
                case 5:
                    System.out.println("\nVerificando estoque...");
                    verificarAlertas(monitor);
                    System.out.println("Verificacao concluida.");
                    break;
                case OPCAO_SAIR:
                    break;
                default:
                    System.out.println("Opcao invalida.");
            }
        } while (opcao != OPCAO_SAIR);
    }

    private static void exibirMenu() {
        System.out.println("\n===== SISTEMA DE ESTOQUE =====");
        System.out.println("1 - Registrar entrada");
        System.out.println("2 - Registrar saida");
        System.out.println("3 - Consultar historico");
        System.out.println("4 - Consultar produtos em estoque");
        System.out.println("5 - Ver alertas de estoque");
        System.out.println("0 - Sair");
    }

    // le um inteiro; texto invalido e descartado e vira excecao de argumento invalido
    private static int lerInteiro(Scanner scanner, String pergunta) {
        System.out.print(pergunta);
        try {
            return scanner.nextInt();
        } catch (InputMismatchException e) {
            scanner.nextLine();
            throw new IllegalArgumentException("digite apenas numeros.");
        }
    }

    private static void registrarEntrada(Scanner scanner, MovimentacaoService servico, MonitorEstoque monitor) {
        try {
            int codigo = lerInteiro(scanner, "Digite o codigo do produto: ");
            int quantidade = lerInteiro(scanner, "Digite a quantidade de entrada: ");
            servico.registrarEntrada(codigo, quantidade);
            System.out.println("Entrada registrada com sucesso!");
        } catch (EstoqueException | IllegalArgumentException e) {
            System.out.println("Erro ao registrar entrada: " + e.getMessage());
            return;
        }
        verificarAlertas(monitor);
    }

    private static void registrarSaida(Scanner scanner, MovimentacaoService servico, MonitorEstoque monitor) {
        try {
            int codigo = lerInteiro(scanner, "Digite o codigo do produto: ");
            int quantidade = lerInteiro(scanner, "Digite a quantidade de saida: ");
            servico.registrarSaida(codigo, quantidade);
            System.out.println("Saida registrada com sucesso!");
        } catch (EstoqueException | IllegalArgumentException e) {
            System.out.println("Erro ao registrar saida: " + e.getMessage());
            return;
        }
        verificarAlertas(monitor);
    }

    private static void consultarHistorico(MovimentacaoService servico) {
        List<Movimentacao> historico = servico.consultarHistorico();
        System.out.println("\nHistorico de movimentacoes:");
        for (Movimentacao movimentacao : historico) {
            System.out.println(movimentacao.getTipo()
                    + " - Produto: " + movimentacao.getCodigoProduto()
                    + " - Quantidade: " + movimentacao.getQuantidade()
                    + " - Data: " + movimentacao.getDataHora());
        }
    }

    private static void consultarProdutos(Estoque estoque) {
        System.out.println("\nProdutos em estoque:");
        for (Produto produto : estoque.listarProdutos()) {
            System.out.println("Codigo: " + produto.getCodigo()
                    + " - Nome: " + produto.getNome()
                    + " - Quantidade: " + produto.getQuantidade());
        }
    }

    // um alerta que falha ao ser enviado nao interrompe o programa
    private static void verificarAlertas(MonitorEstoque monitor) {
        try {
            monitor.verificarEstoque();
        } catch (EstoqueException e) {
            System.out.println("Erro ao verificar alertas: " + e.getMessage());
        }
    }

    // passa os produtos do arquivo para o estoque; codigo repetido no arquivo lanca excecao
    private static void carregarProdutos(RepositorioDados repositorio, Estoque estoque) {
        for (Produto produto : repositorio.carregarProdutos()) {
            estoque.adicionarProduto(produto);
        }
    }

    private static void salvarProdutos(RepositorioDados repositorio, Estoque estoque) {
        try {
            repositorio.salvarProdutos(estoque.listarProdutos());
            System.out.println("Dados salvos com sucesso!");
        } catch (RuntimeException e) {
            System.out.println("Erro ao salvar os dados: " + e.getMessage());
        }
    }
}
