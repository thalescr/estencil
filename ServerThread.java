import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.awt.Color;

public class ServerThread extends Thread {
    Server server;
    Socket socket;
    BufferedReader input;
    DataOutputStream output;

    public ServerThread(Server server, Socket socket) throws IOException {
        // Salva o socket, cria input e output para ler e escrever para o cliente
        this.server = server;
        this.socket = socket;
        this.input = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        this.output = new DataOutputStream(this.socket.getOutputStream());
    }

    // Retorna os pontos das linhas anteriores e posteriores a linha solicitada
    public List<String> getPointsToCalculateLine(int lineNumber) {
        List<String> line = new ArrayList<String>();
        for (int i = lineNumber - 1; i < lineNumber + 2; i ++) {
            for (int j = 1; j < this.server.map.length - 1; j ++) {
                line.add(Stencil.pointToLine(i, j, this.server.map[i][j]));
            }
        }
        return line;
    }

    public void run() {
        String message = "";

        // Recebe mensagens até o servidor fechar o socket
        while (message != null) {
            try {
                // Caso a mensagem seja uma solicitação de uma linha
                if (message.startsWith("request line:")) {
                    int lineNumber = Integer.parseInt(message.split(":")[1]);

                    // Pega os pontos necessários para calcular a linha solicitada
                    List<String> points = this.getPointsToCalculateLine(lineNumber);
                    points.forEach(point -> {
                        try {
                            // Envia cada ponto para o servidor em uma string
                            this.output.writeBytes(point + "\n");
                        } catch (IOException err) {
                            err.printStackTrace();
                        }
                    });

                    // Envia uma mensagem sinalizando o fim da mensagem
                    this.output.writeBytes("end\n");
                }

                // Caso a mensagem seja em formato de 5 inteiros representando um ponto
                // Então o servidor está recebendo a resposta do cliente (nova linha calculada)
                if (message.matches("([0-9]+( )*){5}")) {
                    // Converte a string recebida em coordenadas x, y e um objeto de cor
                    Map<String, Object> point = Stencil.lineToPoint(message, this.server.size);
                    int xCoord = (int) point.get("x");
                    int yCoord = (int) point.get("y");
                    Color color = (Color) point.get("color");

                    // Insere o novo ponto na imagem do servidor
                    this.server.map[xCoord][yCoord] = color;
                }
                message = this.input.readLine();
            } catch (IOException err) {
                err.printStackTrace();
            }
        }
    }
}
