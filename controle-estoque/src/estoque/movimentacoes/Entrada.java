package estoque.movimentacoes;

import estoque.modelo.Estoque;
import java.time.LocalDateTime;

// entrada: soma a quantidade ao saldo do produto
public class Entrada extends Movimentacao {

    public Entrada(int codigoProduto, int quantidade) {
        super(codigoProduto, quantidade);
    }

    // usado ao carregar o historico do arquivo
    public Entrada(int codigoProduto, int quantidade, LocalDateTime dataHora) {
        super(codigoProduto, quantidade, dataHora);
    }

    @Override
    public void executar(Estoque estoque) {
        estoque.aumentarQuantidade(getCodigoProduto(), getQuantidade());
    }

    @Override
    public String getTipo() {
        return "ENTRADA";
    }
}
