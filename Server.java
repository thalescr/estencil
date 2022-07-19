import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.awt.Color;

public class Server extends Thread {
    int size;
    ServerSocket serverSocket;
    List<ServerThread> threads = new ArrayList<ServerThread>();
    Color[][] map;
    int[][] fixedPoints;
    int nClients;

    public void run() {
        while (this.threads.size() < this.nClients) {
            try {
                Socket socket = this.serverSocket.accept();
                ServerThread thread = new ServerThread(this, socket);
                thread.start();
                this.threads.add(thread);
            } catch (IOException err) {
                err.printStackTrace();
            }
        }
    }

    public Server(int size, Color[][] map, int[][] fixedPoints, int nClients) throws IOException {
        this.size = size;
        this.map = map;
        this.fixedPoints = fixedPoints;
        this.nClients = nClients;
        this.serverSocket = new ServerSocket(5564);
    }
}
