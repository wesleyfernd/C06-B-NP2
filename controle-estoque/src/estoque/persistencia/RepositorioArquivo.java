package estoque.persistencia;

import estoque.modelo.Produto;
import estoque.modelo.ProdutoNaoPerecivel;
import estoque.modelo.ProdutoPerecivel;
import estoque.movimentacoes.Entrada;
import estoque.movimentacoes.Movimentacao;
import estoque.movimentacoes.Saida;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// grava e le os dados em arquivos csv separados por ';'
//   produtos.csv : tipo;codigo;nome;quantidade;minimo;validade   (tipo p = perecivel, n = nao perecivel)
//   historico.csv: tipo;codigo;quantidade;data e hora iso      (tipo entrada ou saida)
// erro de leitura ou gravacao vira uncheckedioexception; conteudo invalido vira illegalstateexception
public class RepositorioArquivo implements RepositorioDados {

    private static final String SEPARADOR = ";";
    private static final String TIPO_PERECIVEL = "P";
    private static final String TIPO_NAO_PERECIVEL = "N";
    private static final String TIPO_ENTRADA = "ENTRADA";
    private static final String TIPO_SAIDA = "SAIDA";

    private final Path arquivoProdutos;
    private final Path arquivoHistorico;

    // caminhos relativos a pasta onde o programa e executado (raiz do projeto)
    public RepositorioArquivo() {
        this(Paths.get("dados", "produtos.csv"), Paths.get("dados", "historico.csv"));
    }

    public RepositorioArquivo(Path arquivoProdutos, Path arquivoHistorico) {
        this.arquivoProdutos = Objects.requireNonNull(arquivoProdutos, "O arquivo de produtos nao pode ser nulo.");
        this.arquivoHistorico = Objects.requireNonNull(arquivoHistorico, "O arquivo de historico nao pode ser nulo.");
    }

    // produtos.csv e obrigatorio: se nao existir, a leitura falha
    @Override
    public List<Produto> carregarProdutos() {
        List<Produto> produtos = new ArrayList<>();
        List<String> linhas = lerLinhas(arquivoProdutos);
        for (int i = 0; i < linhas.size(); i++) {
            if (!linhas.get(i).isBlank()) {
                produtos.add(converterProduto(linhas.get(i), i + 1));
            }
        }
        return produtos;
    }

    // reescreve o arquivo inteiro com o estado atual dos produtos
    @Override
    public void salvarProdutos(List<Produto> produtos) {
        StringBuilder conteudo = new StringBuilder();
        for (Produto produto : produtos) {
            String tipo = TIPO_NAO_PERECIVEL;
            String validade = "";
            if (produto instanceof ProdutoPerecivel) {
                tipo = TIPO_PERECIVEL;
                validade = ((ProdutoPerecivel) produto).getDataValidade().toString();
            }
            conteudo.append(tipo).append(SEPARADOR)
                    .append(produto.getCodigo()).append(SEPARADOR)
                    .append(produto.getNome()).append(SEPARADOR)
                    .append(produto.getQuantidade()).append(SEPARADOR)
                    .append(produto.getEstoqueMinimo()).append(SEPARADOR)
                    .append(validade).append("\n");
        }
        escrever(arquivoProdutos, conteudo.toString(), StandardOpenOption.TRUNCATE_EXISTING);
    }

    // acrescenta uma linha ao final do historico, sem reescrever o arquivo
    @Override
    public void registrarMovimentacao(Movimentacao m) {
        String linha = m.getTipo() + SEPARADOR
                + m.getCodigoProduto() + SEPARADOR
                + m.getQuantidade() + SEPARADOR
                + m.getDataHora() + "\n";
        escrever(arquivoHistorico, linha, StandardOpenOption.APPEND);
    }

