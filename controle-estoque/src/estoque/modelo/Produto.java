package estoque.modelo;

import estoque.excecoes.EstoqueInsuficienteException;

// produto generico do estoque; so existe como perecivel ou nao perecivel
public abstract class Produto {

    private final int codigo;
    private final String nome;
    private int quantidade;
    private final int estoqueMinimo;

    public Produto(int codigo, String nome, int quantidade, int estoqueMinimo) {
        if (codigo <= 0) {
            throw new IllegalArgumentException("O codigo deve ser positivo.");
        }
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("O nome nao pode ser vazio.");
        }
        // o csv usa ';' como separador, entao o nome nao pode conte-lo
        if (nome.contains(";") || nome.contains("\n") || nome.contains("\r")) {
            throw new IllegalArgumentException("O nome nao pode conter ';' ou quebras de linha.");
        }
        if (quantidade < 0) {
            throw new IllegalArgumentException("A quantidade inicial nao pode ser negativa.");
        }
        if (estoqueMinimo < 0) {
            throw new IllegalArgumentException("O estoque minimo nao pode ser negativo.");
        }
        this.codigo = codigo;
        this.nome = nome.trim();
        this.quantidade = quantidade;
        this.estoqueMinimo = estoqueMinimo;
    }

    public final int getCodigo() {
        return codigo;
    }

    public final String getNome() {
        return nome;
    }

    public final int getQuantidade() {
        return quantidade;
    }

    public final int getEstoqueMinimo() {
        return estoqueMinimo;
    }

    // soma qtd ao saldo; rejeita qtd invalida e estouro do limite do int
    public final void aumentarQuantidade(int qtd) {
        validarQuantidadePositiva(qtd);
        if (qtd > Integer.MAX_VALUE - quantidade) {
            throw new IllegalArgumentException("Quantidade excede o limite permitido.");
        }
        quantidade += qtd;
    }

    // subtrai qtd do saldo; se qtd for maior que o saldo, lanca excecao sem alterar nada
    public final void diminuirQuantidade(int qtd) {
        validarQuantidadePositiva(qtd);
        if (qtd > quantidade) {
            throw new EstoqueInsuficienteException(
                    "Estoque insuficiente para o produto " + codigo
                    + ". Disponivel: " + quantidade + "; solicitado: " + qtd + ".");
        }
        quantidade -= qtd;
    }

    // alerta somente quando a quantidade fica abaixo do minimo (igual ao minimo nao alerta)
    public final boolean estaAbaixoDoMinimo() {
        return quantidade < estoqueMinimo;
    }

    private static void validarQuantidadePositiva(int qtd) {
        if (qtd <= 0) {
            throw new IllegalArgumentException("A quantidade deve ser maior que zero.");
        }
    }

    // true se o produto esta em condicao de uso; cada subtipo define a regra
    public abstract boolean verificarCondicao();
}
