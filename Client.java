import java.io.DataOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.Socket;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class Client extends Thread {
    int size;
    Color[][] map;
    Socket socket;
    DataOutputStream output;
    BufferedReader input;
    int[] linesToCalculate;
    int[][] fixedPoints;

    // Laço principal do cliente para solicitar as linhas e
    // calculá-las enviando o resultado de volta para o servidor.
    public void run() {
        String message = "";
        try {
            // Inicialmente recebe os pontos fixos do servidor
            while (!message.startsWith("fixed points:")) {
                message = this.input.readLine();
                if (message.startsWith("fixed points:")) {
                    String pointsLine = message.split(":")[1];
                    // Separa os pontos fixos e os armazena em um vetor
                    String[] points = pointsLine.split(" ");
                    this.fixedPoints = new int[points.length][2];
                    for (int i = 0; i < points.length; i ++) {
                        String[] coords = points[i].split(",");
                        this.fixedPoints[i][0] = Integer.parseInt(coords[0]);
                        this.fixedPoints[i][1] = Integer.parseInt(coords[1]);
                    }
                }
            }
        } catch (IOException err) {
            err.printStackTrace();
        }

        try {
            // LAÇO PRINCIPAL: Enquanto não receber um 'finish' do servidor
            while(!message.equals("finish")) {
                // Mensagem sinalizando o início de uma iteração (ex: 'iter 5')
                if (message.startsWith("iter ")) {
                    // Solicita e calcula todas as linhas definidas
                    // ao instanciar o cliente.
                    for (int i = 0; i < this.linesToCalculate.length; i ++) {
                        this.updateLine(this.linesToCalculate[i]);
                    }

                    // Envia uma mensagem sinalizando o fim da linha calculada
                    this.output.writeBytes("finish iter\n");
                }
                // Lê a mensagem seguinte do servidor
                message = this.input.readLine();
            }
            this.socket.close();
        } catch (IOException err) {
            err.printStackTrace();
        }
    }

    // Itera sobre os pontos recebidos e calcula uma nova linha
    public List<Color> calculateLine(int xCoord) {
        int i = 1;
        List<Color> newLine = new ArrayList<Color>();

        for (int j = 1; j < this.size - 1; j ++) {
            Color color;
            // Pula a iteração caso o ponto esteja no vetor de pontos fixos
            if (Stencil.isPointFixed(fixedPoints, xCoord, j)) {
                color = this.map[i][j];
            } else {
                // Calcula a média dos pontos
                color = Stencil.avgColor(
                    this.map[i][j],
                    this.map[i - 1][j],
                    this.map[i][j -1],
                    this.map[i + 1][j],
                    this.map[i][j + 1]
                );
            }
            // Salva o novo ponto em uma lista
            newLine.add(color);
        }

        // Devolve a lista de pontos que representa a nova linha
        return newLine;
    }

    // Método responsável por solicitar um conjunto de pontos para calcular uma linha e
    // devolvê-la ao servidor
    public void updateLine(int line) throws IOException, NumberFormatException {
        String response = "";

        // Envia mensagem solicitando uma determinada linha
        this.output.writeBytes("request line:" + String.valueOf(line) + "\n");

        while(!response.startsWith("line")) {
            response = this.input.readLine();

            // Ao receber uma mensagem "line", recebe os pontos e guarda em uma matriz auxiliar
            if (response.matches("line [0-9]+:(.)*")) {
                String pointsLine = response.split(":")[1];
                String[] points = pointsLine.split(",");
                for (int i = 0; i < points.length; i ++) {
                    Map<String, Object> point = Stencil.lineToPoint(points[i], this.size);
                    int xCoord = (int) point.get("x");
                    int yCoord = (int) point.get("y");
                    Color color = (Color) point.get("color");
                    this.map[(xCoord - line) + 1][yCoord] = color;
                }
            }
        }

        // Chama a função de calcular uma nova linha a partir dos dados recebidos
        List<Color> newLine = this.calculateLine(line);

        // Envia de volta a nova linha calculada para o servidor
        String output = "new line " + String.valueOf(line) + ":";
        for (int j = 0; j < newLine.size(); j ++) {
            String point = Stencil.pointToLine(line, j + 1, newLine.get(j));
            output = output + point + ",";
        }
        this.output.writeBytes(output + "\n");
    }

    // Instancia um novo cliente passando o tamanho do mapa e a lista de
    // linhas que deverão ser calculadas pelo cliente.
    public Client(int size, int[] linesToCalculate) throws IOException {
        this.size = size;
        this.linesToCalculate = linesToCalculate;

        // Inicializa um map auxiliar para salvar
        this.map = new Color[3][this.size];
        for (int i = 0; i < 3; i ++) {
            for (int j = 0; j < this.size; j ++) {
                if (j == 0 || j == this.size - 1) {
                    this.map[i][j] = new Color(127, 127, 127);
                } else {
                    this.map[i][j] = new Color(0, 0, 0);
                }
            }
        }

        // Cria um socket, input e output para ler e escrever no servidor
        this.socket = new Socket("localhost", 5564);  
        this.output = new DataOutputStream(this.socket.getOutputStream());
        this.input = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
    }
}
