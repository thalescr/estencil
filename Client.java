import java.io.DataOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.Socket;
import java.util.Map;
import java.awt.Color;

public class Client {
    int size;
    Color[][] map;
    Socket socket;
    DataOutputStream output;
    BufferedReader input;

    public int requestLine() throws IOException, NumberFormatException {
        String response = "";
        int line = -1;

        this.output.writeBytes("request line\n");
        while (!response.equals("end")) {
            if (response.startsWith("line:")) {
                line = Integer.parseInt(response.split(":")[1]);
            }
            if (response.matches("([0-9]+( )*){5}")) {
                Map<String, Object> point = Stencil.lineToPoint(response, this.size);
                int xCoord = (int) point.get("x");
                int yCoord = (int) point.get("y");
                Color color = (Color) point.get("color");
                this.map[xCoord][yCoord] = color;
            }
            response = this.input.readLine();
        }
        return line;
    }

    public Client(int size) throws IOException {
        this.size = size;
        this.map = new Color[this.size][this.size];
        this.socket = new Socket("localhost", 5564);  
        this.output = new DataOutputStream(this.socket.getOutputStream());
        this.input = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
    }
}
