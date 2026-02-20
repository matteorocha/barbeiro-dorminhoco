import java.util.Random;

public class SargentoTainha implements Runnable {
    private Barbearia barbearia;
    private int periodoCochiloMinMs;
    private int periodoCochiloMaxMs;
    private Random random;

    // Constantes baseadas nos requisitos do trabalho e do professor
    private final int TOTAL_CLIENTES_PARA_GERAR = 1000; // 
    private final int TAXA_GERACAO_EXTERNA_MS = 1000; // 1 cliente a cada 1 segundo (conforme professor)

    private volatile boolean encerrar = false;
    private int tentativasVaziasSequenciais = 0;
    private final int MAX_TENTATIVAS_VAZIAS = 3; // 
    private int clientesGerados = 0;

    public SargentoTainha(Barbearia barbearia, int periodoCochiloMinMs, int periodoCochiloMaxMs) {
        this.barbearia = barbearia;
        this.periodoCochiloMinMs = periodoCochiloMinMs;
        this.periodoCochiloMaxMs = periodoCochiloMaxMs;
        this.random = new Random();
    }

    @Override
    public void run() {
        System.out.println("Sargento Tainha começou a tentar adicionar clientes.");
        while (!encerrar && clientesGerados < TOTAL_CLIENTES_PARA_GERAR) {
            try {
                // 1. Sargento dorme por um tempo aleatório definido na inicialização
                long tempoCochilo = random.nextInt(periodoCochiloMaxMs - periodoCochiloMinMs + 1) + periodoCochiloMinMs;
                Thread.sleep(tempoCochilo);

                // 2. Calcula quantos clientes "chegaram" na fila externa enquanto ele dormia
                // A uma taxa de 1 cliente por segundo (1000 ms)
                int clientesQueChegaram = (int) (tempoCochilo / TAXA_GERACAO_EXTERNA_MS);
                if (clientesQueChegaram == 0 && tempoCochilo > 0) {
                    // Garante que pelo menos 1 cliente seja processado se o sargento cochilou
                    clientesQueChegaram = 1;
                }

                boolean adicionouPeloMenosUmRealNesteCiclo = false;
                boolean encontrouPausaNesteCiclo = false;

                // 3. Processa cada cliente que "chegou" durante o cochilo
                for (int i = 0; i < clientesQueChegaram && clientesGerados < TOTAL_CLIENTES_PARA_GERAR; i++) {
                    Cliente clienteTentativa = Cliente.gerarClienteAleatorio();
                    barbearia.registrarGeracaoCliente(clienteTentativa); // Registra para o relatório

                    if (clienteTentativa.getCategoria() == Cliente.Categoria.PAUSA) {
                        encontrouPausaNesteCiclo = true;
                    } else {
                        clientesGerados++;
                        if (barbearia.adicionarCliente(clienteTentativa)) {
                            System.out.println("Sargento Tainha adicionou um " + clienteTentativa.getCategoria() + ". (" + clientesGerados + "/" + TOTAL_CLIENTES_PARA_GERAR + ")");
                            adicionouPeloMenosUmRealNesteCiclo = true;
                        } else {
                            System.out.println("Sargento Tainha tentou adicionar um " + clienteTentativa.getCategoria() + ", mas a barbearia está cheia.");
                        }
                    }
                }

                // 4. Lógica de encerramento baseada na geração de "PAUSA"
                // Se encontrou uma pausa e não conseguiu adicionar NENHUM cliente real neste ciclo,
                // conta como uma tentativa vazia.
                if (encontrouPausaNesteCiclo && !adicionouPeloMenosUmRealNesteCiclo) {
                    tentativasVaziasSequenciais++;
                } else if (adicionouPeloMenosUmRealNesteCiclo) {
                    // Se conseguiu adicionar pelo menos UM cliente real, reseta o contador
                    tentativasVaziasSequenciais = 0;
                }

                // Verifica critério de término
                if (tentativasVaziasSequenciais >= MAX_TENTATIVAS_VAZIAS) {
                    System.out.println("Sargento Tainha foi para casa (3 tentativas vazias sequenciais).");
                    encerrar = true;
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Sargento Tainha interrompido.");
                break;
            }
        }
        System.out.println("Sargento Tainha finalizou suas operações de adição. Total de clientes gerados: " + clientesGerados);
        barbearia.sargentoFoiEmbora();
    }

    public void encerrar() {
        this.encerrar = true;
    }
}