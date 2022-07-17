import java.io.IOException;
import java.util.Map;
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
        Client client = new Client(size);

        client.requestLine();
    }
}
