import java.util.Scanner;

/**
 * A classe principal que serve como ponto de entrada para a simulação da Barbearia.
 * É responsável por:
 * 1. Coletar a configuração inicial do usuário (Caso de teste, tempos de cochilo).
 * 2. Instanciar todos os objetos e threads (Barbearia, Sargento, Barbeiros, Tenente).
 * 3. Iniciar a simulação dando start() nas threads.
 * 4. Orquestrar o encerramento gracioso da simulação usando join().
 *
 * @author Matheus Rocha
 * @author Guilherme Sahdo
 * @version 1.0
 */
public class Main {
	
	/**
     * O método principal que executa a simulação.
     *
     * @param args Argumentos da linha de comando (não utilizados nesta versão interativa).
     * @throws InterruptedException Se a espera (join) por uma thread for interrompida.
     */
	
    public static void main(String[] args) throws InterruptedException {
        Scanner scanner = new Scanner(System.in);

        System.out.println("--- Barbearia do Recruta Zero ---"); 
        System.out.println("Configuração do Sargento Tainha:");
        System.out.print("Informe o período MÍNIMO de cochilo (em segundos, entre 1 e 5): "); 
        int minCochiloSeg = scanner.nextInt();
        if (minCochiloSeg < 1) minCochiloSeg = 1;
        System.out.print("Informe o período MÁXIMO de cochilo (em segundos, entre 1 e 5): "); 
        int maxCochiloSeg = scanner.nextInt();
        if (maxCochiloSeg > 5) maxCochiloSeg = 5;

        System.out.println("\nEscolha o Caso de Teste para a Barbearia:");
        System.out.println("A: Um barbeiro (Recruta Zero) atende as três filas com prioridade."); 
        System.out.println("B: Dois barbeiros (Recruta Zero e Dentinho) atendem as três filas com prioridade."); 
        System.out.println("C: Três barbeiros (Recruta Zero, Dentinho e Otto), cada um dedicado a uma fila, mas com fallback."); 
        System.out.print("Digite A, B ou C: ");
        String caso = scanner.next().toUpperCase();

        Barbearia barbearia = new Barbearia();

        // Cria e inicia a thread do Sargento Tainha
        SargentoTainha sargentoTainha = new SargentoTainha(barbearia, minCochiloSeg * 1000, maxCochiloSeg * 1000);
        Thread sargentoThread = new Thread(sargentoTainha, "SargentoTainha");

        // Cria as threads dos barbeiros, dependendo do caso
        Barbeiro recrutaZero = new Barbeiro("Recruta Zero", barbearia, caso);
        Thread recrutaZeroThread = new Thread(recrutaZero, "RecrutaZero");

        Barbeiro dentinho = null;
        Thread dentinhoThread = null;

        Barbeiro otto = null;
        Thread ottoThread = null;

        switch (caso) {
            case "A":
                System.out.println("\nExecutando Caso A: Um barbeiro.");
                break;
            case "B":
                System.out.println("\nExecutando Caso B: Dois barbeiros.");
                dentinho = new Barbeiro("Dentinho", barbearia, caso);
                dentinhoThread = new Thread(dentinho, "Dentinho");
                break;
            case "C":
                System.out.println("\nExecutando Caso C: Três barbeiros com dedicação e fallback.");
                dentinho = new Barbeiro("Dentinho", barbearia, caso);
                dentinhoThread = new Thread(dentinho, "Dentinho");
                otto = new Barbeiro("Otto", barbearia, caso);
                ottoThread = new Thread(otto, "Otto");
                break;
            default:
                System.out.println("Caso de teste inválido. Saindo.");
                scanner.close();
                return;
        }

        TenenteEscovinha tenenteEscovinha = new TenenteEscovinha(barbearia);
        Thread tenenteThread = new Thread(tenenteEscovinha, "TenenteEscovinha");

        sargentoThread.start();
        recrutaZeroThread.start();
        if (dentinhoThread != null) dentinhoThread.start();
        if (ottoThread != null) ottoThread.start();
        tenenteThread.start();

        sargentoThread.join();
        System.out.println("\nSargento Tainha finalizou a geração de clientes.");

        recrutaZero.encerrar();
        if (dentinho != null) dentinho.encerrar();
        if (otto != null) otto.encerrar();

        recrutaZeroThread.join();
        if (dentinhoThread != null) dentinhoThread.join();
        if (ottoThread != null) ottoThread.join();
        System.out.println("Todos os barbeiros terminaram seus atendimentos e a barbearia está vazia.");

        tenenteEscovinha.encerrar();
        tenenteThread.join();

        System.out.println("\nSimulação da Barbearia do Recruta Zero finalizada.");
        scanner.close();
    }
}
