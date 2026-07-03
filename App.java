import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;


public class App {

    static String nomeArquivoDados;

    static Scanner teclado;

    static int quadrosDisponiveis;

    static int tempoClock;

    static List<String> referencias;

    static void limparTela() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    /** Gera um efeito de pausa na CLI. Espera por um enter para continuar */
    static void pausa() {
        System.out.println("Digite enter para continuar...");
        teclado.nextLine();
    }

    /** Cabeçalho principal da CLI do sistema */
    static void cabecalho() {
        System.out.println("===================================");
        System.out.println("SIMULAÇAO DE GERENCIA DE MEMORIA :D");
        System.out.println("===================================");
    }
   
    static <T extends Number> T lerOpcao(String mensagem, Class<T> classe) {
    	T valor;
        
    	System.out.println(mensagem);
    	try {
            valor = classe.getConstructor(String.class).newInstance(teclado.nextLine());
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException 
        		| InvocationTargetException | NoSuchMethodException | SecurityException e) {
            return null;
        }
        return valor;
    }
    
    /** Imprime o menu principal, lê a opção do usuário e a retorna (int).
     * @return Um inteiro com a opção do usuário.
     */
    static int menu() {
        cabecalho();
        System.out.println("1 - Algoritmo Segunda Chance");
        System.out.println("2 - Algoritmo escolhido");
        System.out.println("3 - Algoritmo MY");
        System.out.println("0 - Sair");
        System.out.print("Digite sua opçao: ");
        return Integer.parseInt(teclado.nextLine());
    }

	public static void main(String[] args) {
		
		teclado = new Scanner(System.in, Charset.forName("UTF-8"));
        
        nomeArquivoDados = "referencias.txt";

        lerArquivoReferencias(nomeArquivoDados);
        
        int opcao = -1;
      
        do{
            opcao = menu();
            switch (opcao) {
                case 1 -> algoritmoSegundaChance();
                case 2 -> algoritmoEscolhido();
                case 3 -> algoritmoMY();
            }
            pausa();
        }while(opcao != 0);       

        teclado.close();    
    }

    /**  Método para leitura do arquivo com os processos a serem utilizados.
     * @return Lista de processos.
     */
    public static void lerArquivoReferencias(String caminho) {

        Scanner arquivo = null;

        try {

            arquivo = new Scanner( new File(caminho), Charset.forName("UTF-8"));

            quadrosDisponiveis = Integer.parseInt(arquivo.nextLine().split(";")[1]);

            tempoClock = Integer.parseInt( arquivo.nextLine().split(";")[1]);

            referencias = new ArrayList<>();

            while (arquivo.hasNextLine()) {

                String linha = arquivo.nextLine().trim();

                if (!linha.isEmpty()) {
                    referencias.add(linha);
                }
            }

        } catch (IOException e) {

            referencias = null;

        } finally {

            if (arquivo != null)
                arquivo.close();
        }
    }

    private static Pagina buscarPagina(List<Pagina> memoria, int id) {

        for (Pagina pagina : memoria) {

            if (pagina.id == id) {
                return pagina;
            }
        }

        return null;
    }

    /**  Método para imprimir as métricas dos processos. É chamado ao final de um algoritmo de escalonamento pra imprimir as métricas dele.
     * @return Métricas de Tempo de Espera Médio, Tempo de Retorno (Turnaround ) Médio e Vazão (Throughput).
     */
    private static void imprimirMetricas(int pageFaults, String nome) {

        System.out.println("\n=== RESULTADO " + nome + " ===");

        System.out.println("Total de Referências: " + referencias.size());

        System.out.println("Page Faults: " + pageFaults);

        double taxa = (double) pageFaults / referencias.size();

        System.out.printf("Taxa de Page Faults: %.2f%%\n", taxa * 100);
    }

    /**  Algoritmo de escalonamento não-preemptivo simples por ordem de chegada.
     * @return Métricas de Tempo de Espera Médio, Tempo de Retorno (Turnaround ) Médio e Vazão (Throughput) .
     */
    private static void algoritmoSegundaChance() {

        //lista que representa a memoria principal
        List<Pagina> memoria = new ArrayList<>();
        //fila que representa a ordem das paginas, porem as paginas podem receber 2 chance
        Queue<Pagina> fila = new LinkedList<>();
        //numero que representa os page faults
        int pageFaults = 0;

        for (String referencia : referencias) {
            // Divide a linha no formato: ID;TipoAcesso
            String[] dados = referencia.split(";");
            //representa o id da pagina atual
            int idPagina = Integer.parseInt(dados[0]);
            //representa o tipo de acesso Read ou Write
            char acesso = dados[1].charAt(0);
            //verifica se a pagina ja esta na memoria
            Pagina pagina = buscarPagina(memoria, idPagina);
            // Caso a página já esteja na memória, ocorre um HIT
            if (pagina != null) {
                //marca que a página foi referenciada recentemente.
                pagina.R = true;
                //se o acesso for de escrita marca como recentemente modificada
                if (acesso == 'W') {
                    pagina.M = true;
                }
            } else {
                //pagina nao esta na memoria = pagefault
                pageFaults++;
                //se a memoria estiver cheia, remove uma pagina
                if (memoria.size() >= quadrosDisponiveis) {

                    while (true) {
                        //pega a pagina mais antiga da fila
                        Pagina candidata = fila.poll();
                        //se o bit R for true da uma segunda chance inserindo a pagina na fila novamente com o bit R zerado
                        if (candidata.R) {
                            candidata.R = false;
                            fila.add(candidata);
                        } else {
                            //se nao foi recentemente referenciada remove direto
                            memoria.remove(candidata);
                            break;
                        }
                    }
                }
                //cria uma nova pagina
                Pagina nova = new Pagina(idPagina);
                //toda a pagina recem criada foi recem referenciada
                nova.R = true;
                //se o acesso for de escrita marca como recem modificada
                if (acesso == 'W') {
                    nova.M = true;
                }
                //adiciona a pagina a memoria
                memoria.add(nova);
                //adiciona a pagina a fila
                fila.add(nova);
            }
        }

        imprimirMetricas(pageFaults, "SEGUNDA CHANCE");
    }
    

