import java.util.LinkedList; // Para implementar as filas FIFO [cite: 14]
import java.util.Queue;
import java.util.concurrent.Semaphore; // Para controle de acesso concorrente [cite: 8]
import java.util.concurrent.atomic.AtomicInteger; // Para contadores atômicos (seguro para threads)
import java.util.ArrayList; // Para armazenar os tempos para cálculo de médias
import java.util.List;

/**
 * Representa o recurso compartilhado da Barbearia.
 * Esta classe gerencia as filas de espera dos clientes, controla o acesso concorrente
 * utilizando semáforos e é responsável por coletar todas as estatísticas da simulação.
 * É o núcleo de sincronização do projeto.
 *
 * @author Matheus Rocha
 * @author Guilherme Sahdo
 * @version 1.0
 */
public class Barbearia {
    private final int CAPACIDADE_TOTAL = 20; // Total de cadeiras na barbearia [cite: 14]

    /**
     * Retorna o tamanho da fila de uma categoria específica.
     * @param categoria A categoria da fila.
     * @return O número de clientes na fila daquela categoria.
     */
    public int getTamanhoFila(Cliente.Categoria categoria) {
        // Não precisamos de mutex aqui porque os métodos size() de LinkedList são thread-safe para leitura,
        // mas se houvesse risco de uma operação de escrita acontecer ao mesmo tempo que a leitura
        // do Tenente Escovinha, seria bom usar o mutex. Para simples leitura de tamanho, geralmente é ok.
        // Se ocorrerem inconsistências no relatório, pode-se adicionar mutex.
        switch (categoria) {
            case OFICIAL:
                return filaOficiais.size();
            case SARGENTO:
                return filaSargentos.size();
            case CABO:
                return filaCabos.size();
            default:
                return 0; // Para categoria PAUSA ou desconhecida
        }
    }
    
    private volatile boolean sargentoDispensado = false;
    
    // Filas FIFO para cada categoria de cliente [cite: 14]
    private Queue<Cliente> filaOficiais = new LinkedList<>();
    private Queue<Cliente> filaSargentos = new LinkedList<>();
    private Queue<Cliente> filaCabos = new LinkedList<>();
    

    private Semaphore cadeirasLivres = new Semaphore(CAPACIDADE_TOTAL);
    private Semaphore cadeirasOcupadas = new Semaphore(0);
    private Semaphore mutex = new Semaphore(1);          // Garante exclusão mútua ao acessar as filas compartilhadas [cite: 8] 

    // Variáveis para coleta de estatísticas (para o relatório do Tenente Escovinha) [cite: 21, 22, 23, 24, 25, 29, 30]
    private AtomicInteger totalAtendimentosOficiais = new AtomicInteger(0);
    private AtomicInteger totalAtendimentosSargentos = new AtomicInteger(0);
    private AtomicInteger totalAtendimentosCabos = new AtomicInteger(0);

    private AtomicInteger totalClientesOficiaisGerados = new AtomicInteger(0);
    private AtomicInteger totalClientesSargentosGerados = new AtomicInteger(0);
    private AtomicInteger totalClientesCabosGerados = new AtomicInteger(0);
    private AtomicInteger totalClientesPausaGerados = new AtomicInteger(0);

    // Listas para armazenar tempos e comprimentos de fila para cálculo de médias
    private List<Long> temposEsperaOficiais;
    private List<Long> temposEsperaSargentos;
    private List<Long> temposEsperaCabos;

    private List<Long> temposAtendimentoOficiais;
    private List<Long> temposAtendimentoSargentos;
    private List<Long> temposAtendimentoCabos;

