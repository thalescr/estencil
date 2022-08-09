import java.io.DataOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Client extends Thread {
    int size;
    Color[][] map;
    Socket socket;
    DataOutputStream output;
    BufferedReader input;
    List<Integer> linesToCalculate;
    int[][] fixedPoints;

    // Itera sobre os pontos recebidos e calcula as novas linhas
    public Color[][] calculateLines() {
        Color[][] resultMap = Stencil.initMap(this.size);
        this.linesToCalculate.forEach(i -> {
            for (int j = 1; j < this.size - 1; j ++) {
                // Pula a iteração caso o ponto esteja no vetor de pontos fixos
                if (Stencil.isPointFixed(this.fixedPoints, i, j)) {
                    resultMap[i][j] = this.map[i][j];
                } else {
                    // Calcula a média dos pontos
                    resultMap[i][j] = Stencil.avgColor(
                        this.map[i][j],
                        this.map[i - 1][j],
                        this.map[i][j -1],
                        this.map[i + 1][j],
                        this.map[i][j + 1]
                    );
                }
            }
        });
        return resultMap;
    }

    // Laço principal do cliente para solicitar as linhas e
    // calculá-las enviando o resultado de volta para o servidor.
    public void run() {
        String message = "";
        // Inicialmente recebe os pontos fixos do servidor
        while (!message.startsWith("size:")) {
            try {
                message = this.input.readLine();
            } catch (IOException err) {
                err.printStackTrace();
            }

            if (message.startsWith("size:")) {
                // Armazena o tamanho do mapa e inicializa o mapa
                String sizeMessage = message.split(";")[0];
                this.size = Integer.parseInt(sizeMessage.split(":")[1]);
                this.map = Stencil.initMap(this.size);

                // Armazena as linhas para calcular
                String linesToCalculateMessage = message.split(";")[1];
                String[] linesText = linesToCalculateMessage.split(":")[1].split(",");
                this.linesToCalculate = new ArrayList<Integer>();
                for (int i = 0; i < linesText.length; i ++) {
                    this.linesToCalculate.add(Integer.parseInt(linesText[i]));
                }

                // Separa os pontos fixos e os armazena em um vetor
                String fixedPointsMessage = message.split(";")[2];
                String pointsLine = fixedPointsMessage.split(":")[1];

                String[] points = pointsLine.split(" ");
                this.fixedPoints = new int[points.length][2];
                for (int i = 0; i < points.length; i ++) {
                    String[] coords = points[i].split(",");
                    this.fixedPoints[i][0] = Integer.parseInt(coords[0]);
                    this.fixedPoints[i][1] = Integer.parseInt(coords[1]);
                }
            }
        }

        // LAÇO PRINCIPAL: Enquanto não receber 'finish' do servidor realiza
        // os calculos das linhas que foram recebidas e envia de volta
        while(!message.equals("finish")) {
            // Ao receber uma mensagem "line", recebe os pontos e guarda em uma matriz auxiliar
            if (message.matches("line [0-9]+:(.)*")) {
                String pointsLine = message.split(":")[1];
                String[] points = pointsLine.split(",");
                for (int i = 0; i < points.length; i ++) {
                    Map<String, Object> point = Stencil.lineToPoint(points[i]);
                    int xCoord = (int) point.get("x");
                    int yCoord = (int) point.get("y");
                    Color color = (Color) point.get("color");
                    this.map[xCoord][yCoord] = color;
                }
            }

            // Mensagem sinalizando o início de uma iteração (ex: 'iter 5')
            if (message.startsWith("iter ")) {
                Color[][] resultMap = this.calculateLines();

                // Envia de volta as linhas calculadas para o servidor
                this.linesToCalculate.forEach(i -> {
                    String output = "new line " + String.valueOf(i) + ":";
                    for (int j = 1; j < resultMap.length - 1; j ++) {
                        String point = Stencil.pointToLine(i, j, resultMap[i][j]);
                        output = output + point + ",";
                    }

                    try {
                        this.output.writeBytes(output + "\n");
                    } catch (IOException err) {
                        err.printStackTrace();
                    }
                });

                // Envia uma mensagem sinalizando o fim da linha calculada
                try {
                    this.output.writeBytes("finish iter\n");
                } catch (IOException err) {
                    err.printStackTrace();
                }
            }

            // Lê a mensagem seguinte do servidor
            try {
                message = this.input.readLine();
            } catch (IOException err) {
                err.printStackTrace();
            }
        }
        try {
            this.socket.close();
        } catch (IOException err) {
            err.printStackTrace();
        }
    }

    // Instancia um novo cliente passando o tamanho do mapa e a lista de
    // linhas que deverão ser calculadas pelo cliente.
    public Client() throws IOException {
        // Cria um socket, input e output para ler e escrever no servidor
        this.socket = new Socket("localhost", 5564);  
        this.output = new DataOutputStream(this.socket.getOutputStream());
        this.input = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
    }
}
