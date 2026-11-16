package estoque.monitoramento;

// contrato de quem envia notificacoes; esconde como a mensagem e entregue
public interface Notificavel {

    void notificar(String mensagem);
}
