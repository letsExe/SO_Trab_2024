import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

// Descrição: Classe ProducerConsumerGUI que representa a interface gráfica para o problema produtor-consumidor usando sockets.
//            A interface permite definir parâmetros de produção e consumo, inicializar o processo e exibir o log de atividades.
// Entrada: Parâmetros de velocidade dos produtores e consumidores, tamanho do buffer, e número de produtores e consumidores.
// Saída: Log com informações sobre produção e consumo de itens.
// Pré-condição: Todos os parâmetros devem estar corretamente preenchidos.
// Pós-condição: A interface permite iniciar e visualizar o processo.

public class ProducerConsumerGUI extends JFrame {
    private JTextField producerSpeedField; // Campo de texto para velocidade do produtor.
    private JTextField consumerSpeedField; // Campo de texto para velocidade do consumidor.
    private JTextField bufferSizeField;    // Campo de texto para o tamanho do buffer.
    private JTextField numProducersField;  // Campo de texto para o número de produtores.
    private JTextField numConsumersField;  // Campo de texto para o número de consumidores.
    private JTextArea logArea;             // Área de texto para exibir logs.
    private JButton startButton;           // Botão para iniciar o processo.

    private List<Producer> producers;      // Lista para armazenar threads de produtores.
    private List<Consumer> consumers;      // Lista para armazenar threads de consumidores.

    // Descrição: Construtor que configura a interface gráfica inicial com campos de entrada, botões e área de log.
    // Entrada: Nenhuma.
    // Saída: Interface gráfica exibida para o usuário.
    // Pré-condição: Parâmetros padrões iniciais definidos.
    // Pós-condição: Interface pronta para receber valores e iniciar o processo.
    public ProducerConsumerGUI() {
        setTitle("Produtor-Consumidor com Sockets"); // Define o título da janela.
        setSize(500, 500); // Define o tamanho da janela.
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Fecha a aplicação ao fechar a janela.
        setLocationRelativeTo(null); // Centraliza a janela na tela.

        // Painel de parâmetros
        JPanel paramPanel = new JPanel(new GridLayout(6, 2, 5, 5)); // Cria painel de grade para entrada de parâmetros.
        paramPanel.setBorder(BorderFactory.createTitledBorder("Parâmetros")); // Adiciona uma borda com título.

        // Campos de entrada de velocidade do produtor e do consumidor, tamanho do buffer, e número de produtores e consumidores.
        paramPanel.add(new JLabel("Velocidade do Produtor (itens/s):"));
        producerSpeedField = new JTextField("1.0"); // Campo de entrada com valor padrão para velocidade do produtor.
        paramPanel.add(producerSpeedField);

        paramPanel.add(new JLabel("Velocidade do Consumidor (itens/s):"));
        consumerSpeedField = new JTextField("1.0"); // Campo de entrada com valor padrão para velocidade do consumidor.
        paramPanel.add(consumerSpeedField);

        paramPanel.add(new JLabel("Tamanho do Buffer:"));
        bufferSizeField = new JTextField("5"); // Campo de entrada com valor padrão para o tamanho do buffer.
        paramPanel.add(bufferSizeField);

        paramPanel.add(new JLabel("Número de Produtores:"));
        numProducersField = new JTextField("1"); // Campo de entrada com valor padrão para o número de produtores.
        paramPanel.add(numProducersField);

        paramPanel.add(new JLabel("Número de Consumidores:"));
        numConsumersField = new JTextField("1"); // Campo de entrada com valor padrão para o número de consumidores.
        paramPanel.add(numConsumersField);

        startButton = new JButton("Iniciar"); // Botão para iniciar o processo.
        paramPanel.add(startButton);

        paramPanel.add(new JLabel("")); // Espaço vazio para alinhamento.

        // Área de log para exibir mensagens do processo.
        logArea = new JTextArea();
        logArea.setEditable(false); // Impede que o usuário edite o log.
        JScrollPane scrollPane = new JScrollPane(logArea); // Adiciona barra de rolagem à área de log.

        // Layout principal da interface gráfica
        setLayout(new BorderLayout());
        add(paramPanel, BorderLayout.NORTH); // Adiciona o painel de parâmetros na parte superior.
        add(scrollPane, BorderLayout.CENTER); // Adiciona a área de log ao centro.

        // Ação do botão Iniciar
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startProcess(); // Inicia o processo quando o botão é clicado.
            }
        });
    }

    // Descrição: Atualiza o log na interface gráfica com uma nova mensagem.
    // Entrada: Mensagem de log (String).
    // Saída: Mensagem exibida na área de log.
    // Pré-condição: Interface gráfica deve estar ativa.
    // Pós-condição: Mensagem adicionada ao log.
    public void updateLog(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n"); // Adiciona a mensagem ao log.
        });
    }

    // Descrição: Método para iniciar o processo de criação de produtores e consumidores, e configurar os parâmetros.
    // Entrada: Nenhuma.
    // Saída: Threads de produtores e consumidores iniciadas.
    // Pré-condição: Parâmetros de entrada preenchidos corretamente.
    // Pós-condição: Processo iniciado e log atualizado.
    private void startProcess() {
        logArea.setText(""); // Limpa o log.

        double producerSpeed = Double.parseDouble(producerSpeedField.getText()); // Lê a velocidade do produtor.
        double consumerSpeed = Double.parseDouble(consumerSpeedField.getText()); // Lê a velocidade do consumidor.
        int bufferSize = Integer.parseInt(bufferSizeField.getText()); // Lê o tamanho do buffer.
        int numProducers = Integer.parseInt(numProducersField.getText()); // Lê o número de produtores.
        int numConsumers = Integer.parseInt(numConsumersField.getText()); // Lê o número de consumidores.

        int basePort = 5000; // Porta base para os consumidores.
        String host = "localhost"; // Host onde o consumidor estará rodando.

        producers = new ArrayList<>(); // Inicializa a lista de produtores.
        consumers = new ArrayList<>(); // Inicializa a lista de consumidores.

        // Inicia os consumidores primeiro para garantir que estejam prontos para aceitar as conexões.
        for (int i = 0; i < numConsumers; i++) {
            Consumer consumer = new Consumer(basePort + i, consumerSpeed, this);
            consumer.start();
            consumers.add(consumer);
        }

        // Aguarda um pouco para garantir que os consumidores estão prontos.
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Inicia os produtores
        for (int i = 0; i < numProducers; i++) {
            Producer producer = new Producer(host, basePort + (i % numConsumers), producerSpeed, bufferSize, this);
            producer.start();
            producers.add(producer);
        }

        startButton.setEnabled(false); // Desabilita o botão Iniciar.

        // Thread para reativar o botão após as threads finalizarem.
        new Thread(() -> {
            try {
                for (Producer producer : producers) {
                    producer.join(); // Aguarda a finalização de cada produtor.
                }
                for (Consumer consumer : consumers) {
                    consumer.join(); // Aguarda a finalização de cada consumidor.
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            SwingUtilities.invokeLater(() -> {
                startButton.setEnabled(true); // Reabilita o botão Iniciar após a finalização de todas as threads.
            });
        }).start();
    }

    // Descrição: Método principal para iniciar a interface gráfica.
    // Entrada: Argumentos da linha de comando (não utilizados).
    // Saída: Interface gráfica exibida.
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ProducerConsumerGUI gui = new ProducerConsumerGUI(); // Instancia a GUI.
            gui.setVisible(true); // Torna a GUI visível.
        });
    }
}

