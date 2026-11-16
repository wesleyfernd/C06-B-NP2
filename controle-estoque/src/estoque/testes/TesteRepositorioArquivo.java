package estoque.testes;

import estoque.modelo.Produto;
import estoque.modelo.ProdutoNaoPerecivel;
import estoque.modelo.ProdutoPerecivel;
import estoque.movimentacoes.Entrada;
import estoque.movimentacoes.Movimentacao;
import estoque.movimentacoes.Saida;
import estoque.persistencia.RepositorioArquivo;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// testa leitura e gravacao em csv usando arquivos temporarios (os arquivos reais nao sao tocados)
public class TesteRepositorioArquivo {

    static int falhas = 0;

    static void verifica(boolean condicao, String descricao) {
        if (condicao) {
            System.out.println("[OK] " + descricao);
        } else {
            falhas++;
            System.out.println("[FALHOU] " + descricao);
        }
    }

    // retorna true se o codigo lancou a excecao esperada
    static boolean lanca(Class<? extends Throwable> tipo, Runnable codigo) {
        try {
            codigo.run();
            return false;
        } catch (Throwable t) {
            return tipo.isInstance(t);
        }
    }

    public static void main(String[] args) throws IOException {
        Path pasta = Files.createTempDirectory("teste-estoque");
        Path produtosCsv = pasta.resolve("dados").resolve("produtos.csv");
        Path historicoCsv = pasta.resolve("dados").resolve("historico.csv");
        RepositorioArquivo repo = new RepositorioArquivo(produtosCsv, historicoCsv);

        // --- arquivos ausentes
        verifica(repo.carregarHistorico().isEmpty(), "historico ausente devolve lista vazia");
        verifica(lanca(UncheckedIOException.class, repo::carregarProdutos),
                "produtos.csv ausente lanca UncheckedIOException");

        // --- salvar e carregar produtos (a pasta dados e criada sozinha)
        List<Produto> produtos = new ArrayList<>();
        produtos.add(new ProdutoPerecivel(1, "Leite", 5, 5, LocalDate.of(2027, 1, 20)));
        produtos.add(new ProdutoNaoPerecivel(2, "Arroz", 12, 10));
        repo.salvarProdutos(produtos);
        List<Produto> lidos = repo.carregarProdutos();
        verifica(lidos.size() == 2, "carrega os 2 produtos salvos");
        verifica(lidos.get(0) instanceof ProdutoPerecivel
                        && ((ProdutoPerecivel) lidos.get(0)).getDataValidade().equals(LocalDate.of(2027, 1, 20)),
                "perecivel volta com a data de validade");
        verifica(lidos.get(1) instanceof ProdutoNaoPerecivel && lidos.get(1).getQuantidade() == 12,
                "nao perecivel volta com a quantidade");

        // --- historico: acrescenta sem apagar o que ja existe
        LocalDateTime data = LocalDateTime.of(2026, 11, 13, 20, 16, 37);
        repo.registrarMovimentacao(new Entrada(1, 5, data));
        repo.registrarMovimentacao(new Saida(2, 3, data));
        List<Movimentacao> historico = repo.carregarHistorico();
        verifica(historico.size() == 2, "historico tem as 2 movimentacoes gravadas");
        verifica(historico.get(0).getTipo().equals("ENTRADA") && historico.get(1).getTipo().equals("SAIDA"),
                "historico preserva a ordem e o tipo");
        verifica(historico.get(0).getDataHora().equals(data), "historico preserva a data e hora");

        // --- tolerancia e validacao de conteudo
        Files.writeString(produtosCsv, "P;1;Leite;5;5;2027-01-20\n\nN;2;Arroz;12;10;\n\n");
        verifica(repo.carregarProdutos().size() == 2, "linhas em branco sao ignoradas");

        Files.writeString(produtosCsv, "N;2;Arroz\n");
        verifica(lanca(IllegalStateException.class, repo::carregarProdutos), "linha incompleta e rejeitada");
        Files.writeString(produtosCsv, "X;2;Arroz;12;10;\n");
        verifica(lanca(IllegalStateException.class, repo::carregarProdutos), "tipo de produto desconhecido e rejeitado");
        Files.writeString(produtosCsv, "N;dois;Arroz;12;10;\n");
        verifica(lanca(IllegalStateException.class, repo::carregarProdutos), "numero invalido e rejeitado");
        Files.writeString(produtosCsv, "N;2;Arroz;-1;10;\n");
        verifica(lanca(IllegalStateException.class, repo::carregarProdutos), "quantidade negativa no arquivo e rejeitada");
        Files.writeString(produtosCsv, "P;1;Leite;5;5;\n");
        verifica(lanca(IllegalStateException.class, repo::carregarProdutos), "perecivel sem validade e rejeitado");
        Files.writeString(historicoCsv, "TROCA;1;5;2026-11-13T20:16:37\n");
        verifica(lanca(IllegalStateException.class, repo::carregarHistorico), "tipo de movimentacao desconhecido e rejeitado");

        System.out.println();
        if (falhas == 0) {
            System.out.println("TODOS OS TESTES DE PERSISTENCIA PASSARAM.");
        } else {
            System.out.println(falhas + " TESTE(S) FALHARAM.");
            System.exit(1);
        }
    }
}
