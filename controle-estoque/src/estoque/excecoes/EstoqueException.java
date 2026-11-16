package estoque.excecoes;

// excecao base das falhas de negocio; a main captura todas com um unico catch
public class EstoqueException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public EstoqueException(String mensagem) {
        super(mensagem);
    }
}
