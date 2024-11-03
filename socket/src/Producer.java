import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

// Descrição: Classe Producer que representa o produtor no problema produtor-consumidor, usando sockets para enviar itens ao consumidor.
//            Esta classe gera itens e envia ao consumidor através de um socket, com um intervalo controlado pela velocidade.
// Entrada: Endereço do host (String), porta (int), velocidade de produção (double), tamanho do buffer (int), e interface gráfica (ProducerConsumerGUI).
// Saída: Mensagens de log que atualizam a interface com o status de itens produzidos.
// Pré-condição: O consumidor deve estar em execução e disponível para receber a conexão.
// Pós-condição: A conexão é fechada após o envio de todos os itens.

public class Producer extends Thread {
    private String host; // Endereço do host onde o consumidor está executando.
    private int port; // Porta do servidor consumidor.
    private double speed; // Velocidade de produção de itens por segundo.
    private int bufferSize; // Quantidade de itens a serem produzidos.
    private ProducerConsumerGUI gui; // Interface gráfica para atualizar logs.

    // Descrição: Construtor da classe Producer que inicializa o produtor com os parâmetros fornecidos.
    // Entrada: Host (String), porta (int), velocidade de produção (double), tamanho do buffer (int), e a interface gráfica (ProducerConsumerGUI).
    // Saída: Nenhuma.
    // Pré-condição: Parâmetros válidos e o consumidor deve estar em execução.
    // Pós-condição: Objeto Producer configurado para enviar itens ao consumidor.
    public Producer(String host, int port, double speed, int bufferSize, ProducerConsumerGUI gui) {
        this.host = host;
        this.port = port;
        this.speed = speed;
        this.bufferSize = bufferSize;
        this.gui = gui;
    }

    // Descrição: Método principal da thread que cria um socket e envia itens para o consumidor com base na velocidade definida.
    // Entrada: Nenhuma.
    // Saída: Mensagens de log sobre itens produzidos.
    // Pré-condição: O consumidor deve estar escutando na porta especificada.
    // Pós-condição: O socket é fechado após o envio de todos os itens.
    @Override
    public void run() {
        try {
            // Cria um socket para se conectar ao consumidor no endereço e porta especificados.
            Socket socket = new Socket(host, port);
            // Cria um PrintWriter para enviar mensagens (itens) ao consumidor através do socket.
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            // Loop para produzir e enviar itens até atingir o bufferSize.
            for (int i = 0; i < bufferSize; i++) {
                // Simula o tempo de produção com base na velocidade definida.
                Thread.sleep((long) (1000 / speed));

                // Gera o item com um identificador único.
                String item = "Item " + (i + 1);

                // Envia o item ao consumidor através do socket.
                out.println(item);

                // Atualiza a interface gráfica com o log do item produzido.
                gui.updateLog("Produziu: " + item);
            }

            // Fecha o fluxo de saída e o socket após a produção.
            out.close();
            socket.close();
        } catch (Exception e) {
            // Captura qualquer exceção e atualiza a interface com a mensagem de erro.
            gui.updateLog("Erro no Produtor: " + e.getMessage());
        }
    }
}
