# Simulador de Algoritmo de Substituição de Páginas

Sistema desenvolvido para o Trabalho Prático 2 da discplina de Sistemas Operacionais.

------------------------------------------------------------------------

## Integrantes

-   Nome 1: Anthony Cesar de Carvalho Santos
-   Nome 2: Caio Santos Borges 
-   Nome 3: Sofia Fernandes Ferreira Silva

------------------------------------------------------------------------

## Descrição do que foi implementado

Este projeto implementa um simulador de algoritmos de substituição de
páginas, permitindo analisar o comportamento de diferentes estratégias de
gerenciamento de páginas na memôria.

O sistema lê um conjunto de páginas a partir de um arquivo de entrada
(`referencias.txt`) e executa os algoritmos selecionados pelo usuário via
interface de linha de comando (CLI).

------------------------------------------------------------------------

## Requisitos para execução

-   Java JDK 8 ou superior instalado\
-   Terminal ou IDE (IntelliJ, VS Code, Eclipse, etc.)

------------------------------------------------------------------------

## Estrutura esperada

    /src
     ├── App.java
     ├── Pagina.java
     ├── referencia.txt

------------------------------------------------------------------------

## Compilação

No terminal, dentro da pasta do projeto:

``` bash
javac App.java
```

Se houver múltiplos arquivos `.java`:

``` bash
javac *.java
```

------------------------------------------------------------------------

## Execução

Após compilar:

``` bash
java App
```

------------------------------------------------------------------------

## Arquivo de entrada utilizado

O arquivo `processos.txt` deve estar na raiz do projeto e seguir o
formato:

    Quadros Disponiveis;[Valor]
    Tempo Clock;[Valor em ms]
    ID Pagina;Tipo Acesso(R/W)

### Exemplo:

    Quadros Disponiveis;4
    Tempo Clock;10
    0;R
    1;W
    0;R
    2;W

-   **Quadros Disponiveis**: Quantidade de quadros disponíveis
-   **Tempo Clock**: tempo da chegada daquele processo
-   **ID Pagina**: Id da página recebida
-   **Tipo Acesso**: Tipo do acesso a pagina, sendo R para leitura e W para escrita

------------------------------------------------------------------------

## Algoritmos implementados

-   First In, First Out (FIFO)
-   Segunda Chance  
-   Algoritmo MY 

------------------------------------------------------------------------

## Saída

Após a execução do algoritmo de simulação selecionado pelo usuário, o sistema exibe:

-   Total de referências
-   Quantidade de Page Faults 
-   Taxa de Page Faults:

------------------------------------------------------------------------

## Observações

-   Certifique-se de que o arquivo `referencias.txt` está corretamente
    formatado.
- Certifique-se de que o arquivo `referencias.txt` está na raiz do projeto.
-   Para grandes quantidades de processos, recomenda-se salvar a saída
    em arquivo.

------------------------------------------------------------------------
## Descrição do Algoritmo MY – Semáforo

O algoritmo MY foi desenvolvido a partir da ideia do algoritmo Segunda Chance, porém utilizando três níveis de prioridade para representar a permanência de uma página na memória.

A ideia foi utilizar um sistema semelhante a um semáforo, onde cada cor representa o "estado de vida" da página e a ordem de remoção delas.

-   ***Verde***: página recém-utilizada ou recentemente acessada.
-   ***Amarela***: página que perdeu uma oportunidade de permanecer na memória.
-   ***Vermelha***: página que já recebeu todas as oportunidades e pode ser removida.

Sempre que uma página é acessada (HIT), ela retorna para a cor ***Verde***, indicando que continua sendo utilizada e deve permanecer na memória por mais tempo.

Esse algoritmo busca favorecer páginas frequentemente acessadas, evitando que sejam removidas logo após um único ciclo de substituição.

### Mecânica de funcionamento:

O algoritmo processa cada referência  vinda do arquivo da seguinte forma:

1. Verifica se a página já está carregada na memória.
2. Em caso de HIT, a página:
    -   ativa o bit de referência (R);
    -   ativa o bit de modificação (M) caso o acesso seja de escrita;
    -   retorna para a cor ***Verde*** caso esteja com outro estado.
3. Em caso de Page Fault:
    -   se houver espaço livre, a página é inserida diretamente com sua cor sendo ***Verde***;
    -   caso contrário, o algoritmo executa a de escolha da vítima.

    ##### A escolha da vítima segue a ordem:

