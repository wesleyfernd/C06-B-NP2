package estoque.testes;

import estoque.excecoes.EstoqueException;
import estoque.excecoes.EstoqueInsuficienteException;
import estoque.excecoes.ProdutoNaoEncontradoException;
import estoque.modelo.Estoque;
import estoque.modelo.Produto;
import estoque.modelo.ProdutoNaoPerecivel;
import estoque.movimentacoes.Entrada;
import estoque.movimentacoes.Movimentacao;
import estoque.movimentacoes.MovimentacaoService;
import estoque.movimentacoes.Saida;
import estoque.persistencia.RepositorioDados;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// testa entradas, saidas e historico usando um repositorio falso em memoria
public class TesteMovimentacoes {

    // repositorio de mentira: guarda tudo em listas, sem arquivo.
    static class RepositorioMemoria implements RepositorioDados {
        final List<Movimentacao> movimentos = new ArrayList<>();
        int salvamentosDeProdutos = 0;
        boolean falhar = false;

        @Override public List<Produto> carregarProdutos() { return new ArrayList<>(); }
        @Override public void salvarProdutos(List<Produto> produtos) {
            if (falhar) throw new IllegalStateException("disco cheio");
            salvamentosDeProdutos++;
        }
        @Override public void registrarMovimentacao(Movimentacao m) { movimentos.add(m); }
        @Override public List<Movimentacao> carregarHistorico() { return new ArrayList<>(movimentos); }
    }

    static int falhas = 0;

    static void verifica(boolean condicao, String descricao) {
        if (condicao) {
            System.out.println("[OK] " + descricao);
        } else {
            falhas++;
            System.out.println("[FALHOU] " + descricao);
        }
    }

    // executa o codigo e informa se lancou a excecao esperada.
    static boolean lanca(Class<? extends Throwable> tipo, Runnable codigo) {
        try {
            codigo.run();
            return false;
        } catch (Throwable t) {
            return tipo.isInstance(t);
        }
    }

    public static void main(String[] args) {
        Estoque estoque = new Estoque();
        estoque.adicionarProduto(new ProdutoNaoPerecivel(101, "Arroz", 10, 5));
        RepositorioMemoria repo = new RepositorioMemoria();
        MovimentacaoService servico = new MovimentacaoService(estoque, repo);

        // --- entrada e saida validas
        servico.registrarEntrada(101, 5);
        verifica(estoque.buscarProduto(101).getQuantidade() == 15, "entrada de 5 soma ao saldo (10 -> 15)");
        servico.registrarSaida(101, 3);
        verifica(estoque.buscarProduto(101).getQuantidade() == 12, "saida de 3 subtrai do saldo (15 -> 12)");

        // --- historico
        List<Movimentacao> hist = servico.consultarHistorico();
        verifica(hist.size() == 2, "historico tem 2 movimentacoes");
        verifica(hist.get(0).getTipo().equals("ENTRADA") && hist.get(1).getTipo().equals("SAIDA"),
                "historico preserva a ordem (ENTRADA, SAIDA)");
        verifica(lanca(UnsupportedOperationException.class, () -> hist.add(new Entrada(101, 1))),
                "historico devolvido e somente leitura");
        verifica(repo.movimentos.size() == 2 && repo.salvamentosDeProdutos == 2,
                "cada movimentacao foi registrada e os saldos salvos no repositorio");

        // --- regras de rejeicao
        verifica(lanca(EstoqueInsuficienteException.class, () -> servico.registrarSaida(101, 13)),
                "saida maior que o saldo lanca EstoqueInsuficienteException");
        verifica(estoque.buscarProduto(101).getQuantidade() == 12, "saida rejeitada NAO altera o estoque");
        verifica(servico.consultarHistorico().size() == 2 && repo.movimentos.size() == 2,
                "saida rejeitada NAO entra no historico nem e gravada");
        verifica(lanca(IllegalArgumentException.class, () -> servico.registrarEntrada(101, 0)),
                "entrada com quantidade zero e rejeitada");
        verifica(lanca(IllegalArgumentException.class, () -> servico.registrarSaida(101, -4)),
                "saida com quantidade negativa e rejeitada");
        verifica(lanca(ProdutoNaoEncontradoException.class, () -> servico.registrarEntrada(999, 1)),
                "produto inexistente lanca ProdutoNaoEncontradoException");
        verifica(servico.consultarHistorico().size() == 2, "rejeicoes nao poluem o historico");
        verifica(estoque.buscarProduto(101).getQuantidade() == 12, "saldo continua 12 depois das rejeicoes");

        // --- saida exatamente igual ao saldo e permitida (zera o estoque)
        servico.registrarSaida(101, 12);
        verifica(estoque.buscarProduto(101).getQuantidade() == 0, "saida igual ao saldo zera o estoque");

        // --- polimorfismo: uma lista de movimentacao, comportamentos diferentes
        Estoque outro = new Estoque();
        outro.adicionarProduto(new ProdutoNaoPerecivel(7, "Feijao", 10, 2));
        List<Movimentacao> lote = List.of(new Entrada(7, 5), new Saida(7, 2));
        for (Movimentacao m : lote) {
            m.executar(outro);
        }
        verifica(outro.buscarProduto(7).getQuantidade() == 13, "polimorfismo: +5 e -2 sobre 10 = 13");

        // --- historico carregado de um repositorio ja existente
        RepositorioMemoria repo2 = new RepositorioMemoria();
        LocalDateTime antiga = LocalDateTime.of(2026, 1, 15, 9, 30, 0);
        repo2.movimentos.add(new Entrada(7, 3, antiga));
        MovimentacaoService servico2 = new MovimentacaoService(outro, repo2);
        verifica(servico2.consultarHistorico().size() == 1
                && servico2.consultarHistorico().get(0).getDataHora().equals(antiga),
                "servico carrega historico existente preservando a data/hora");
        servico2.registrarEntrada(7, 1);
        verifica(servico2.consultarHistorico().size() == 2, "novas movimentacoes sao adicionadas ao historico carregado");

        // --- falha de persistencia
        RepositorioMemoria repoRuim = new RepositorioMemoria();
        MovimentacaoService servico3 = new MovimentacaoService(outro, repoRuim);
        repoRuim.falhar = true;
        int antes = outro.buscarProduto(7).getQuantidade();
        verifica(lanca(EstoqueException.class, () -> servico3.registrarEntrada(7, 2)),
                "falha ao salvar vira EstoqueException com mensagem clara");
        verifica(outro.buscarProduto(7).getQuantidade() == antes + 2,
                "falha ao salvar: operacao permanece aplicada em memoria (Main salva ao encerrar)");

        // --- objetos de movimentacao
        Movimentacao e = new Entrada(101, 4);
        verifica(e.getCodigoProduto() == 101 && e.getQuantidade() == 4 && e.getDataHora() != null,
                "getters de Movimentacao funcionam");
        verifica(lanca(IllegalArgumentException.class, () -> new Entrada(0, 4)),
                "codigo de produto nao positivo e rejeitado");
        verifica(lanca(NullPointerException.class, () -> new MovimentacaoService(null, repo)),
                "servico rejeita estoque nulo");

        System.out.println();
        if (falhas == 0) {
            System.out.println("TODOS OS TESTES DE MOVIMENTACOES PASSARAM.");
        } else {
            System.out.println(falhas + " TESTE(S) FALHARAM.");
            System.exit(1);
        }
    }
}
