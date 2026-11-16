package estoque.movimentacoes;

import estoque.modelo.Estoque;
import java.time.LocalDateTime;

// saida: subtrai a quantidade do saldo; saldo insuficiente lanca excecao
public class Saida extends Movimentacao {

    public Saida(int codigoProduto, int quantidade) {
        super(codigoProduto, quantidade);
    }

    // usado ao carregar o historico do arquivo
    public Saida(int codigoProduto, int quantidade, LocalDateTime dataHora) {
        super(codigoProduto, quantidade, dataHora);
    }

    @Override
    public void executar(Estoque estoque) {
        estoque.diminuirQuantidade(getCodigoProduto(), getQuantidade());
    }

    @Override
    public String getTipo() {
        return "SAIDA";
    }
}