-   Página Verde -> passa para Amarela;
-   Página Amarela -> passa para Vermelha;
-   Página Vermelha -> é removida da memória.

Caso nenhuma página esteja inicialmente com a cor ***Vermelho***, o algoritmo percorre novamente a memória até que alguma página chegue ao estado ***Vermelho***, garantindo que toda página receba duas oportunidades antes da remoção.

### Complexidade arquitetural

A estrutura utilizada consiste em uma lista contendo todas as páginas atualmente carregadas na memória.

Cada página armazena:

-   identificador da página;
-   bit de referência (R);
-   bit de modificação (M);
-   cor correspondente ao algoritmo MY, com os valores possíveis sendo ***Verde***, ***Amarelo*** e ***Vermelho***.

No pior caso, a escolha da vítima pode percorrer a memória até três vezes:

1. Verde -> Amarela;
2. Amarela -> Vermelha;
3. localização da primeira Vermelha.

Embora o algoritmo possa realizar mais de uma passagem sobre a lista, o número máximo de percursos é constante (até três).

A escolha da vítima possui complexidade linear **O(n)**. No pior caso, quando todas as páginas estão inicialmente no estado Verde, o algoritmo realiza duas passagens completas pela memória (***Verde*** -> ***Amarela*** e ***Amarela*** -> ***Vermelha***) e uma terceira passagem parcial, encerrada assim que encontra a primeira página Vermelha. Dessa forma, o custo máximo é de aproximadamente **2n + 1** operações, mantendo complexidade **O(n)**.

# Relatório de Simulação:

## 1. Cenário de Teste e Metodologia

Para a avaliação final dos algoritmos de substituição de páginas, o simulador processou uma cadeia estruturada de **30 referências à memória**. 

Este cenário de teste foi desenhado com um alto grau de **localidade de referência**, onde um pequeno conjunto de páginas (neste caso, as páginas `1` e `2`) é acessado de forma contínua e intercalada com páginas aleatórias, simulando o comportamento real do núcleo de execução de um processo. 

* **Configuração do ambiente:** 4 quadros de memória disponíveis.

---

## 2. Resultados Obtidos

| Posição | Algoritmo | Total de Referências | Page Faults | Taxa de Faltas |
| :---: | :--- | :---: | :---: | :---: |
| **1º** | Segunda Chance | 30 | 14 | 46,67% |
| **2º** | Algoritmo MY | 30 | 16 | 53,33% |
| **3º** | FIFO | 30 | 18 | 60,00% |

---

## 3. Análise Técnica e Justificativa

Os resultados comprovam a diferença de eficiência entre abordagens baseadas no tempo de carregamento e abordagens baseadas no histórico de uso das páginas:

### 📉 Baixo desempenho do FIFO (60,00%)
O algoritmo obteve a pior taxa de acertos porque adota uma política de substituição estritamente temporal ("cega" ao uso). Ao encher a memória, o FIFO removeu as páginas mais antigas (as de número `1` e `2`). Como essas páginas formavam o núcleo estrutural da carga de trabalho e continuavam sendo requisitadas logo em seguida, o sistema foi forçado a buscá-las no disco repetidas vezes, gerando um alto índice de *Page Faults* desnecessários.

### 🔄 Desempenho intermediário do Algoritmo MY (53,33%)
A solução própria do grupo demonstrou uma melhoria clara em relação ao FIFO. O mecanismo de rebaixamento gradual de prioridade (**Verde** $\rightarrow$ **Amarelo** $\rightarrow$ **Vermelho**) conseguiu proteger as páginas mais acessadas da remoção imediata. Isso garantiu que as páginas vitais sobrevivessem por mais ciclos na memória RAM, reduzindo a taxa de erro.

### 🏆 Superioridade do Segunda Chance (46,67%)
Este algoritmo apresentou o melhor desempenho para a carga de trabalho. A verificação do bit de referência ($R=1$) funcionou como um escudo perfeito para o padrão de localidade de referência do teste. As páginas `1` e `2`, por serem muito lidas/escritas, mantinham seus bits ativos. 

Quando a memória enchia e elas eram avaliadas para remoção, o sistema imediatamente as "perdoava" (zerando o bit) e as enviava para o final da fila. Isso expulsou de forma muito eficiente apenas as páginas "ruídos" (`3`, `4`, `5`, etc.), cravando a menor taxa de faltas da simulação.
