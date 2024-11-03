import javax.swing.*;
import java.awt.*;
import java.util.concurrent.Semaphore;

// Descrição: Classe ProducerConsumerGUI que implementa a interface gráfica e a lógica para o problema produtor-consumidor usando semáforos.
// Entrada: Tamanho do buffer (definido pelo usuário), número de produtores e consumidores, e tempos de produção e consumo.
// Saída: Log de produção e consumo exibido na interface gráfica.
// Pré-condição: Todos os parâmetros de entrada devem ser fornecidos pelo usuário.
// Pós-condição: Interface gráfica exibida, e o processo de produção e consumo controlado por semáforos.
public class ProducerConsumerGUI {
    private final int bufferSize; // Tamanho do buffer
    private final int[] buffer;   // Buffer para armazenar os itens
    private int in = 0;           // Índice de inserção
    private int out = 0;          // Índice de remoção
    private volatile boolean stopped = false; // Flag para parar o processo

    // Semáforos
    private final Semaphore mutex = new Semaphore(1); // Controle de acesso ao buffer
    private final Semaphore empty; // Controle de espaços vazios no buffer
    private final Semaphore full;  // Controle de espaços preenchidos no buffer

    private final JFrame frame;        // Janela principal
    private final JTextArea logTextArea; // Área de texto para exibir o log

    // Descrição: Construtor que configura a interface gráfica e inicializa os semáforos.
    // Entrada: Tamanho do buffer (int).
    // Saída: Interface gráfica exibida para o usuário.
    // Pré-condição: bufferSize deve ser um valor positivo.
    // Pós-condição: A interface gráfica é configurada e exibida.
    public ProducerConsumerGUI(int bufferSize) {
        this.bufferSize = bufferSize;
        this.buffer = new int[bufferSize];
        this.empty = new Semaphore(bufferSize); // Inicializa com espaços vazios
        this.full = new Semaphore(0);           // Inicializa sem espaços preenchidos

        // Configuração da interface gráfica
        frame = new JFrame("Simulação Produtor-Consumidor");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 400);
        frame.setLayout(new BorderLayout());

        logTextArea = new JTextArea();
        logTextArea.setEditable(false); // Impede edição pelo usuário
        JScrollPane scrollPane = new JScrollPane(logTextArea);
        frame.add(scrollPane, BorderLayout.CENTER);