    private List<Integer> comprimentosMediosFilaOficiais;
    private List<Integer> comprimentosMediosFilaSargentos;
    private List<Integer> comprimentosMediosFilaCabos;

    
    /**
     * Constrói e inicializa a Barbearia.
     * Configura as filas, os semáforos para controle de capacidade e
     * as estruturas de dados para coleta de estatísticas.
     */
    public Barbearia() {
        filaOficiais = new LinkedList<>();
        filaSargentos = new LinkedList<>();
        filaCabos = new LinkedList<>();

        // Inicialização dos semáforos
        cadeirasLivres = new Semaphore(CAPACIDADE_TOTAL); // Todas as cadeiras estão livres inicialmente
        cadeirasOcupadas = new Semaphore(0);              // Nenhuma cadeira está ocupada inicialmente
        mutex = new Semaphore(1);                         // Apenas 1 thread pode acessar as filas por vez

        // Inicialização das variáveis de estatísticas
        totalAtendimentosOficiais = new AtomicInteger(0);
        totalAtendimentosSargentos = new AtomicInteger(0);
        totalAtendimentosCabos = new AtomicInteger(0);

        totalClientesOficiaisGerados = new AtomicInteger(0);
        totalClientesSargentosGerados = new AtomicInteger(0);
        totalClientesCabosGerados = new AtomicInteger(0);
        totalClientesPausaGerados = new AtomicInteger(0);

        temposEsperaOficiais = new ArrayList<>();
        temposEsperaSargentos = new ArrayList<>();
        temposEsperaCabos = new ArrayList<>();

        temposAtendimentoOficiais = new ArrayList<>();
        temposAtendimentoSargentos = new ArrayList<>();
        temposAtendimentoCabos = new ArrayList<>();

        comprimentosMediosFilaOficiais = new ArrayList<>();
        comprimentosMediosFilaSargentos = new ArrayList<>();
        comprimentosMediosFilaCabos = new ArrayList<>();
    }

    /**
     * Tenta adicionar um cliente à sua respectiva fila de espera.
     * Este método é chamado pelo SargentoTainha. Ele utiliza um tryAcquire() não-bloqueante,
     * o que significa que se a barbearia estiver cheia, o cliente é imediatamente rejeitado.
     *
     * @param cliente O cliente a ser adicionado.
     * @return true se o cliente conseguiu um lugar na fila, false caso contrário.
     * @throws InterruptedException se a thread for interrompida.
     */
 
    public boolean adicionarCliente(Cliente cliente) throws InterruptedException {
        // ... (contagem de clientes gerados por categoria, antes do acquire)

        // Tenta adquirir uma cadeira livre imediatamente. Se não conseguir, retorna false (manda embora).
        if (!cadeirasLivres.tryAcquire()) { // ATENÇÃO AQUI: tryAcquire() em vez de acquire()
            return false; // Cliente é "mandado embora" porque não há cadeira
        }

        mutex.acquire(); // Entra na seção crítica

        // Adiciona o cliente à fila apropriada
        switch (cliente.getCategoria()) {
            case OFICIAL:
                filaOficiais.add(cliente);
                break;
            case SARGENTO:
                filaSargentos.add(cliente);
                break;
            case CABO:
                filaCabos.add(cliente);
                break;
            default:
                break;
        }

        mutex.release(); // Sai da seção crítica

        cadeirasOcupadas.release(); // Sinaliza que há um cliente esperando para ser atendido
        return true;
    }

    /**
     * Retira o próximo cliente da fila para ser atendido, respeitando a ordem de prioridade
     * global (Oficiais > Sargentos > Cabos)[cite: 18].
     * Este método é bloqueante: se não houver clientes, a thread do barbeiro esperará.
     * Usado nos Casos A e B. [cite: 44, 46]
     *
     * @return O próximo cliente a ser atendido, ou null se a simulação estiver terminando.
     * @throws InterruptedException se a thread for interrompida.
     */
    public Cliente pegarProximoCliente() throws InterruptedException {
        cadeirasOcupadas.acquire();
        
        if (isSargentoDispensado() && isBarbeariaVazia()) {
            cadeirasOcupadas.release(); // Devolve o permit para outro barbeiro poder sair também
            return null;
        }

        mutex.acquire();            // Entra na seção crítica para acessar as filas

        Cliente cliente = null;
        // Atende por prioridade: Oficiais > Sargentos > Cabos [cite: 18]
        if (!filaOficiais.isEmpty()) {
            cliente = filaOficiais.poll();
            totalAtendimentosOficiais.incrementAndGet();
        } else if (!filaSargentos.isEmpty()) {
            cliente = filaSargentos.poll();
            totalAtendimentosSargentos.incrementAndGet();
        } else if (!filaCabos.isEmpty()) {
            cliente = filaCabos.poll();
            totalAtendimentosCabos.incrementAndGet();
        }

        mutex.release();            // Sai da seção crítica

        cadeirasLivres.release(); // Libera uma cadeira, pois o cliente está saindo da fila para ser atendido
        return cliente;
    }

