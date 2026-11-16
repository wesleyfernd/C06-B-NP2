package estoque.excecoes;

// busca de codigo inexistente
public class ProdutoNaoEncontradoException extends EstoqueException {

    private static final long serialVersionUID = 1L;

    public ProdutoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
