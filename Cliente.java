// NENHUMA linha de 'package' aqui

import java.util.Random;

/**
 * Representa um único cliente da barbearia.
 * Esta é uma classe de dados (POJO) que armazena a categoria do cliente,
 * o tempo necessário para seu atendimento e o momento de sua criação para
 * calcular o tempo de espera.
 *
 * @author Matheus Rocha
 * @author Guilherme Sahdo
 * @version 1.0
 */

public class Cliente {
	/**
     * Enumeração para as categorias de clientes, que também definem a prioridade de atendimento.
     * A categoria PAUSA é um evento especial para indicar que não havia ninguém na fila externa. [cite: 35, 37]
     */
    public enum Categoria {
        OFICIAL,    // Prioridade mais alta
        SARGENTO,   // Prioridade média
        CABO,       // Prioridade mais baixa
        PAUSA       // Categoria 0 para indicar que não há ninguém na fila
    }

    private Categoria categoria;
    private int tempoServico; // Duração do corte de cabelo em segundos
    private long tempoChegada; // Momento em que o cliente foi criado

    /**
     * Constrói uma nova instância de Cliente.
     * O tempo de chegada é registrado automaticamente no momento da criação.
     *
     * @param categoria A categoria do cliente (OFICIAL, SARGENTO, CABO).
     * @param tempoServico O tempo em segundos necessário para atender este cliente.
     */
    public Cliente(Categoria categoria, int tempoServico) {
        this.categoria = categoria;
        this.tempoServico = tempoServico;
        this.tempoChegada = System.currentTimeMillis();
    }

    /**
     * Retorna a categoria do cliente.
     * @return A categoria do cliente.
     */
    public Categoria getCategoria() {
        return categoria;
    }

    /**
     * Retorna o tempo de serviço necessário para este cliente.
     * @return O tempo de serviço em segundos.
     */
    public int getTempoServico() {
        return tempoServico;
    }

    /**
     * Retorna o timestamp de quando o cliente foi criado.
     * @return O tempo de chegada em milissegundos.
     */
    public long getTempoChegada() {
        return tempoChegada;
    }

    /**
     * Método de fábrica estático para gerar um cliente com categoria e tempo de serviço aleatórios,
     * seguindo as regras do trabalho prático.
     * @return Uma nova instância de Cliente.
     */
    public static Cliente gerarClienteAleatorio() {
        Random random = new Random();
        int tipoCategoria = random.nextInt(4);

        Categoria categoria;
        int tempoServico = 0;

        switch (tipoCategoria) {
            case 0:
                categoria = Categoria.PAUSA;
                tempoServico = 0;
                break;
            case 1:
                categoria = Categoria.OFICIAL;
                tempoServico = random.nextInt(3) + 4; // 4 a 6 segundos [cite: 17]
                break;
            case 2:
                categoria = Categoria.SARGENTO;
                tempoServico = random.nextInt(3) + 2; // 2 a 4 segundos [cite: 17]
                break;
            case 3:
                categoria = Categoria.CABO;
                tempoServico = random.nextInt(3) + 1; // 1 a 3 segundos [cite: 17]
                break;
            default:
                categoria = Categoria.PAUSA;
                tempoServico = 0;
                break;
        }
        return new Cliente(categoria, tempoServico);
    }
}