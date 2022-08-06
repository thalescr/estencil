import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.awt.Color;

public class ServerWorker {
    Server server;
    Socket socket;
    BufferedReader input;
    DataOutputStream output;

    public ServerWorker(Server server, Socket socket) throws IOException {
        // Salva o socket, cria input e output para ler e escrever para o cliente
        this.server = server;
        this.socket = socket;
        this.input = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        this.output = new DataOutputStream(this.socket.getOutputStream());
    }

    private void sendRequestedLine(int lineNumber) throws IOException {
        // Pega os pontos das linhas anteriores e posteriores a linha solicitada
        for (int i = lineNumber - 1; i < lineNumber + 2; i ++) {
            for (int j = 1; j < this.server.map.length - 1; j ++) {
                Color color = this.server.map[i][j];
                try {
                    // Envia cada ponto para o servidor em uma string
                    String line = Stencil.pointToLine(i, j, color);
                    this.output.writeBytes(line + "\n");
                } catch (IOException err) {
                    err.printStackTrace();
                }
            }
        }

        // Envia uma mensagem sinalizando o fim da linha enviada
        this.output.writeBytes("end\n");
    }

    private void saveCalculatedLine(String message) {
        Map<String, Object> point = Stencil.lineToPoint(message, this.server.size);
        int xCoord = (int) point.get("x");
        int yCoord = (int) point.get("y");
        Color color = (Color) point.get("color");

        // Insere o novo ponto na imagem do servidor
        this.server.map[xCoord][yCoord] = color;
    }

    public void sendFinish() throws IOException {
        this.output.writeBytes("finish\n");
    }

    public void callNewIteration(int iter) throws IOException {
        String message = "";

        // Informa o cliente o início de uma nova iteração
        this.output.writeBytes("iter " + String.valueOf(iter) + "\n");

        // Recebe a requisição de uma linha e as linhas calculadas até o fim da iteração
        while (!message.equals("finish iter")) {
            // Caso a mensagem seja uma solicitação de uma linha
            if (message.startsWith("request line:")) {
                int lineNumber = Integer.parseInt(message.split(":")[1]);
                this.sendRequestedLine(lineNumber);
            }

            // Caso a mensagem seja em formato de 5 inteiros representando um ponto
            // Então o servidor está recebendo a resposta do cliente (nova linha calculada)
            if (message.matches("([0-9]+( )){4}[0-9]+")) {
                this.saveCalculatedLine(message);
            }
            message = this.input.readLine();
        }
    }
}
