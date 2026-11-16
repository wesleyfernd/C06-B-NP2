package estoque.modelo;

import estoque.excecoes.ProdutoDuplicadoException;
import estoque.excecoes.ProdutoNaoEncontradoException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

// guarda os produtos por codigo, mantendo a ordem de cadastro
public class Estoque {

    private final Map<Integer, Produto> produtos;

    public Estoque() {
        this.produtos = new LinkedHashMap<>();
    }

    // rejeita produto nulo e codigo ja cadastrado
    public void adicionarProduto(Produto produto) {
        Objects.requireNonNull(produto, "O produto nao pode ser nulo.");
        int codigo = produto.getCodigo();
        if (produtos.containsKey(codigo)) {
            throw new ProdutoDuplicadoException(
                    "Ja existe um produto com o codigo " + codigo + ".");
        }
        produtos.put(codigo, produto);
    }

    // devolve o objeto original; codigo inexistente lanca excecao (nunca retorna null)
    public Produto buscarProduto(int codigo) {
        Produto produto = produtos.get(codigo);
        if (produto == null) {
            throw new ProdutoNaoEncontradoException(
                    "Produto com codigo " + codigo + " nao encontrado.");
        }
        return produto;
    }

    // devolve uma copia da lista (os produtos continuam sendo os mesmos objetos)
    public List<Produto> listarProdutos() {
        return new ArrayList<>(produtos.values());
    }

    // localiza o produto e delega a alteracao do saldo a ele
    public void aumentarQuantidade(int codigo, int qtd) {
        buscarProduto(codigo).aumentarQuantidade(qtd);
    }

    public void diminuirQuantidade(int codigo, int qtd) {
        buscarProduto(codigo).diminuirQuantidade(qtd);
    }
}
