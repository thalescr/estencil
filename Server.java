import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Server extends Thread {
    int size;
    ServerSocket serverSocket;
    Color[][] map;
    int[][] fixedPoints;
    int nClients;

    public void run() {
        List<ServerWorker> workers = new ArrayList<ServerWorker>();

        // Inicializa o cronometro para marcar o tempo
        StopWatch sw = new StopWatch();

        // Aguarda a conexão de n clientes
        while (workers.size() < this.nClients) {
            try {
                // Quando um cliente se conectar, cria um socket e instancia um
                // objeto ServerWorker que terá os métodos para se comunicar com
                // o cliente.
                Socket socket = this.serverSocket.accept();
                ServerWorker worker = new ServerWorker(this, socket);

                // Calcula a seção de trabalho do cliente e envia a ele os pontos fixos
                int i = workers.size();
                int section = this.size / this.nClients;

                int startIndex = (i * section) + 1;
                int stopIndex = ((i + 1) * section) + 1;

                worker.linesToCalculate = IntStream.range(startIndex, stopIndex).boxed().collect(Collectors.toList());
                worker.sendInitialInfo();

                workers.add(worker);
            } catch (IOException err) {
                err.printStackTrace();
            }
        }

        // Realiza as iterações do algoritmo de Estêncil
        for (int iter = 0; iter < 1000; iter ++) {
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

        // Printa o tempo decorrido
        sw.printElapsedTime();

        // Exporta o resultado para o arquivo output.dat
        Stencil.outputToFile(this.map, "output.dat");
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
