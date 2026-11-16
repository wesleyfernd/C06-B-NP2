# Controle de Estoque

Projeto final da NP2 de **C06 – Programação Orientada a Objetos** (Prof. Christopher Lima).
Sistema de console em Java para controlar produtos, registrar entradas e saídas, guardar histórico em arquivo e avisar quando o estoque precisa de atenção.

## Integrantes e partes

| Parte | Conteúdo | Integrante |
|---|---|---|
| 1 | Produtos e Estoque | Marcela Coelho |
| 2 | Movimentações | Wesley Fernandes |
| 3 | Persistência e Integração (`Main`) | Jussara |
| 4 | Monitoramento e Comunicação | Rafaela |

## Requisitos

Java 11 ou superior (testado com Java 21). Nenhuma biblioteca externa.

## Estrutura

```text
controle-estoque/
├── README.md
├── dados/                      arquivos csv usados pelo programa
│   ├── produtos.csv
│   └── historico.csv
├── docs/                       diagrama UML (png, svg e puml)
└── src/estoque/
    ├── Main.java               menu e integração das partes
    ├── excecoes/               EstoqueException e as 3 excecoes especificas
    ├── modelo/                 Produto (abstrata), ProdutoPerecivel, ProdutoNaoPerecivel, Estoque
    ├── movimentacoes/          Movimentacao (abstrata), Entrada, Saida, MovimentacaoService
    ├── persistencia/           RepositorioDados (interface), RepositorioArquivo
    ├── monitoramento/          Notificavel (interface), NotificadorConsole, MonitorEstoque
    └── testes/                 programas de teste (sem framework)
```

## Como executar

Sempre a partir da **pasta raiz do projeto** (onde fica a pasta `dados`).

Linux / macOS:

```bash
mkdir -p out
javac -encoding UTF-8 -d out $(find src -name "*.java")
java -cp out estoque.Main
```

Windows (PowerShell):

```powershell
mkdir out -Force
javac -encoding UTF-8 -d out (Get-ChildItem -Recurse src -Filter *.java).FullName
java -cp out estoque.Main
```

IntelliJ: abrir a pasta `controle-estoque` como projeto, marcar `src` como *Sources Root* e executar `estoque.Main`.

## Testes

```bash
java -cp out estoque.testes.TesteProdutosEstoque
java -cp out estoque.testes.TesteMovimentacoes
java -cp out estoque.testes.TesteMonitoramento
java -cp out estoque.testes.TesteRepositorioArquivo
```

Cada um termina com a mensagem `TODOS OS TESTES ... PASSARAM.` Os testes não alteram os arquivos da pasta `dados` (o de persistência usa arquivos temporários).

## Formato dos arquivos

`dados/produtos.csv` (separador `;`): `tipo;codigo;nome;quantidade;minimo;validade`, com tipo `P` (perecível, validade `AAAA-MM-DD`) ou `N` (não perecível, validade vazia).

```text
P;1;Leite;5;5;2026-12-20
N;2;Arroz;12;10;
```

`dados/historico.csv`: `tipo;codigo;quantidade;dataHora`, com tipo `ENTRADA` ou `SAIDA`. É criado e preenchido automaticamente.

## Regras do sistema

| Situação | Comportamento |
|---|---|
| Código duplicado | rejeitado (`ProdutoDuplicadoException`) |
| Produto inexistente | `ProdutoNaoEncontradoException` |
| Quantidade negativa no cadastro | rejeitada |
| Entrada ou saída com quantidade zero ou negativa | rejeitada |
| Saída maior que o saldo | `EstoqueInsuficienteException`, sem alterar o estoque |
| Quantidade abaixo do mínimo | alerta `ESTOQUE BAIXO` (e `PRODUTO ESGOTADO` quando zera) |
| Perecível vencido | alerta `PRODUTO VENCIDO` |
| Encerramento do programa | salva os produtos em `dados/produtos.csv` |

O projeto não usa threads.

## UML

Diagrama completo anexado na pasta docs.

## Uso de IA

| Parte | Utilizou IA? | Ferramenta         | Como foi usada                                                                                                                               |
|---|--------------|--------------------|----------------------------------------------------------------------------------------------------------------------------------------------|
| 1 – Produtos e Estoque | Sim  | ChatGPT       | Apoio na organização da estrutura das classes, compreensão e aplicação dos conceitos de POO, além de sugestões para melhorias e verificação do funcionamento do código                                                                                                                                 |
| 2 – Movimentações | Sim          | Claude (Anthropic) | desenho das classes, implementação, testes e documentação da parte.                                                                          |
| 3 – Persistência e Integração | Sim          | Claude + Chat GPT  | Auxiliado na integração das classes com a main, revisão e ajuda com os arquivos em formato csv;                                              |
| 4 – Monitoramento e Comunicação | Sim          | Claude IA          | Utilizado como apoio para esclarecer dúvidas sobre conteúdos, revisar alguns trechos do código, ajudar na identificação e correção de erros. |

Todo o código foi lido, executado e validado pelo grupo. 
