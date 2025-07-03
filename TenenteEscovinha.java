import java.util.concurrent.atomic.AtomicBoolean; // Para sinalizar o término de forma segura entre threads

/**
 * ]Representa a thread do Tenente Escovinha, o "observador" da simulação.
 * Sua função é monitorar o estado da barbearia periodicamente e, ao final da
 * simulação, gerar um relatório completo com todas as métricas de desempenho.
 *
 * @author Matheus Rocha
 * @author Guilherme Sahdo
 * @version 1.0
 */

public class TenenteEscovinha implements Runnable {
    private Barbearia barbearia;
    private AtomicBoolean encerrar = new AtomicBoolean(false); // Flag para sinalizar o término da thread
    private long inicioSimulacao; // Tempo de início da simulação para cálculo de duração
    
    /**
     * Constrói a thread do Tenente Escovinha.
     *
     * @param barbearia A instância compartilhada da Barbearia que ele irá monitorar.
     */
    public TenenteEscovinha(Barbearia barbearia) {
        this.barbearia = barbearia;
        this.inicioSimulacao = System.currentTimeMillis();
    }

    
    /**
     * O ciclo de vida principal da thread do Tenente.
     * Em um laço, ele dorme por 3 segundos, acorda para registrar dados e
     * imprimir um relatório parcial, até ser sinalizado para encerrar.
     */
    @Override
    public void run() {
        System.out.println("Tenente Escovinha iniciou o monitoramento."); // [cite: 19]
        while (!encerrar.get()) { // Continua monitorando até ser sinalizado para encerrar
            try {
                Thread.sleep(3000); // Verifica o estado da barbearia a cada 3 segundos

                // Registrar o comprimento atual das filas para o cálculo da média posterior
                barbearia.registrarComprimentosFila();

                // Exemplo de relatório parcial (pode ser mais detalhado)
                System.out.println("\n--- Relatório Parcial do Tenente Escovinha ---");
                System.out.printf("Cadeiras Ocupadas: %d / %d (%.2f%%)\n",
                        barbearia.getCadeirasOcupadas(), 20, barbearia.getOcupacaoPercentual()); // [cite: 22]
                System.out.printf("Ocupação por categoria: Oficiais %.2f%%, Sargentos %.2f%%, Cabos %.2f%%\n",
                        barbearia.getOcupacaoPercentualPorCategoria(Cliente.Categoria.OFICIAL), // [cite: 22]
                        barbearia.getOcupacaoPercentualPorCategoria(Cliente.Categoria.SARGENTO), // [cite: 22]
                        barbearia.getOcupacaoPercentualPorCategoria(Cliente.Categoria.CABO)); // [cite: 22]
                System.out.println("Tamanho atual das filas: Oficiais: " + barbearia.getTamanhoFila(Cliente.Categoria.OFICIAL) +
                                   ", Sargentos: " + barbearia.getTamanhoFila(Cliente.Categoria.SARGENTO) +
                                   ", Cabos: " + barbearia.getTamanhoFila(Cliente.Categoria.CABO)); // [cite: 23]
                System.out.println("----------------------------------------------");

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Tenente Escovinha interrompido.");
                break; // Sai do loop
            }
        }
        gerarRelatorioFinal(); // Gera o relatório final ao encerrar
    }

    /**
     * Sinaliza para o Tenente Escovinha que ele deve parar de monitorar e gerar o relatório final.
     */
    public void encerrar() {
        encerrar.set(true);
    }

