import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.awt.Color;

public class Server extends Thread {
    int size;
    ServerSocket serverSocket;
    Color[][] map;
    int[][] fixedPoints;
    int nClients;

    public void run() {
        List<ServerWorker> workers = new ArrayList<ServerWorker>();

        // Aguarda a conexão de n clientes
        while (workers.size() < this.nClients) {
            try {
                // Quando um cliente se conectar, cria um socket e instancia um
                // objeto ServerWorker que terá os métodos para se comunicar com
                // o cliente.
                Socket socket = this.serverSocket.accept();
                ServerWorker worker = new ServerWorker(this, socket);
                workers.add(worker);
            } catch (IOException err) {
                err.printStackTrace();
            }
        }

        for (int iter = 0; iter < 3; iter ++) {
            int currentIteration = iter;
            System.out.println(String.valueOf(iter));
            List<Thread> threads = new ArrayList<Thread>();
            workers.forEach(worker -> {
                Thread newThread = new Thread(() -> {
                    try {
                        worker.callNewIteration(currentIteration);
                    }
                    catch (IOException err) {
                        err.printStackTrace();
                    }
                });
                newThread.start();
                threads.add(newThread);
            });

            threads.forEach(thread -> {
                try {
                    thread.join();
                } catch (InterruptedException err) {
                    err.printStackTrace();
                }
            });
        }

        workers.forEach(worker -> {
            try {
                worker.sendFinish();
            } catch (IOException err) {
                err.printStackTrace();
            }
        });

        Stencil.outputToFile(this.map);
    }

    public Server(int size, Color[][] map, int[][] fixedPoints, int nClients) throws IOException {
        this.size = size;
        this.map = map;
        this.fixedPoints = fixedPoints;
        this.nClients = nClients;
        this.serverSocket = new ServerSocket(5564);
    }
}
