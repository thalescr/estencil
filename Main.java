import java.io.IOException;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

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
        Map<String, Object> result = Stencil.inputFileToMap("input.dat");

        // Salva o mapa de bits, os pontos fixos e o tamanho
        Color[][] map = (Color[][]) result.get("map");
        int[][] fixedPoints = (int[][]) result.get("fixedPoints");
        int size = map.length - 2;

        // Instancia e inicializa o servidor
        Server server = new Server(size, map, fixedPoints, nClients);
        server.start();

        // Cria uma lista de clientes
        List<Client> clients = new ArrayList<Client>();
        for (int i = 0; i < nClients; i ++) {
            // Instancia o cliente
            Client client = new Client();
            client.start();
            clients.add(client);
        }
    }
}
