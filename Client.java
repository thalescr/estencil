import java.io.DataOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.Socket;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.awt.Color;

public class Client {
    int size;
    Color[][] map;
    Socket socket;
    DataOutputStream output;
    BufferedReader input;
    int[] linesToCalculate;

    // Calcula cada linha que foi informado para o cliente calcular
    public void calculateIteration() {
        for (int i = 0; i < this.linesToCalculate.length; i ++) {
            try {
                this.updateLine(this.linesToCalculate[i]);
            } catch (IOException err) {
                err.printStackTrace();
            }
        }
    }

    // Itera sobre os pontos recebidos e calcula uma nova linha
    public List<Color> calculateLine() {
        int i = 1;
        List<Color> newLine = new ArrayList<Color>();

        for (int j = 1; j < this.size - 1; j ++) {
            // Calcula a média dos pontos
            Color color = Stencil.avgColor(
                map[i][j],
                map[i - 1][j],
                map[i][j -1],
                map[i + 1][j],
                map[i][j + 1]
            );
            newLine.add(color);
        }
        return newLine;
    }

    // Método responsável por solicitar um conjunto de pontos para calcular uma linha e devolvê-la ao servidor
    public void updateLine(int line) throws IOException, NumberFormatException {
        String response = "";

        // Envia mensagem solicitando uma determinada linha
        this.output.writeBytes("request line:" + String.valueOf(line) + "\n");

        // Enquanto não receber uma mensagem "end", recebe os pontos e guarda em uma matriz auxiliar
        while (!response.equals("end")) {
            if (response.matches("([0-9]+( )*){5}")) {
                Map<String, Object> point = Stencil.lineToPoint(response, this.size);
                int xCoord = (int) point.get("x");
                int yCoord = (int) point.get("y");
                Color color = (Color) point.get("color");
                this.map[(xCoord - line) + 1][yCoord] = color;
            }
            response = this.input.readLine();
        }

        // Chama a função de calcular uma nova linha a partir dos dados recebidos
        List<Color> newLine = this.calculateLine();

        // Envia de volta a nova linha calculada para o servidor
        for (int j = 0; j < newLine.size(); j ++) {
            this.output.writeBytes(Stencil.pointToLine(line, j + 1, newLine.get(j)) + "\n");
        }

        this.output.writeBytes("end\n");
    }

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
