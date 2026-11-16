package estoque.monitoramento;

import estoque.excecoes.EstoqueException;
import estoque.modelo.Estoque;
import estoque.modelo.Produto;
import estoque.modelo.ProdutoPerecivel;
import java.util.List;

// verifica o estoque e envia alertas; so consulta, nunca altera saldo
public class MonitorEstoque {

    private final Estoque estoque;
    private final Notificavel notificador;

    public MonitorEstoque(Estoque estoque, Notificavel notificador) {
        if (estoque == null) {
            throw new IllegalArgumentException("O estoque nao pode ser nulo.");
        }
        if (notificador == null) {
            throw new IllegalArgumentException("O notificador nao pode ser nulo.");
        }
        this.estoque = estoque;
        this.notificador = notificador;
    }

    // percorre todos os produtos e envia um alerta para cada problema encontrado
    public void verificarEstoque() {
        List<Produto> produtos = estoque.listarProdutos();

        for (Produto produto : produtos) {
            // saldo zero gera o alerta de esgotado; saldo baixo so alerta se nao estiver zerado
            if (produto.getQuantidade() == 0) {
                enviar("PRODUTO ESGOTADO - " + produto.getNome()
                        + " (codigo " + produto.getCodigo() + "): o estoque acabou.");
            } else if (produto.estaAbaixoDoMinimo()) {
                enviar("ESTOQUE BAIXO - " + produto.getNome()
                        + " (codigo " + produto.getCodigo() + "): quantidade atual "
                        + produto.getQuantidade() + ", minimo " + produto.getEstoqueMinimo() + ".");
            }

            // vencido e independente do saldo: o mesmo produto pode gerar dois alertas
            if (!produto.verificarCondicao()) {
                String detalhe = "";
                if (produto instanceof ProdutoPerecivel) {
                    detalhe = " Validade: " + ((ProdutoPerecivel) produto).getDataValidade() + ".";
                }
                enviar("PRODUTO VENCIDO - " + produto.getNome()
                        + " (codigo " + produto.getCodigo() + ")." + detalhe);
            }
        }
    }

    // falha do notificador vira excecao do grupo para a main tratar
    private void enviar(String mensagem) {
        try {
            notificador.notificar(mensagem);
        } catch (RuntimeException e) {
            throw new EstoqueException("Falha ao enviar notificacao: " + e.getMessage());
        }
    }
}
