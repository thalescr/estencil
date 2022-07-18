import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.awt.Color;

public class ServerThread extends Thread {
    Server server;
    Socket socket;
    BufferedReader input;
    DataOutputStream output;

    public ServerThread(Server server, Socket socket) throws IOException {
        this.server = server;
        this.socket = socket;
        this.input = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        this.output = new DataOutputStream(this.socket.getOutputStream());
    }

    public void run() {
        String message = "";
        while (message != null) {
            try {
                message = this.input.readLine();
                if (message.startsWith("request line:")) {
                    int lineNumber = Integer.parseInt(message.split(":")[1]);
                    List<String> lines = this.server.getLine(lineNumber);
                    lines.forEach(line -> {
                        try {
                            this.output.writeBytes(line + "\n");
                        } catch (IOException err) {
                            err.printStackTrace();
                        }
                    });
                    this.output.writeBytes("end\n");
                }

                if (message.matches("([0-9]+( )*){5}")) {
                    Map<String, Object> point = Stencil.lineToPoint(message, this.server.size);
                    int xCoord = (int) point.get("x");
                    int yCoord = (int) point.get("y");
                    Color color = (Color) point.get("color");
                    this.server.map[xCoord][yCoord] = color;
                }
            } catch (IOException err) {
                err.printStackTrace();
            }
        }
    }
}
