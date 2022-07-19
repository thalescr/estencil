import java.io.IOException;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.List;
import java.util.ArrayList;
import java.awt.Color;

public class Main {
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

        // Lê o arquivo de entrada input.dat
        Map<String, Object> result = Stencil.inputFileToMap();

        // Salva o mapa de bits, os pontos fixos e o tamanho
        Color[][] map = (Color[][]) result.get("map");
        int[][] fixedPoints = (int[][]) result.get("fixedPoints");
        int size = map.length;

        // Instancia e inicializa o servidor
        Server server = new Server(size, map, fixedPoints, nClients);
        server.start();

        // Cria uma lista de clientes
        List<Client> clients = new ArrayList<Client>();
        for (int i = 0; i < nClients; i ++) {
            // Calcula os números das linhas que cada cliente irá calcular e salva em linesToCalculate
            int section = (size - 2) / nClients;
            int[] linesToCalculate = IntStream.range((i * section) + 1, (i + 1) * section).toArray();

            // Instancia o cliente enviando o tamanho da imagem e as linhas que o cliente ficará responsável por calcular
            Client client = new Client(size, linesToCalculate);
            clients.add(client);
        }

        // Inicializa o cronômetro para medir o tempo de execução
        StopWatch stopWatch = new StopWatch();

        // Laço de iterações do estencil
        for (int iter = 0; iter < 10; iter ++) {
            // Coloca cada cliente para calcular sua parte da imagem
            List<Thread> threads = new ArrayList<Thread>();
            clients.forEach(client -> {
                Thread thread = new Thread(() -> {
                    client.calculateIteration();
                });
                thread.start();
                threads.add(thread);
            });

            // Aguarda todos os clientes terminarem de calcular suas iterações
            threads.forEach(thread -> {
                try {
                    thread.join();
                } catch (InterruptedException err) {
                    err.printStackTrace();
                }
            });
        }

        // Fecha os sockets dos clientes
        clients.forEach(client -> {
            try {
                client.socket.close();
            } catch (IOException err) {
                err.printStackTrace();
            }
        });

        // Mostra o tempo decorrido
        stopWatch.printElapsedTime();

        // Exporta a imagem
        Stencil.outputToFile(server.map);
    }
}
