package estoque.excecoes;

// cadastro de codigo ja existente
public class ProdutoDuplicadoException extends EstoqueException {

    private static final long serialVersionUID = 1L;

    public ProdutoDuplicadoException(String mensagem) {
        super(mensagem);
    }
}