        JButton stopButton = new JButton("Parar");
        stopButton.addActionListener(e -> stop()); // Adiciona ação para o botão de parar
        frame.add(stopButton, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    // Descrição: Método para atualizar o log na interface gráfica com uma nova mensagem.
    // Entrada: Mensagem a ser exibida no log (String).
    // Saída: Mensagem adicionada à área de log da interface.
    // Pré-condição: Interface gráfica deve estar ativa.
    // Pós-condição: Mensagem exibida no log.
    private void updateLog(String message) {
        SwingUtilities.invokeLater(() -> logTextArea.append(message + "\n"));
    }

    // Descrição: Insere um item no buffer de forma sincronizada usando semáforos.
    // Entrada: Item a ser inserido (int).
    // Saída: Log de produção exibido na interface gráfica.
    // Pré-condição: Deve haver espaço vazio no buffer.
    // Pós-condição: Item é inserido no buffer e o log é atualizado.
    public void insert(int item) throws InterruptedException {
        empty.acquire(); // Espera por um espaço vazio
        mutex.acquire(); // Acessa a região crítica
        if (stopped) {
            mutex.release();
            return;
        }
        buffer[in] = item;
        in = (in + 1) % bufferSize;
        updateLog("Produziu: " + item); // Exibe o item produzido no log
        mutex.release();
        full.release(); // Sinaliza um espaço preenchido
    }

    // Descrição: Remove um item do buffer de forma sincronizada usando semáforos.
    // Entrada: Nenhuma.
    // Saída: Item removido do buffer.
    // Pré-condição: Deve haver um item disponível no buffer.
    // Pós-condição: Item é removido do buffer e o log é atualizado.
    public int remove() throws InterruptedException {
        full.acquire(); // Espera por um item disponível
        mutex.acquire(); // Acessa a região crítica
        if (stopped) {
            mutex.release();
            return -1;
        }
        int item = buffer[out];
        buffer[out] = 0; // Limpa o buffer visualmente
        out = (out + 1) % bufferSize;
        updateLog("Consumiu: " + item); // Exibe o item consumido no log
        mutex.release();
        empty.release(); // Sinaliza um espaço vazio
        return item;
    }

    // Descrição: Método para interromper o processo de produção e consumo.
    // Entrada: Nenhuma.
    // Saída: Processo interrompido e log atualizado.
    // Pré-condição: O processo deve estar em execução.
    // Pós-condição: Processo é interrompido e as threads são liberadas.
    public void stop() {
        stopped = true;
        empty.release(bufferSize);
        full.release(bufferSize);
        updateLog("Processo interrompido."); // Exibe mensagem de interrupção no log
    }

    // Descrição: Classe interna Producer que implementa o processo de produção.
    // Entrada: Referência ao buffer (ProducerConsumerGUI) e tempo de produção (int).
    // Saída: Itens produzidos exibidos no log.
    // Pré-condição: O buffer deve estar inicializado.
    // Pós-condição: Os itens são produzidos e inseridos no buffer.
    public static class Producer implements Runnable {
        private final ProducerConsumerGUI pc;
        private final int productionTime;

        // Descrição: Construtor da classe Producer que inicializa o produtor com os parâmetros de referência ao buffer e tempo de produção.
        // Entrada: Referência ao objeto ProducerConsumerGUI (pc) e tempo de produção (int).
        // Saída: Nenhuma.
        // Pré-condição: O objeto ProducerConsumerGUI deve estar inicializado.
        // Pós-condição: Um objeto Producer é criado com o tempo de produção configurado.
        public Producer(ProducerConsumerGUI pc, int productionTime) {
            this.pc = pc;
            this.productionTime = productionTime;
        }

        // Descrição: Método principal de execução da thread de produção, que insere itens no buffer enquanto o processo não é interrompido.
        // Entrada: Nenhuma.
        // Saída: Itens produzidos e inseridos no buffer.
        // Pré-condição: O buffer deve estar inicializado e o processo não deve estar interrompido.
        // Pós-condição: Itens são produzidos e inseridos no buffer em intervalos de tempo definidos, até que o processo seja interrompido.
        @Override
        public void run() {
            try {
                // Loop de produção que continua enquanto o processo não for interrompido.
                while (!pc.stopped) {
                    int item = produceItem(); // Gera um novo item
                    pc.insert(item);          // Insere o item no buffer
                    if (pc.stopped) break;     // Verifica se o processo foi interrompido
                    Thread.sleep(productionTime); // Aguarda o tempo de produção antes de produzir o próximo item
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Interrompe a thread em caso de exceção
            }
        }


        // Descrição: Gera um item aleatório para inserção no buffer.
        // Entrada: Nenhuma.
        // Saída: Item produzido (int).
        // Pré-condição: O processo de produção deve estar em execução.
        // Pós-condição: Item gerado para ser inserido no buffer.
        private int produceItem() {
            int item = (int) (Math.random() * 100);
            pc.updateLog("Produzindo item: " + item); // Atualiza o log com o item produzido
            return item;
        }
    }

    // Descrição: Classe interna Consumer que implementa o processo de consumo.
    // Entrada: Referência ao buffer (ProducerConsumerGUI) e tempo de consumo (int).
    // Saída: Itens consumidos exibidos no log.
    // Pré-condição: O buffer deve conter itens para consumo.
    // Pós-condição: Os itens são consumidos e removidos do buffer.
    public static class Consumer implements Runnable {
        private final ProducerConsumerGUI pc;
        private final int consumptionTime;


        // Descrição: Construtor da classe Consumer que inicializa o consumidor com os parâmetros de referência ao buffer e tempo de consumo.
        // Entrada: Referência ao objeto ProducerConsumerGUI (pc) e tempo de consumo (int).
        // Saída: Nenhuma.
        // Pré-condição: O objeto ProducerConsumerGUI deve estar inicializado.
        // Pós-condição: Um objeto Consumer é criado com o tempo de consumo configurado.
        public Consumer(ProducerConsumerGUI pc, int consumptionTime) {
            this.pc = pc;
            this.consumptionTime = consumptionTime;
        }

        // Descrição: Método principal de execução da thread de consumo, que remove itens do buffer enquanto o processo não é interrompido.
        // Entrada: Nenhuma.
        // Saída: Itens consumidos e removidos do buffer.
        // Pré-condição: O buffer deve estar inicializado e o processo não deve estar interrompido.
        // Pós-condição: Itens são consumidos e removidos do buffer em intervalos de tempo definidos, até que o processo seja interrompido.
        @Override
        public void run() {
            try {
                // Loop de consumo que continua enquanto o processo não for interrompido.
                while (!pc.stopped) {
                    int item = pc.remove(); // Remove um item do buffer
                    if (item != -1) {       // Verifica se o item é válido
                        consumeItem(item);  // Consome o item se for válido
                    }
                    Thread.sleep(consumptionTime); // Aguarda o tempo de consumo antes de consumir o próximo item
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Interrompe a thread em caso de exceção
            }
        }

        // Descrição: Consome um item do buffer e exibe no log.
        // Entrada: Item a ser consumido (int).
        // Saída: Mensagem de consumo exibida no log.
        // Pré-condição: Item deve ser válido.
        // Pós-condição: Item consumido e removido do buffer.
        private void consumeItem(int item) {
            pc.updateLog("Consumindo item: " + item); // Atualiza o log com o item consumido
        }
    }

    // Descrição: Método principal para iniciar a interface gráfica e o processo de configuração.
    // Entrada: Parâmetros de entrada fornecidos pelo usuário.
    // Saída: Interface gráfica configurada e threads de produtores e consumidores iniciadas.
    // Pré-condição: Parâmetros válidos fornecidos pelo usuário.
    // Pós-condição: Interface exibida e processo iniciado.
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Janela de entrada de configuração inicial
            JTextField bufferSizeField = new JTextField();
            JTextField numProducersField = new JTextField();
            JTextField numConsumersField = new JTextField();
            JTextField productionTimeField = new JTextField();
            JTextField consumptionTimeField = new JTextField();

            Object[] fields = {
                    "Tamanho do buffer:", bufferSizeField,
                    "Número de produtores:", numProducersField,
                    "Número de consumidores:", numConsumersField,
                    "Tempo de produção (ms):", productionTimeField,
                    "Tempo de consumo (ms):", consumptionTimeField
            };

            int option = JOptionPane.showConfirmDialog(null, fields, "Configuração Inicial", JOptionPane.OK_CANCEL_OPTION);

            if (option == JOptionPane.OK_OPTION) {
                int bufferSize = Integer.parseInt(bufferSizeField.getText());
                int numProducers = Integer.parseInt(numProducersField.getText());
                int numConsumers = Integer.parseInt(numConsumersField.getText());
                int productionTime = Integer.parseInt(productionTimeField.getText());
                int consumptionTime = Integer.parseInt(consumptionTimeField.getText());

                ProducerConsumerGUI pc = new ProducerConsumerGUI(bufferSize);

                for (int i = 0; i < numProducers; i++) {
                    new Thread(new Producer(pc, productionTime)).start();
                }

                for (int i = 0; i < numConsumers; i++) {
                    new Thread(new Consumer(pc, consumptionTime)).start();
                }
            }
        });
    }
}
