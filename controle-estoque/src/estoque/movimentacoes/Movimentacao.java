package estoque.movimentacoes;

import estoque.modelo.Estoque;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

// registro de uma entrada ou saida; os dados nao mudam depois de criados
public abstract class Movimentacao {

    private final int codigoProduto;
    private final int quantidade;
    private final LocalDateTime dataHora;

    // movimentacao nova: a data e hora e o momento atual
    protected Movimentacao(int codigoProduto, int quantidade) {
        this(codigoProduto, quantidade, LocalDateTime.now());
    }

    // movimentacao lida do arquivo: preserva a data e hora original
    protected Movimentacao(int codigoProduto, int quantidade, LocalDateTime dataHora) {
        if (codigoProduto <= 0) {
            throw new IllegalArgumentException("O codigo do produto deve ser positivo.");
        }
        if (quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade da movimentacao deve ser maior que zero.");
        }
        Objects.requireNonNull(dataHora, "A data/hora da movimentacao nao pode ser nula.");
        this.codigoProduto = codigoProduto;
        this.quantidade = quantidade;
        this.dataHora = dataHora.truncatedTo(ChronoUnit.SECONDS);
    }

    public final int getCodigoProduto() {
        return codigoProduto;
    }

    public final int getQuantidade() {
        return quantidade;
    }

    public final LocalDateTime getDataHora() {
        return dataHora;
    }

    // aplica a movimentacao no estoque; se falhar, o estoque nao e alterado
    public abstract void executar(Estoque estoque);

    // texto gravado no historico: entrada ou saida
    public abstract String getTipo();

    @Override
    public String toString() {
        return dataHora + " | " + getTipo() + " | produto " + codigoProduto
                + " | quantidade " + quantidade;
    }
}
