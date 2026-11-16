package estoque.persistencia;

import estoque.modelo.Produto;
import estoque.movimentacoes.Movimentacao;
import java.util.List;

// contrato de persistencia; quem usa nao sabe se os dados vao para arquivo ou banco
public interface RepositorioDados {

    List<Produto> carregarProdutos();

    void salvarProdutos(List<Produto> produtos);

    // acrescenta uma movimentacao ao historico gravado
    void registrarMovimentacao(Movimentacao m);

    // lista vazia se ainda nao houver historico (nunca null)
    List<Movimentacao> carregarHistorico();
}
