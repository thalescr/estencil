import java.io.IOException;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.List;
import java.util.ArrayList;
import java.awt.Color;

public class Main {
    public static void main(String[] argv) throws NumberFormatException, IOException {
        if (argv != null && argv.length != 1) {
            System.out.println("Insira a quantidade de clientes");
            return;
        }
        int nClients = Integer.parseInt(argv[0]);
        if (nClients != 2 && nClients != 4 && nClients != 8 && nClients != 16) {
            System.out.println("A quantidade de clientes deve ser igual a 2, 4, 8 ou 16.");
            return;
        }

        Map<String, Object> result = Stencil.inputFileToMap();
        Color[][] map = (Color[][]) result.get("map");
        int[][] fixedPoints = (int[][]) result.get("fixedPoints");
        int size = map.length;

        Server server = new Server(size, map, fixedPoints, nClients);
        server.start();
        List<Client> clients = new ArrayList<Client>();

        for (int i = 0; i < nClients; i ++) {
            int section = (size - 2) / nClients;
            int[] linesToCalculate = IntStream.range((i * section) + 1, (i + 1) * section).toArray();
            Client client = new Client(size, linesToCalculate);
            clients.add(client);
        }

        StopWatch stopWatch = new StopWatch();

        for (int iter = 0; iter < 10000; iter ++) {
            List<Thread> threads = new ArrayList<Thread>();
            clients.forEach(client -> {
                Thread thread = new Thread(() -> {
                    client.calculateIteration();
                });
                thread.start();
                threads.add(thread);
            });

            threads.forEach(thread -> {
                try {
                    thread.join();
                } catch (InterruptedException err) {
                    err.printStackTrace();
                }
            });
        }

        clients.forEach(client -> {
            try {
                client.socket.close();
            } catch (IOException err) {
                err.printStackTrace();
            }
        });

        stopWatch.printElapsedTime();

        Stencil.outputToFile(server.map);
    }
}
