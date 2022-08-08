import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class ServerWorker {
    Server server;
    Socket socket;
    BufferedReader input;
    DataOutputStream output;
    int size;
    Color[][] auxMap;
    List<Integer> calculatedLines;

    public ServerWorker(Server server, Socket socket) throws IOException {
        // Salva o socket, cria input e output para ler e escrever para o cliente
        this.server = server;
        this.socket = socket;
        this.input = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        this.output = new DataOutputStream(this.socket.getOutputStream());
        this.size = this.server.size;
        this.auxMap = new Color[this.size - 1][this.size - 1];
    }

    public void sendFixedPoints() throws IOException {
        String output = "fixed points:";
        for (int i = 0; i < this.server.fixedPoints.length; i ++) {
            int xCoord = this.server.fixedPoints[i][0];
            int yCoord = this.server.fixedPoints[i][1];
            output += String.valueOf(xCoord) + "," + String.valueOf(yCoord) + " ";
        }
        this.output.writeBytes(output + "\n");
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

    // Armazena uma linha calculada pelo cliente no mapa principal
    private void saveCalculatedLine(String message) {
        Map<String, Object> point = Stencil.lineToPoint(message, this.server.size);
        int xCoord = (int) point.get("x");
        int yCoord = (int) point.get("y");
        Color color = (Color) point.get("color");

        // Insere o novo ponto em um mapa auxiliar
        this.auxMap[xCoord][yCoord] = color;

        // Salva o número da linha calculada
        this.calculatedLines.add(xCoord);
    }

    // Envia uma mensagem sinalizando o fim de todas as iterações
    public void sendFinish() throws IOException {
        this.output.writeBytes("finish\n");
    }

    // Inicia uma nova iteração e gerencia as requisições
    // e respostas do cliente
    public void callNewIteration(int iter) throws IOException {
        String message = "";

        // Limpa a lista de linhas calculadas
        this.calculatedLines = new ArrayList<Integer>();

        // Informa o cliente o início de uma nova iteração
        this.output.writeBytes("iter " + String.valueOf(iter) + "\n");

        // Recebe a requisição de uma linha e as linhas calculadas
        // até o fim da iteração
        while (!message.equals("finish iter")) {
            // Caso a mensagem seja uma solicitação de uma linha
            if (message.startsWith("request line:")) {
                int lineNumber = Integer.parseInt(message.split(":")[1]);
                this.sendRequestedLine(lineNumber);
            }

            // Caso a mensagem seja em formato de 5 inteiros representando
            // um ponto, então o servidor está recebendo a resposta do
            // cliente (nova linha calculada)
            if (message.matches("([0-9]+( )){4}[0-9]+")) {
                this.saveCalculatedLine(message);
            }

            // Lê a mensagem seguinte do cliente
            message = this.input.readLine();
        }

        // Commita as linhas calculadas na imagem ao fim de cada iteração
        this.calculatedLines.forEach(i -> {
            for (int j = 1; j < this.size - 1; j ++) {
                this.server.map[i][j] = this.auxMap[i][j];
            }
        });
    }
}
