package estoque.modelo;

import java.time.LocalDate;
import java.util.Objects;

// produto com data de validade obrigatoria
public class ProdutoPerecivel extends Produto {

    private final LocalDate dataValidade;

    public ProdutoPerecivel(int codigo, String nome, int quantidade,
                            int estoqueMinimo, LocalDate dataValidade) {
        super(codigo, nome, quantidade, estoqueMinimo);
        this.dataValidade = Objects.requireNonNull(dataValidade,
                "A data de validade e obrigatoria para produtos pereciveis.");
    }

    public LocalDate getDataValidade() {
        return dataValidade;
    }

    // vencido quando a validade e anterior a hoje; validade igual a hoje ainda e valida
    @Override
    public boolean verificarCondicao() {
        return !dataValidade.isBefore(LocalDate.now());
    }
}