    // historico.csv pode nao existir ainda: nesse caso devolve lista vazia
    @Override
    public List<Movimentacao> carregarHistorico() {
        List<Movimentacao> historico = new ArrayList<>();
        if (!Files.exists(arquivoHistorico)) {
            return historico;
        }
        List<String> linhas = lerLinhas(arquivoHistorico);
        for (int i = 0; i < linhas.size(); i++) {
            if (!linhas.get(i).isBlank()) {
                historico.add(converterMovimentacao(linhas.get(i), i + 1));
            }
        }
        return historico;
    }

    private Produto converterProduto(String linha, int numeroLinha) {
        String[] campos = linha.split(SEPARADOR, -1);
        if (campos.length < 5) {
            throw linhaInvalida(arquivoProdutos, numeroLinha, "campos insuficientes");
        }
        try {
            String tipo = campos[0].trim();
            int codigo = Integer.parseInt(campos[1].trim());
            String nome = campos[2];
            int quantidade = Integer.parseInt(campos[3].trim());
            int minimo = Integer.parseInt(campos[4].trim());

            if (tipo.equals(TIPO_PERECIVEL)) {
                if (campos.length < 6) {
                    throw linhaInvalida(arquivoProdutos, numeroLinha, "perecivel sem data de validade");
                }
                return new ProdutoPerecivel(codigo, nome, quantidade, minimo,
                        LocalDate.parse(campos[5].trim()));
            }
            if (tipo.equals(TIPO_NAO_PERECIVEL)) {
                return new ProdutoNaoPerecivel(codigo, nome, quantidade, minimo);
            }
            throw linhaInvalida(arquivoProdutos, numeroLinha, "tipo desconhecido '" + tipo + "'");
        } catch (IllegalArgumentException | DateTimeParseException e) {
            // numero ou data mal formatados e dados recusados pelo construtor do produto
            throw linhaInvalida(arquivoProdutos, numeroLinha, e.getMessage());
        }
    }

    private Movimentacao converterMovimentacao(String linha, int numeroLinha) {
        String[] campos = linha.split(SEPARADOR, -1);
        if (campos.length < 4) {
            throw linhaInvalida(arquivoHistorico, numeroLinha, "campos insuficientes");
        }
        try {
            String tipo = campos[0].trim();
            int codigo = Integer.parseInt(campos[1].trim());
            int quantidade = Integer.parseInt(campos[2].trim());
            LocalDateTime dataHora = LocalDateTime.parse(campos[3].trim());

            if (tipo.equals(TIPO_ENTRADA)) {
                return new Entrada(codigo, quantidade, dataHora);
            }
            if (tipo.equals(TIPO_SAIDA)) {
                return new Saida(codigo, quantidade, dataHora);
            }
            throw linhaInvalida(arquivoHistorico, numeroLinha, "tipo desconhecido '" + tipo + "'");
        } catch (IllegalArgumentException | DateTimeParseException e) {
            throw linhaInvalida(arquivoHistorico, numeroLinha, e.getMessage());
        }
    }

    private static IllegalStateException linhaInvalida(Path arquivo, int numeroLinha, String motivo) {
        return new IllegalStateException("arquivo " + arquivo + ", linha " + numeroLinha + ": " + motivo);
    }

    private static List<String> lerLinhas(Path arquivo) {
        try {
            return Files.readAllLines(arquivo);
        } catch (IOException e) {
            throw new UncheckedIOException("nao foi possivel ler " + arquivo, e);
        }
    }

    // cria a pasta e o arquivo se preciso; 'modo' define se acrescenta ou substitui o conteudo
    private static void escrever(Path arquivo, String texto, StandardOpenOption modo) {
        try {
            if (arquivo.getParent() != null) {
                Files.createDirectories(arquivo.getParent());
            }
            Files.writeString(arquivo, texto, StandardOpenOption.CREATE, StandardOpenOption.WRITE, modo);
        } catch (IOException e) {
            throw new UncheckedIOException("nao foi possivel gravar " + arquivo, e);
        }
    }
}
