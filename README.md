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