    /**
     * Elabora e exibe o relatório final das atividades da barbearia.
     */
    private void gerarRelatorioFinal() {
        long duracaoSimulacaoMs = System.currentTimeMillis() - inicioSimulacao;
        double duracaoSimulacaoSeg = duracaoSimulacaoMs / 1000.0;

        System.out.println("\n\n*** Relatório Final das Atividades da Barbearia ***"); // [cite: 21]
        System.out.printf("Duração Total da Simulação: %.2f segundos\n\n", duracaoSimulacaoSeg);

        // 1. Estado de ocupação das cadeiras (% por categoria e livre)
        System.out.println("1. Estado de Ocupação das Cadeiras:");
        System.out.printf("   Total Ocupadas: %d (%.2f%%)\n", barbearia.getCadeirasOcupadas(), barbearia.getOcupacaoPercentual());
        System.out.printf("   Total Livres: %d (%.2f%%)\n", barbearia.getCadeirasLivres(), 100.0 - barbearia.getOcupacaoPercentual());
        System.out.printf("   Ocupação Média por Categoria: Oficiais %.2f%%, Sargentos %.2f%%, Cabos %.2f%%\n\n",
                barbearia.getOcupacaoPercentualPorCategoria(Cliente.Categoria.OFICIAL),
                barbearia.getOcupacaoPercentualPorCategoria(Cliente.Categoria.SARGENTO),
                barbearia.getOcupacaoPercentualPorCategoria(Cliente.Categoria.CABO));

        // 2. Comprimento médio das filas
        System.out.println("2. Comprimento Médio das Filas:");
        System.out.printf("   Oficiais: %.2f\n", barbearia.getComprimentoMedioFila(Cliente.Categoria.OFICIAL));
        System.out.printf("   Sargentos: %.2f\n", barbearia.getComprimentoMedioFila(Cliente.Categoria.SARGENTO));
        System.out.printf("   Cabos: %.2f\n\n", barbearia.getComprimentoMedioFila(Cliente.Categoria.CABO));

        // 3. Tempo médio de atendimento por categoria
        System.out.println("3. Tempo Médio de Atendimento por Categoria (segundos):");
        System.out.printf("   Oficiais: %.2f\n", barbearia.getTempoMedioAtendimento(Cliente.Categoria.OFICIAL));
        System.out.printf("   Sargentos: %.2f\n", barbearia.getTempoMedioAtendimento(Cliente.Categoria.SARGENTO));
        System.out.printf("   Cabos: %.2f\n\n", barbearia.getTempoMedioAtendimento(Cliente.Categoria.CABO));

        // 4. Tempo médio de espera por categoria
        System.out.println("4. Tempo Médio de Espera por Categoria (segundos):");
        System.out.printf("   Oficiais: %.2f\n", barbearia.getTempoMedioEspera(Cliente.Categoria.OFICIAL));
        System.out.printf("   Sargentos: %.2f\n", barbearia.getTempoMedioEspera(Cliente.Categoria.SARGENTO));
        System.out.printf("   Cabos: %.2f\n\n", barbearia.getTempoMedioEspera(Cliente.Categoria.CABO));

        // 5. Número de atendimentos por categoria
        System.out.println("5. Número de Atendimentos por Categoria:");
        System.out.printf("   Oficiais: %d\n", barbearia.getTotalAtendimentos(Cliente.Categoria.OFICIAL));
        System.out.printf("   Sargentos: %d\n", barbearia.getTotalAtendimentos(Cliente.Categoria.SARGENTO));
        System.out.printf("   Cabos: %d\n\n", barbearia.getTotalAtendimentos(Cliente.Categoria.CABO));

        // 6. Número total de clientes por categoria (oficiais, sargentos, cabos e pausa)
        System.out.println("6. Número Total de Clientes Gerados por Categoria:");
        System.out.printf("   Oficiais: %d\n", barbearia.getTotalClientesGerados(Cliente.Categoria.OFICIAL));
        System.out.printf("   Sargentos: %d\n", barbearia.getTotalClientesGerados(Cliente.Categoria.SARGENTO));
        System.out.printf("   Cabos: %d\n", barbearia.getTotalClientesGerados(Cliente.Categoria.CABO));
        System.out.printf("   Pausas: %d\n\n", barbearia.getTotalClientesGerados(Cliente.Categoria.PAUSA));

        System.out.println("**************************************************");
    }
}
