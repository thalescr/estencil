import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.List;

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
                if (message.equals("request line")) {
                    int lineNumber = 1;
                    this.output.writeBytes("line:" + lineNumber + "\n");
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
            } catch (IOException err) {
                err.printStackTrace();
            }
        }
    }
}
