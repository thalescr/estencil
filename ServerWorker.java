import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.List;
import java.util.Map;

public class ServerWorker {
    Server server;
    Socket socket;
    BufferedReader input;
    DataOutputStream output;
    int size;
    Color[][] auxMap;
    List<Integer> linesToCalculate;

    public ServerWorker(Server server, Socket socket) throws IOException {
        // Salva o socket, cria input e output para ler e escrever para o cliente
        this.server = server;
        this.socket = socket;
        this.input = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        this.output = new DataOutputStream(this.socket.getOutputStream());
        this.size = this.server.size;
        this.auxMap = Stencil.initMap(this.size);
    }

    public void sendInitialInfo() throws IOException {
        // Envia o tamanho do mapa
        String output = "size:" + String.valueOf(this.size);

        // Envia as linhas que serão calculadas
        output = output + ";lines to calculate:";
        for (int i = 0; i < this.linesToCalculate.size(); i ++) {
            output += String.valueOf(this.linesToCalculate.get(i)) + ",";
        }
        output = output.substring(0, output.length() - 1) + ";fixed points:";

        // Envia os pontos fixos
        for (int i = 0; i < this.server.fixedPoints.length; i ++) {
            int xCoord = this.server.fixedPoints[i][0];
            int yCoord = this.server.fixedPoints[i][1];
            output += String.valueOf(xCoord) + "," + String.valueOf(yCoord) + " ";
        }
        output = output.substring(0, output.length() - 1);
        this.output.writeBytes(output + "\n");
    }

    private void sendLinesToCalculate() throws IOException {
        int startIndex = this.linesToCalculate.get(0) - 1;
        int stopIndex = this.linesToCalculate.get(this.linesToCalculate.size() - 1) + 1;

        for (int i = startIndex; i < stopIndex + 1; i ++) {
            String output = "line " + String.valueOf(i) + ":";
            for (int j = 1; j < this.size - 1; j ++) {
                Color color = this.server.map[i][j];
                String line = Stencil.pointToLine(i, j, color);
                output = output + line + ",";
            }

            // Envia os pontos das linhas separados por virgula
            try {
                output = output.substring(0, output.length() - 1);
                this.output.writeBytes(output + "\n");
            } catch (IOException err) {
                err.printStackTrace();
            }
        }
    }

    // Armazena uma linha calculada pelo cliente no mapa principal
    private void saveCalculatedLine(String message) {
        String pointsLine = message.split(":")[1];
        String[] points = pointsLine.split(",");

        for (int i = 0; i < points.length; i ++) {
            Map<String, Object> point = Stencil.lineToPoint(points[i]);
            int xCoord = (int) point.get("x");
            int yCoord = (int) point.get("y");
            Color color = (Color) point.get("color");

            // Insere o novo ponto em um mapa auxiliar
            this.auxMap[xCoord][yCoord] = color;
        }
    }

    // Envia uma mensagem sinalizando o fim de todas as iterações
    public void sendFinish() throws IOException {
        this.output.writeBytes("finish\n");
    }

    // Inicia uma nova iteração e gerencia as requisições
    // e respostas do cliente
    public void callNewIteration(int iter) throws IOException {
        String message = "";

        // Envia as linhas para o cliente
        this.sendLinesToCalculate();

        // Informa o cliente o início de uma nova iteração
        this.output.writeBytes("iter " + String.valueOf(iter) + "\n");

        // Recebe a requisição de uma linha e as linhas calculadas
        // até o fim da iteração
        while (!message.equals("finish iter")) {
            // Caso a mensagem seja uma "new line" então o servidor está recebendo
            // a resposta do cliente (nova linha calculada)
            if (message.matches("new line [0-9]+:(.)*")) {
                this.saveCalculatedLine(message);
            }

            // Lê a mensagem seguinte do cliente
            message = this.input.readLine();
        }

        // Commita as linhas calculadas na imagem ao fim de cada iteração
        this.linesToCalculate.forEach(i -> {
            for (int j = 1; j < this.size - 1; j ++) {
                this.server.map[i][j] = this.auxMap[i][j];
            }
        });
    }
}