    /**  Variante preemptiva do SJF. O escalonador sempre escolhe o processo que possui o menor tempo de execução restante
     * @return Métricas de Tempo de Espera Médio, Tempo de Retorno (Turnaround ) Médio e Vazão (Throughput) .
     */
    private static void algoritmoEscolhido() { //FIFO

        //lista que representa a memoria principal
        List<Pagina> memoria = new ArrayList<>();
        //fila que representa a ordem das paginas
        Queue<Pagina> fila = new LinkedList<>();
        //numero de page faults
        int pageFaults = 0;
        //para cada referencia de pagina no arquivo
        for (String referencia : referencias) {
            //divide os dados 
            String[] dados = referencia.split(";");
            //id da pagina atual
            int idPagina = Integer.parseInt(dados[0]);
            //tipo de acesso
            char acesso = dados[1].charAt(0);
            //busca a pagina na memoria
            Pagina pagina = buscarPagina(memoria, idPagina);
            //se a pagina for encontrada da HIT
            if (pagina != null) {
                //altera o bit R de recentemente referenciada
                pagina.R = true;
                //se o acesso for de escrita altera o bit de recentemente modificada
                if (acesso == 'W') {
                    pagina.M = true;
                }
            } else {
                // se a pagina nao foi encontrada da page fault
                pageFaults++;
                //se nao tiver espaço na memoria remove a pagina mais antiga
                if (memoria.size() >= quadrosDisponiveis) {
                    Pagina removida = fila.poll();
                    memoria.remove(removida);
                }
                //cria nova pagina
                Pagina nova = new Pagina(idPagina);
                //coloca o bit de recentemente referenciada como true
                nova.R = true;
                //se o acesso for de escrita altera o bit de recentemente modificada tambem
                if (acesso == 'W') {
                    nova.M = true;
                }
                //adiciona a nova pagina na memoria
                memoria.add(nova);
                //adiciona a nova pagina na fila de paginas
                fila.add(nova);
            }
        }

        imprimirMetricas(pageFaults, "FIFO");
    }

    /** Algoritmo MY - Semáforo.
     * Utiliza três estados (Verde, Amarela e Vermelha) para definir a prioridade
     * de permanência das páginas na memória.
     * @return Métricas de Page Faults.
     */
    private static void algoritmoMY() {

        // Lista que representa a memória principal
        List<Pagina> memoria = new ArrayList<>();

        // Contador de Page Faults
        int pageFaults = 0;

        // Percorre todas as referências do arquivo
        for (String referencia : referencias) {

            // Divide a linha do arquivo
            String[] dados = referencia.split(";");

            // Obtém o identificador da página
            int idPagina = Integer.parseInt(dados[0]);

            // Obtém o tipo de acesso (R ou W)
            char acesso = dados[1].charAt(0);

            // Procura a página na memória
            Pagina pagina = buscarPagina(memoria, idPagina);

            // HIT: página já está na memória
            if (pagina != null) {

                // Marca o bit de referência
                pagina.R = true;

                // Caso seja escrita, ativa também o bit de modificação
                if (acesso == 'W') {
                    pagina.M = true;
                }

                // Sempre que a página é utilizada, ela volta para Verde
                pagina.cor = Cor.VERDE;
            }
            // PAGE FAULT: página não está carregada
            else {

                // Incrementa o contador de faltas de página
                pageFaults++;

                // Se a memória estiver cheia, escolhe uma vítima
                if (memoria.size() >= quadrosDisponiveis) {

                    int vitima = escolherVitimaMY(memoria);

                    // Remove a página escolhida
                    memoria.remove(vitima);
                }

                // Cria a nova página
                Pagina nova = new Pagina(idPagina);

                // Marca a página como referenciada
                nova.R = true;

                // Caso o acesso seja de escrita, marca também como modificada
                if (acesso == 'W') {
                    nova.M = true;
                }

                // Insere a nova página na memória
                memoria.add(nova);
            }
        }

        // Exibe as métricas obtidas
        imprimirMetricas(pageFaults, "ALGORITMO MY");
    }

    /** Método responsável por escolher qual página será removida da memória
     * seguindo a política do algoritmo MY.
     * Verde -> Amarela.
     * Amarela -> Vermelha.
     * Vermelha -> Removida.
     * @param memoria Lista de páginas atualmente na memória.
     * @return Índice da página escolhida para remoção.
     */
    private static int escolherVitimaMY(List<Pagina> memoria) {

        // Continua percorrendo até encontrar uma página Vermelha
        while (true) {

            // Percorre todas as páginas da memória
            for (int i = 0; i < memoria.size(); i++) {

                Pagina pagina = memoria.get(i);

                // Se encontrou uma Vermelha, ela será removida
                if (pagina.cor == Cor.VERMELHA) {
                    return i;
                }

                // Caso seja Amarela, passa para Vermelha
                if (pagina.cor == Cor.AMARELA) {
                    pagina.cor = Cor.VERMELHA;
                }

                // Caso seja Verde, passa para Amarela
                else {
                    pagina.cor = Cor.AMARELA;
                }
            }
        }
    }

       
}