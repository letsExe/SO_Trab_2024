import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

// Descrição: Classe Consumer que representa o consumidor no problema produtor-consumidor usando sockets para comunicação.
//            Esta classe aguarda uma conexão do produtor, recebe itens via socket e simula o consumo desses itens.
// Entrada: Porta de conexão (int), velocidade de consumo (double) e a interface gráfica (ProducerConsumerGUI).
// Saída: Mensagens de log que atualizam a interface com o status de conexão e itens consumidos.
// Pré-condição: A porta especificada deve estar livre para ser utilizada.
// Pós-condição: A conexão é fechada e o consumo de itens é registrado na interface.

public class Consumer extends Thread {
    private int port; // Porta onde o servidor irá escutar.
    private double speed; // Velocidade de consumo em itens por segundo.
    private ProducerConsumerGUI gui; // Interface gráfica para atualizar logs.

    // Descrição: Construtor da classe Consumer que inicializa o consumidor com os parâmetros fornecidos.
    // Entrada: Porta de conexão (int), velocidade de consumo (double) e a interface gráfica (ProducerConsumerGUI).
    // Saída: Nenhuma.
    // Pré-condição: Porta deve ser um valor válido e disponível, e gui deve estar inicializada.
    // Pós-condição: Objeto Consumer configurado para escutar a porta e consumir itens.
    public Consumer(int port, double speed, ProducerConsumerGUI gui) {
        this.port = port;
        this.speed = speed;
        this.gui = gui;
    }

    // Descrição: Método principal da thread que inicializa o servidor e consome itens enviados pelo produtor.
    // Entrada: Nenhuma.
    // Saída: Mensagens de log sobre o status da conexão e itens consumidos.
    // Pré-condição: A porta deve estar disponível para conexão.
    // Pós-condição: O servidor é encerrado quando o consumo termina ou ocorre uma exceção.
    @Override
    public void run() {
        try {
            // Cria um socket servidor na porta especificada, onde o consumidor aguardará uma conexão do produtor.
            ServerSocket serverSocket = new ServerSocket(port);
            gui.updateLog("Consumidor esperando conexão..."); // Atualiza a interface indicando que está esperando por conexão.

            // Aceita a conexão de um produtor. O método accept bloqueia até que um produtor se conecte.
            Socket socket = serverSocket.accept();
            gui.updateLog("Conexão estabelecida com o Produtor."); // Atualiza a interface indicando que a conexão foi estabelecida.

            // Cria um BufferedReader para ler as mensagens (itens) enviadas pelo produtor através do socket.
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String item; // Variável que armazena cada item lido.
            // Lê os itens produzidos enquanto o produtor enviar dados.
            while ((item = in.readLine()) != null) {
                // Simula o tempo de consumo baseado na velocidade especificada.
                Thread.sleep((long) (1000 / speed)); // Converte a velocidade em tempo de espera entre consumos.
                gui.updateLog("Consumiu: " + item); // Atualiza a interface indicando que o item foi consumido.
            }

            // Fecha os fluxos de entrada e o socket após terminar o consumo.
            in.close();
            socket.close();
            serverSocket.close();
        } catch (Exception e) {
            // Captura qualquer exceção e atualiza a interface com a mensagem de erro.
            gui.updateLog("Erro no Consumidor: " + e.getMessage());
        }
    }
}