    /**
     * Retira um cliente para ser atendido, com uma lógica específica para o Caso C.
     * O barbeiro tenta primeiro pegar um cliente de sua fila preferida. Se estiver vazia,
     * [cite_start]ele atende outras filas, respeitando a prioridade global. [cite: 48]
     *
     * @param categoriaPreferida A categoria que o barbeiro tem preferência em atender.
     * @return O cliente a ser atendido, ou null se não houver clientes.
     * @throws InterruptedException se a thread for interrompida.
     */
    public Cliente pegarCliente(Cliente.Categoria categoriaPreferida) throws InterruptedException {
        // NOTA: Para o Caso C, a aquisição/liberação dos semáforos é mais complexa aqui.
        // O barbeiro deve primeiro tentar sua fila preferida SEM bloquear o semáforo 'cadeirasOcupadas'
        // para evitar deadlocks se sua fila estiver vazia mas outras tiverem clientes.
        // Uma abordagem simplificada: adquirir 'cadeirasOcupadas' e depois aplicar a lógica de prioridade.
        // Se após adquirir 'cadeirasOcupadas' e 'mutex' não houver cliente na fila preferida,
        // ele tenta outras filas. Se não encontra NENHUM cliente, precisa liberar o permit de 'cadeirasOcupadas'.

        // Adquirir cadeirasOcupadas ANTES de entrar na seção crítica para garantir que há pelo menos 1 cliente
        // em alguma fila. Isso evita que o barbeiro entre no mutex, encontre suas filas vazias, e saia sem liberar
        // um permit que outra thread poderia usar.
        cadeirasOcupadas.acquire();

        mutex.acquire(); // Entra na seção crítica

        Cliente cliente = null;

        // Tenta a categoria preferida primeiro
        switch (categoriaPreferida) {
            case OFICIAL:
                if (!filaOficiais.isEmpty()) {
                    cliente = filaOficiais.poll();
                    totalAtendimentosOficiais.incrementAndGet();
                }
                break;
            case SARGENTO:
                if (!filaSargentos.isEmpty()) {
                    cliente = filaSargentos.poll();
                    totalAtendimentosSargentos.incrementAndGet();
                }
                break;
            case CABO:
                if (!filaCabos.isEmpty()) {
                    cliente = filaCabos.poll();
                    totalAtendimentosCabos.incrementAndGet();
                }
                break;
            default:
                break; // Não deveria acontecer
        }

        // Se a categoria preferida estava vazia, tenta outras categorias por prioridade global
        if (cliente == null) {
            if (!filaOficiais.isEmpty()) {
                cliente = filaOficiais.poll();
                totalAtendimentosOficiais.incrementAndGet();
            } else if (!filaSargentos.isEmpty()) {
                cliente = filaSargentos.poll();
                totalAtendimentosSargentos.incrementAndGet();
            } else if (!filaCabos.isEmpty()) {
                cliente = filaCabos.poll();
                totalAtendimentosCabos.incrementAndGet();
            }
        }

        mutex.release(); // Sai da seção crítica

        // Se um cliente foi encontrado, libera uma cadeira para o próximo cliente entrar
        if (cliente != null) {
            cadeirasLivres.release();
        } else {
            // Isso não deve acontecer se cadeirasOcupadas.acquire() foi bem-sucedido e as filas foram verificadas corretamente,
            // mas é um fallback para garantir que o permit é liberado se nenhum cliente for encontrado.
            // Poderia indicar um problema lógico ou de timing.
            cadeirasOcupadas.release(); // Libera o permit adquirido se nenhum cliente foi encontrado.
        }

        return cliente;
    }


    // Métodos para o Tenente Escovinha coletar dados para o relatório [cite: 21]

    /**
     * Retorna o número atual de cadeiras ocupadas por clientes na barbearia.
     */
    public int getCadeirasOcupadas() {
        // A soma dos tamanhos das filas representa os clientes esperando nas cadeiras
        return filaOficiais.size() + filaSargentos.size() + filaCabos.size();
    }

    /**
     * Retorna o número atual de cadeiras livres.
     */
    public int getCadeirasLivres() {
        return CAPACIDADE_TOTAL - getCadeirasOcupadas();
    }

    /**
     * Retorna verdadeiro se todas as filas estiverem vazias. Usado pelos barbeiros para saber quando parar.
     */
    public boolean isBarbeariaVazia() {
        // Verifica se todas as filas estão vazias
        return filaOficiais.isEmpty() && filaSargentos.isEmpty() && filaCabos.isEmpty();
    }

