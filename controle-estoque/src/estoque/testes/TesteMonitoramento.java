package estoque.testes;

import estoque.excecoes.EstoqueException;
import estoque.modelo.Estoque;
import estoque.modelo.ProdutoNaoPerecivel;
import estoque.modelo.ProdutoPerecivel;
import estoque.monitoramento.MonitorEstoque;
import estoque.monitoramento.Notificavel;
import estoque.monitoramento.NotificadorConsole;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// testa alertas do monitor usando um notificador falso em memoria
public class TesteMonitoramento {

    static int falhas = 0;

    static void verifica(boolean condicao, String descricao) {
        if (condicao) {
            System.out.println("[OK] " + descricao);
        } else {
            falhas++;
            System.out.println("[FALHOU] " + descricao);
        }
    }

    // retorna true se o monitor lancou estoqueexception ao verificar.
    static boolean monitorFalhou(MonitorEstoque monitor) {
        try {
            monitor.verificarEstoque();
            return false;
        } catch (EstoqueException e) {
            return true;
        }
    }

    // retorna true se criar o monitor com estes argumentos lanca illegalargumentexception.
    static boolean monitorRejeitou(Estoque estoque, Notificavel notificador) {
        try {
            new MonitorEstoque(estoque, notificador);
            return false;
        } catch (IllegalArgumentException e) {
            return true;
        }
    }

    public static void main(String[] args) {
        // --- estoque sem problemas: nenhum alerta
        Estoque estoque = new Estoque();
        estoque.adicionarProduto(new ProdutoNaoPerecivel(1, "Arroz", 10, 5));
        estoque.adicionarProduto(new ProdutoPerecivel(2, "Leite", 20, 5, LocalDate.now().plusDays(10)));
        NotificadorMemoria notificador = new NotificadorMemoria();
        MonitorEstoque monitor = new MonitorEstoque(estoque, notificador);

        monitor.verificarEstoque();
        verifica(notificador.mensagens.size() == 0, "estoque normal: nenhum alerta enviado");

        // --- abaixo do minimo (regra: quantidade < minimo)
        estoque.diminuirQuantidade(1, 5);  // 5 == minimo: ainda nao alerta
        monitor.verificarEstoque();
        verifica(notificador.mensagens.size() == 0, "quantidade igual ao minimo nao gera alerta");

        estoque.diminuirQuantidade(1, 1);  // 4 < 5: alerta
        monitor.verificarEstoque();
        verifica(notificador.mensagens.size() == 1
                        && notificador.mensagens.get(0).startsWith("ESTOQUE BAIXO")
                        && notificador.mensagens.get(0).contains("Arroz"),
                "quantidade abaixo do minimo gera alerta ESTOQUE BAIXO");

        // --- estoque zerado: alerta produto esgotado (e so um alerta, nao dois)
        notificador.mensagens.clear();
        estoque.diminuirQuantidade(1, 4);  // 4 -> 0
        monitor.verificarEstoque();
        verifica(notificador.mensagens.size() == 1
                        && notificador.mensagens.get(0).startsWith("PRODUTO ESGOTADO")
                        && notificador.mensagens.get(0).contains("Arroz"),
                "quantidade zero gera alerta PRODUTO ESGOTADO");
        estoque.aumentarQuantidade(1, 4);  // volta para 4 para os proximos testes

        // --- o monitor nao altera o saldo
        verifica(estoque.buscarProduto(1).getQuantidade() == 4, "monitor nao altera o saldo do produto");

        // --- perecivel vencido
        notificador.mensagens.clear();
        Estoque outro = new Estoque();
        outro.adicionarProduto(new ProdutoPerecivel(3, "Iogurte", 50, 5, LocalDate.now().minusDays(1)));
        new MonitorEstoque(outro, notificador).verificarEstoque();
        verifica(notificador.mensagens.size() == 1
                        && notificador.mensagens.get(0).startsWith("PRODUTO VENCIDO")
                        && notificador.mensagens.get(0).contains("Iogurte"),
                "perecivel vencido gera alerta PRODUTO VENCIDO");

        // --- validade no dia de hoje ainda e valida (regra do produto)
        notificador.mensagens.clear();
        Estoque hoje = new Estoque();
        hoje.adicionarProduto(new ProdutoPerecivel(4, "Queijo", 50, 5, LocalDate.now()));
        new MonitorEstoque(hoje, notificador).verificarEstoque();
        verifica(notificador.mensagens.size() == 0, "validade no dia de hoje nao gera alerta");

        // --- dois problemas no mesmo produto: dois alertas
        notificador.mensagens.clear();
        Estoque duplo = new Estoque();
        duplo.adicionarProduto(new ProdutoPerecivel(5, "Manteiga", 1, 5, LocalDate.now().minusDays(2)));
        new MonitorEstoque(duplo, notificador).verificarEstoque();
        verifica(notificador.mensagens.size() == 2, "abaixo do minimo e vencido gera 2 alertas");

        // --- falha no envio vira estoqueexception
        notificador.falhar = true;
        verifica(monitorFalhou(monitor), "falha no notificador vira EstoqueException");
        notificador.falhar = false;

        // --- notificador do console
        NotificadorConsole console = new NotificadorConsole();
        verifica(console.getOrigem().equals("Setor de Estoque") && console.getDestino().equals("Setor de Compras"),
                "NotificadorConsole usa Estoque -> Compras por padrao");

        boolean rejeitouMensagem = false;
        try {
            console.notificar("");
        } catch (IllegalArgumentException e) {
            rejeitouMensagem = true;
        }
        verifica(rejeitouMensagem, "mensagem vazia e rejeitada pelo NotificadorConsole");

        boolean rejeitouOrigem = false;
        try {
            new NotificadorConsole("", "Compras");
        } catch (IllegalArgumentException e) {
            rejeitouOrigem = true;
        }
        verifica(rejeitouOrigem, "origem vazia e rejeitada");

        Notificavel referencia = console;  // polimorfismo: tipo da interface
        referencia.notificar("Teste de comunicacao: mensagem simulada enviada.");

        // --- validacoes do construtor
        verifica(monitorRejeitou(null, notificador), "monitor rejeita estoque nulo");
        verifica(monitorRejeitou(estoque, null), "monitor rejeita notificador nulo");

        System.out.println();
        if (falhas == 0) {
            System.out.println("TODOS OS TESTES DE MONITORAMENTO PASSARAM.");
        } else {
            System.out.println(falhas + " TESTE(S) FALHARAM.");
            System.exit(1);
        }
    }
}

// notificador de mentira: guarda as mensagens em uma lista em vez de imprimir
class NotificadorMemoria implements Notificavel {
    List<String> mensagens = new ArrayList<>();
    boolean falhar = false;

    @Override
    public void notificar(String mensagem) {
        if (falhar) {
            throw new IllegalStateException("canal fora do ar");
        }
        mensagens.add(mensagem);
    }
}
