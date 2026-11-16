package estoque.monitoramento;

// simula o envio de mensagem entre dois setores imprimindo no console
public class NotificadorConsole implements Notificavel {

    private final String origem;
    private final String destino;

    // origem e destino padrao do projeto
    public NotificadorConsole() {
        this("Setor de Estoque", "Setor de Compras");
    }

    public NotificadorConsole(String origem, String destino) {
        if (origem == null || origem.equals("")) {
            throw new IllegalArgumentException("A origem nao pode ser vazia.");
        }
        if (destino == null || destino.equals("")) {
            throw new IllegalArgumentException("O destino nao pode ser vazio.");
        }
        this.origem = origem;
        this.destino = destino;
    }

    // mensagem vazia conta como falha de envio
    @Override
    public void notificar(String mensagem) {
        if (mensagem == null || mensagem.equals("")) {
            throw new IllegalArgumentException("A mensagem da notificacao nao pode ser vazia.");
        }
        System.out.println(origem + " -> " + destino + ": " + mensagem);
    }

    public String getOrigem() {
        return origem;
    }

    public String getDestino() {
        return destino;
    }
}
