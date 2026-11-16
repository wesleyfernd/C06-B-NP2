package estoque.excecoes;

// saida maior que o saldo disponivel
public class EstoqueInsuficienteException extends EstoqueException {

    private static final long serialVersionUID = 1L;

    public EstoqueInsuficienteException(String mensagem) {
        super(mensagem);
    }
}