    /**
     * Registra o comprimento atual das filas para cálculo de média.
     * Chamado periodicamente pelo Tenente Escovinha.
     */
    public void registrarComprimentosFila() throws InterruptedException {
        mutex.acquire(); // Garante acesso exclusivo às filas para leitura dos tamanhos
        comprimentosMediosFilaOficiais.add(filaOficiais.size());
        comprimentosMediosFilaSargentos.add(filaSargentos.size());
        comprimentosMediosFilaCabos.add(filaCabos.size());
        mutex.release();
    }

    /**
     * Registra o tempo de espera de um cliente para o cálculo da média no relatório final. [cite: 25]
     * Este método é thread-safe.
     *
     * @param categoria A categoria do cliente atendido.
     * @param tempo O tempo de espera em milissegundos.
     * @throws InterruptedException se a thread for interrompida ao adquirir o lock.
     */
    public void registrarTempoEspera(Cliente.Categoria categoria, long tempo) throws InterruptedException {
        mutex.acquire(); // Garante acesso exclusivo à lista
        try {
            switch (categoria) {
                case OFICIAL:
                    temposEsperaOficiais.add(tempo);
                    break;
                case SARGENTO:
                    temposEsperaSargentos.add(tempo);
                    break;
                case CABO:
                    temposEsperaCabos.add(tempo);
                    break;
                default:
                    break;
            }
        } finally {
            mutex.release(); // Libera o acesso
        }
    }

    /**
     * Registra o tempo de atendimento de um cliente.
     */
    public void registrarTempoAtendimento(Cliente.Categoria categoria, long tempo) throws InterruptedException {
        mutex.acquire(); // Garante acesso exclusivo à lista
        try {
            switch (categoria) {
                case OFICIAL:
                    temposAtendimentoOficiais.add(tempo);

                    break;
                case SARGENTO:
                    temposAtendimentoSargentos.add(tempo);
                    break;
                case CABO:
                    temposAtendimentoCabos.add(tempo);
                    break;
                default:
                    break;
            }
        } finally {
            mutex.release(); // Libera o acesso
        }
    }

    
    /**
     * Métodos para calcular as médias para o relatório final
     */
    public double getOcupacaoPercentual() {
        return (double) getCadeirasOcupadas() / CAPACIDADE_TOTAL * 100;
    }
    
  

    /**
     * Calcula o percentual de ocupação de uma categoria específica em relação à capacidade TOTAL da barbearia.
     *
     * @param categoria A categoria de cliente a ser medida.
     * @return A porcentagem de ocupação (de 0.0 a 100.0) que esta categoria representa do total de cadeiras.
     */
    public double getOcupacaoPercentualPorCategoria(Cliente.Categoria categoria) {
        int clientesNestaCategoria = 0;
        switch (categoria) {
            case OFICIAL:
                clientesNestaCategoria = filaOficiais.size();
                break;
            case SARGENTO:
                clientesNestaCategoria = filaSargentos.size();
                break;
            case CABO:
                clientesNestaCategoria = filaCabos.size();
                break;
            default:
                return 0;
        }
        return (double) clientesNestaCategoria / CAPACIDADE_TOTAL * 100;
    }

    /**
     * Calcula o comprimento médio de uma fila específica ao longo de toda a simulação.
     * Utiliza os registros coletados periodicamente pelo Tenente Escovinha.
     *
     * @param categoria A categoria da fila para o cálculo da média.
     * @return O comprimento médio da fila como um valor double.
     */
    public double getComprimentoMedioFila(Cliente.Categoria categoria) {
        List<Integer> listaParaMedia = null;
        switch (categoria) {
            case OFICIAL:
                listaParaMedia = comprimentosMediosFilaOficiais;
                break;
            case SARGENTO:
                listaParaMedia = comprimentosMediosFilaSargentos;
                break;
            case CABO:
                listaParaMedia = comprimentosMediosFilaCabos;
                break;
            default:
                return 0;
        }

        if (listaParaMedia.isEmpty()) return 0.0;
        return listaParaMedia.stream().mapToInt(Integer::intValue).average().orElse(0.0);
    }

