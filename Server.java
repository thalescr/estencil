import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

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

                // Envia os pontos fixos para o cliente
                worker.sendFixedPoints();

                workers.add(worker);
            } catch (IOException err) {
                err.printStackTrace();
            }
        }

        // Realiza as iterações do algoritmo de Estêncil
        for (int iter = 0; iter < 3; iter ++) {
            int currentIteration = iter;

            // Cria uma lista de threads que são executadas paralelamente
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

            // Aguarda o fim da execução das threads para seguir para a próxima iteração
            threads.forEach(thread -> {
                try {
                    thread.join();
                } catch (InterruptedException err) {
                    err.printStackTrace();
                }
            });
        }

        // Após calcular todas as iterações, envia um finish para os clientes
        // encerrarem as suas conexões.
        workers.forEach(worker -> {
            try {
                worker.sendFinish();
            } catch (IOException err) {
                err.printStackTrace();
            }
        });

        // Exporta o resultado para o arquivo output.dat
        Stencil.outputToFile(this.map);
    }

    // Instancia o servidor armazenando o mapa de pontos, os pontos fixos,
    // o tamanho do mapa, o número de clientes e o socket responsável por
    // receber as conexões dos clientes.
    public Server(int size, Color[][] map, int[][] fixedPoints, int nClients) throws IOException {
        this.size = size;
        this.map = map;
        this.fixedPoints = fixedPoints;
        this.nClients = nClients;
        this.serverSocket = new ServerSocket(5564);
    }
}
