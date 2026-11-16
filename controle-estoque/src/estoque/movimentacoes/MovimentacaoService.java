package estoque.movimentacoes;

import estoque.excecoes.EstoqueException;
import estoque.modelo.Estoque;
import estoque.persistencia.RepositorioDados;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

// coordena cada movimentacao: aplica no estoque, guarda no historico e persiste
public class MovimentacaoService {

    private final Estoque estoque;
    private final RepositorioDados repositorio;
    private final List<Movimentacao> historico;

    // ja carrega o historico gravado (lista vazia se ainda nao houver)
    public MovimentacaoService(Estoque estoque, RepositorioDados repositorio) {
        this.estoque = Objects.requireNonNull(estoque, "O estoque nao pode ser nulo.");
        this.repositorio = Objects.requireNonNull(repositorio, "O repositorio nao pode ser nulo.");
        this.historico = new ArrayList<>(Objects.requireNonNull(
                repositorio.carregarHistorico(), "O historico carregado nao pode ser nulo."));
    }

    public void registrarEntrada(int codigo, int quantidade) {
        processar(new Entrada(codigo, quantidade));
    }

    public void registrarSaida(int codigo, int quantidade) {
        processar(new Saida(codigo, quantidade));
    }

    // copia somente leitura, para o historico so mudar pelos metodos registrar
    public List<Movimentacao> consultarHistorico() {
        return Collections.unmodifiableList(new ArrayList<>(historico));
    }

    // fluxo unico para qualquer tipo de movimentacao:
    // 1) executa no estoque (se falhar, nada foi alterado nem gravado)
    // 2) adiciona ao historico em memoria
    // 3) grava o historico e os saldos no repositorio
    private void processar(Movimentacao movimentacao) {
        movimentacao.executar(estoque);
        historico.add(movimentacao);
        try {
            repositorio.registrarMovimentacao(movimentacao);
            repositorio.salvarProdutos(estoque.listarProdutos());
        } catch (RuntimeException e) {
            // a operacao ja valeu em memoria; a main salva de novo ao encerrar
            throw new EstoqueException("Movimentacao aplicada em memoria, mas nao foi possivel salvar: "
                    + e.getMessage());
        }
    }
}
