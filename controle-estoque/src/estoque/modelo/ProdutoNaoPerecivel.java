package estoque.modelo;

// produto sem data de validade; a condicao e sempre valida
public class ProdutoNaoPerecivel extends Produto {

    public ProdutoNaoPerecivel(int codigo, String nome, int quantidade, int estoqueMinimo) {
        super(codigo, nome, quantidade, estoqueMinimo);
    }

    @Override
    public boolean verificarCondicao() {
        return true;
    }
}
