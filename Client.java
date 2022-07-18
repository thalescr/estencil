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

    public void calculateIteration() {
        for (int i = 0; i < this.linesToCalculate.length; i ++) {
            try {
                this.updateLine(this.linesToCalculate[i]);
            } catch (IOException err) {
                err.printStackTrace();
            }
        }
    }

    public List<Color> calculateLine() {
        int i = 1;
        List<Color> newLine = new ArrayList<Color>();
        for (int j = 1; j < this.size - 1; j ++) {
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

    public void updateLine(int line) throws IOException, NumberFormatException {
        String response = "";

        this.output.writeBytes("request line:" + String.valueOf(line) + "\n");
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

        List<Color> newLine = this.calculateLine();
        for (int j = 0; j < newLine.size(); j ++) {
            this.output.writeBytes(Stencil.pointToLine(line, j + 1, newLine.get(j)) + "\n");
        }
        this.output.writeBytes("end\n");
    }

    public Client(int size, int[] linesToCalculate) throws IOException {
        this.size = size;
        this.linesToCalculate = linesToCalculate;
        this.map = new Color[3][this.size];
        this.map[0][0] = new Color(127, 127, 127);
        this.map[1][0] = new Color(127, 127, 127);
        this.map[2][0] = new Color(127, 127, 127);
        this.map[0][this.size - 1] = new Color(127, 127, 127);
        this.map[1][this.size - 1] = new Color(127, 127, 127);
        this.map[2][this.size - 1] = new Color(127, 127, 127);

        this.socket = new Socket("localhost", 5564);  
        this.output = new DataOutputStream(this.socket.getOutputStream());
        this.input = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
    }
}
