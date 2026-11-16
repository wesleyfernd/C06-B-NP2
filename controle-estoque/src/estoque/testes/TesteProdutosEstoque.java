package estoque.testes;

import estoque.modelo.Estoque;
import estoque.modelo.Produto;
import estoque.modelo.ProdutoNaoPerecivel;
import estoque.modelo.ProdutoPerecivel;
import estoque.excecoes.EstoqueInsuficienteException;
import estoque.excecoes.ProdutoDuplicadoException;
import estoque.excecoes.ProdutoNaoEncontradoException;
import java.time.LocalDate;
import java.util.List;

// testa produto, subtipos e estoque; roda sem arquivos
public class TesteProdutosEstoque {

    public static void main(String[] args) {
        Estoque estoque = new Estoque();

        // variaveis do tipo produto recebem objetos dos dois subtipos
        Produto arroz = new ProdutoNaoPerecivel(101, "Arroz", 12, 10);
        Produto leite = new ProdutoPerecivel(102, "Leite", 4, 5, LocalDate.now().plusDays(7));
        Produto vencido = new ProdutoPerecivel(103, "Iogurte", 2, 3, LocalDate.now().minusDays(1));

        estoque.adicionarProduto(arroz);
        estoque.adicionarProduto(leite);
        estoque.adicionarProduto(vencido);

        // a mesma chamada executa a versao de cada subtipo
        for (Produto produto : estoque.listarProdutos()) {
            System.out.println(produto.getNome() + " | quantidade=" + produto.getQuantidade()
                    + " | condicao=" + produto.verificarCondicao()
                    + " | estoque baixo=" + produto.estaAbaixoDoMinimo());
        }

        conferir(estoque.listarProdutos().size() == 3, "Cadastro/listagem");
        conferir(arroz.verificarCondicao(), "Nao perecivel valido na regra modelada");
        conferir(leite.verificarCondicao(), "Perecivel dentro da validade");
        conferir(!vencido.verificarCondicao(), "Perecivel vencido");
        conferir(leite.estaAbaixoDoMinimo(), "Alerta de estoque baixo");
        conferir(!arroz.estaAbaixoDoMinimo(), "Sem alerta quando >= minimo");

        estoque.aumentarQuantidade(101, 3);
        conferir(arroz.getQuantidade() == 15, "Entrada de quantidade");
        estoque.diminuirQuantidade(101, 5);
        conferir(arroz.getQuantidade() == 10, "Saida de quantidade");

        esperarExcecao(EstoqueInsuficienteException.class,
                () -> estoque.diminuirQuantidade(101, 11), "Saldo insuficiente");
        conferir(arroz.getQuantidade() == 10, "Saldo permanece apos falha");
        esperarExcecao(ProdutoDuplicadoException.class,
                () -> estoque.adicionarProduto(new ProdutoNaoPerecivel(101, "Outro", 1, 1)),
                "Produto duplicado");
        esperarExcecao(ProdutoNaoEncontradoException.class,
                () -> estoque.buscarProduto(999), "Produto inexistente");
        esperarExcecao(IllegalArgumentException.class,
                () -> estoque.aumentarQuantidade(101, 0), "Quantidade zero");
        esperarExcecao(IllegalArgumentException.class,
                () -> new ProdutoNaoPerecivel(105, "", 1, 1), "Nome vazio");
        esperarExcecao(IllegalArgumentException.class,
                () -> new ProdutoNaoPerecivel(106, "Teste", -1, 1), "Quantidade negativa");
        esperarExcecao(IllegalArgumentException.class,
                () -> estoque.aumentarQuantidade(101, Integer.MAX_VALUE), "Overflow");

        List<Produto> lista = estoque.listarProdutos();
        lista.clear();  // altera so a copia da lista, nao o mapa do estoque
        conferir(estoque.listarProdutos().size() == 3, "Copia defensiva da lista");
        System.out.println("\nTODOS OS TESTES DE PRODUTOS E ESTOQUE PASSARAM.");
    }

    private static void conferir(boolean condicao, String descricao) {
        if (!condicao) {
            throw new AssertionError("Falhou: " + descricao);
        }
        System.out.println("[OK] " + descricao);
    }

    private static void esperarExcecao(Class<? extends Throwable> tipo,
                                      Runnable operacao, String descricao) {
        try {
            operacao.run();
        } catch (Throwable erro) {
            if (tipo.isInstance(erro)) {
                System.out.println("[OK] " + descricao + " (excecao esperada)");
                return;
            }
            throw new AssertionError("Excecao incorreta em " + descricao, erro);
        }
        throw new AssertionError("Faltou excecao em " + descricao);
    }
}
