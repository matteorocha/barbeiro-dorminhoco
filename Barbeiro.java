/**
 * Representa a thread de um Barbeiro, o "consumidor" de clientes. 
 * Ele retira clientes da barbearia, simula o atendimento (dormindo pelo tempo
 * de serviço do cliente) e registra as estatísticas de atendimento e espera.
 *
 * @author Matheus Rocha
 * @author Guilherme Sahdo
 * @version 1.0
 */
public class Barbeiro implements Runnable {
    private String nome;
    private Barbearia barbearia;
    private volatile boolean encerrar = false; // Flag para sinalizar o término da thread
    private String casoAtual; // Para adaptar o comportamento de acordo com o caso (A, B, C)
    
    
    /**
     * Constrói a thread de um Barbeiro.
     *
     * @param nome O nome do barbeiro (ex: "Recruta Zero").
     * @param barbearia A instância compartilhada da Barbearia.
     * @param casoAtual A letra do caso de teste ('A', 'B' ou 'C') que define seu comportamento.
     */
    public Barbeiro(String nome, Barbearia barbearia, String casoAtual) {
        this.nome = nome;
        this.barbearia = barbearia;
        this.casoAtual = casoAtual;
    }
    
    /**
     * O ciclo de vida principal da thread do Barbeiro.
     * Continua pegando e atendendo clientes até que seja sinalizado para encerrar
     * e não haja mais clientes na barbearia.
     */
    @Override
    public void run() {
        System.out.println(nome + " começou a trabalhar.");
        // O barbeiro só deve parar quando for sinalizado para encerrar E a barbearia estiver vazia
        while (!encerrar || !barbearia.isBarbeariaVazia()) {
            Cliente cliente = null;
            try {
                if (casoAtual.equals("A") || casoAtual.equals("B")) {
                   // Casos A e B: Barbeiro atende a fila com prioridade global
                    cliente = barbearia.pegarProximoCliente();
                } else if (casoAtual.equals("C")) {
                    // Caso C: Barbeiro dedicado a uma fila, mas pode atender outras se a sua estiver vazia
                    Cliente.Categoria categoriaPreferida;

                    // Define a categoria preferida de cada barbeiro no Caso C
                    if (nome.equals("Recruta Zero")) {
                        categoriaPreferida = Cliente.Categoria.OFICIAL;
                    } else if (nome.equals("Dentinho")) {
                        categoriaPreferida = Cliente.Categoria.SARGENTO;
                    } else { // Otto
                        categoriaPreferida = Cliente.Categoria.CABO;
                    }
                    cliente = barbearia.pegarCliente(categoriaPreferida);
                }

                if (cliente != null) {
                    // Cálculo do tempo de espera
                    long tempoEspera = System.currentTimeMillis() - cliente.getTempoChegada();
                    barbearia.registrarTempoEspera(cliente.getCategoria(), tempoEspera);

                    System.out.println(nome + " está atendendo um " + cliente.getCategoria() + ". Tempo de serviço: " + cliente.getTempoServico() + "s. Fila Of: " + barbearia.getTamanhoFila(Cliente.Categoria.OFICIAL) + " Sgt: " + barbearia.getTamanhoFila(Cliente.Categoria.SARGENTO) + " Cabo: " + barbearia.getTamanhoFila(Cliente.Categoria.CABO));
                    Thread.sleep(cliente.getTempoServico() * 1000); // Simula o tempo de corte

                    // Registro do tempo de atendimento
                    barbearia.registrarTempoAtendimento(cliente.getCategoria(), cliente.getTempoServico() * 1000); // Guardar em ms

                    System.out.println(nome + " terminou de atender o " + cliente.getCategoria() + ".");
                } else {
                    // Se não há clientes para atender no momento, o barbeiro espera um pouco para não consumir CPU em excesso
                	if (encerrar && barbearia.isBarbeariaVazia()) {
                        break;
                    }
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println(nome + " interrompido.");
                break; // Sai do loop
            }
        }
        System.out.println(nome + " terminou de trabalhar.");
    }

    /**
     * Sinaliza para o barbeiro que ele deve parar de aceitar novos clientes.
     * Ele deve, no entanto, terminar de atender todos que já estão na barbearia.
     */
    public void encerrar() {
        this.encerrar = true;
    }
}
