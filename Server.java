import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

        // Inicializa o cronometro para marcar o tempo
        StopWatch sw = new StopWatch();

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

        // Printa o tempo decorrido
        sw.printElapsedTime();

        // Exporta o resultado para o arquivo output.dat
        Stencil.outputToFile(this.map, "output.dat");
    }

    // Instancia o servidor
    public Server(int nClients) throws IOException {
        // Lê o arquivo de entrada input.dat
        Map<String, Object> result = Stencil.inputFileToMap("input.dat");

        // Salva o mapa de bits, os pontos fixos e o tamanho
        this.map = (Color[][]) result.get("map");
        this.fixedPoints = (int[][]) result.get("fixedPoints");
        this.size = map.length - 2;

        // Salva o número de clientes e cria um socket do servidor
        this.nClients = nClients;
        this.serverSocket = new ServerSocket(5564);
    }

    public static void main(String[] argv) throws NumberFormatException, IOException {
        // Recebe e armazena a quantidade de clientes desejada
        if (argv != null && argv.length != 1) {
            System.out.println("Insira a quantidade de clientes");
            return;
        }
        int nClients = Integer.parseInt(argv[0]);
        if (nClients != 2 && nClients != 4 && nClients != 8 && nClients != 16) {
            System.out.println("A quantidade de clientes deve ser igual a 2, 4, 8 ou 16.");
            return;
        }

        // Instancia e inicializa o servidor
        Server server = new Server(nClients);
        server.start();
    }
}