    /**
     * Calcula o tempo médio de atendimento (corte de cabelo) para uma categoria de cliente.
     *
     * @param categoria A categoria de cliente para o cálculo.
     * @return O tempo médio de atendimento em segundos.
     */
    public double getTempoMedioAtendimento(Cliente.Categoria categoria) {
        List<Long> listaParaMedia = null;
        switch (categoria) {
            case OFICIAL:
                listaParaMedia = temposAtendimentoOficiais;
                break;
            case SARGENTO:
                listaParaMedia = temposAtendimentoSargentos;
                break;
                case CABO:
                listaParaMedia = temposAtendimentoCabos;
                break;
            default:
                return 0;
        }

        if (listaParaMedia.isEmpty()) return 0.0;
        return listaParaMedia.stream().mapToLong(Long::longValue).average().orElse(0.0) / 1000.0; // Em segundos
    }

    /**
     * Calcula o tempo médio de espera na fila para uma categoria de cliente.
     *
     * @param categoria A categoria de cliente para o cálculo.
     * @return O tempo médio de espera em segundos.
     */
    public double getTempoMedioEspera(Cliente.Categoria categoria) {
        List<Long> listaParaMedia = null;
        switch (categoria) {
            case OFICIAL:
               listaParaMedia = temposEsperaOficiais;
                break;
            case SARGENTO:
                listaParaMedia = temposEsperaSargentos;
                break;
            case CABO:
               listaParaMedia = temposEsperaCabos;
                break;
            default:
                return 0;
        }

        if (listaParaMedia.isEmpty()) return 0.0;
        return listaParaMedia.stream().mapToLong(Long::longValue).average().orElse(0.0) / 1000.0; // Em segundos
    }

    /**
     * Retorna o número total de atendimentos concluídos para uma categoria específica.
     *
     * @param categoria A categoria cujos atendimentos serão contados.
     * @return O número total de atendimentos.
     */
    public int getTotalAtendimentos(Cliente.Categoria categoria) {
        switch (categoria) {
            case OFICIAL:
                return totalAtendimentosOficiais.get();
            case SARGENTO:
                return totalAtendimentosSargentos.get();
            case CABO:
                return totalAtendimentosCabos.get();
            default:
                return 0;
        }
    }

    /**
     * Registra cada cliente GERADO pelo Sargento Tainha para fins de relatório.
     * Este método é chamado ANTES da tentativa de adicionar o cliente às filas,
     * garantindo a contagem de todas as tentativas, incluindo as que foram rejeitadas.
     *
     * @param cliente O cliente que acabou de ser gerado pelo Sargento Tainha.
     */
    public void registrarGeracaoCliente(Cliente cliente) {
        if (cliente == null) return;
        switch (cliente.getCategoria()) {
        case OFICIAL:
            totalClientesOficiaisGerados.incrementAndGet();
            break;
        case SARGENTO:
            totalClientesSargentosGerados.incrementAndGet();
            break;
        case CABO:
            totalClientesCabosGerados.incrementAndGet();
            break;
        case PAUSA:
            totalClientesPausaGerados.incrementAndGet();
            break;
        }
    }
    
    /**
     * Retorna o número total de clientes GERADOS de uma certa categoria,
     * incluindo pausas e clientes que foram mandados embora por falta de espaço.
     *
     * @param categoria A categoria a ser contada.
     * @return O número total de clientes gerados.
     */
    public int getTotalClientesGerados(Cliente.Categoria categoria) {
        if (categoria == null) return 0;

        switch (categoria) {
            case OFICIAL:
                return totalClientesOficiaisGerados.get();
            case SARGENTO:
                return totalClientesSargentosGerados.get();
            case CABO:
                return totalClientesCabosGerados.get();
            case PAUSA:
                return totalClientesPausaGerados.get();
            default:
                return 0;
        }
    }
    
    /**
     * Sinaliza que o Sargento Tainha encerrou suas atividades e não adicionará mais clientes.
     * Este método é crucial para o encerramento gracioso da simulação, pois "acorda"
     * quaisquer barbeiros que estejam dormindo para que possam finalizar seus ciclos.
     */
    public void sargentoFoiEmbora() {
        this.sargentoDispensado = true;
        // Libera 3 permits, um para cada barbeiro potencial no Caso C.
        // Isso garante que, se eles estiverem esperando em cadeirasOcupadas.acquire(),
        // eles serão liberados para prosseguir e verificar as condições de encerramento.
        cadeirasOcupadas.release(3);
    }

    /**
     * Verifica se o Sargento Tainha já foi dispensado.
     * @return true, se o sargento não está mais ativo, false caso contrário.
     */
    public boolean isSargentoDispensado() {
        return this.sargentoDispensado;
    }
}
