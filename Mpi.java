import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import mpi.*;

public class Mpi {

    static public void main(String[] args) throws MPIException {
        MPI.Init(args);

        // Lê o arquivo de entrada input.dat
        Map<String, Object> result = Stencil.inputFileToMap("input.dat");

        // Salva o mapa de bits, os pontos fixos e o tamanho
        Color[][] map = (Color[][]) result.get("map");
        int[][] fixedPoints = (int[][]) result.get("fixedPoints");
        int size = map.length - 2;
        int[] linesToCalculate;

        int tag = 50;
        int myRank = MPI.COMM_WORLD.getRank();
        int nClients = MPI.COMM_WORLD.getSize();
        int section = size / (nClients - 1);
        int startIndex = ((myRank - 1) * section) + 1;
        int stopIndex = (myRank * section) + 1;
        int[] message = new int[size * 3];
        Color[][] auxMap = Stencil.initMap(size);

        for (int iter = 0; iter < 3; iter ++) {
            // Servidor envia as linhas para cada cliente
            if (myRank == 0) {
                for (int client = 1; client < nClients; client ++) {
                    for (int i = section * (client - 1); i < section * client; i ++) {
                        for (int j = 0; j < size; j ++) {
                            message[j * 3] = map[i][j].getRed();
                            message[(j * 3) + 1] = map[i][j].getGreen();
                            message[(j * 3) + 2] = map[i][j].getBlue();
                        }

                        MPI.COMM_WORLD.send(message, section * 3, MPI.INT, client, tag);
                    }
                }

            // Cliente recebe as linhas do servidor
            } else {
                for (int i = 0; i < section; i ++) {
                    MPI.COMM_WORLD.recv(message, section * 3, MPI.INT, 0, tag);

                    for (int j = 0; j < size; j ++) {
                        map[i + (startIndex - 1)][j] = new Color(
                            message[j * 3],
                            message[(j * 3) + 1],
                            message[(j * 3) + 2]
                        );
                    }
                }

                // Cliente calcula as linhas em seu conjunto de trabalho
                for (int i = startIndex; i < stopIndex; i ++) {
                    for (int j = 1; j < size - 1; j ++) {
                        // Pula a iteração caso o ponto esteja no vetor de pontos fixos
                        if (Stencil.isPointFixed(fixedPoints, i, j)) {
                            auxMap[i][j] = map[i][j];
                        } else {
                            // Calcula a média dos pontos
                            auxMap[i][j] = Stencil.avgColor(
                                map[i][j],
                                map[i - 1][j],
                                map[i][j -1],
                                map[i + 1][j],
                                map[i][j + 1]
                            );
                        }
                    }
                }

                if (iter == 2) {
                    Stencil.outputToFile(auxMap, "output.dat");
                }
            }

            // Cliente envia as linhas calculadas
            if (myRank != 0) {
                for (int i = startIndex; i < stopIndex; i ++) {
                    for (int j = 0; j < size; j ++) {
                        message[j * 3] = auxMap[i][j].getRed();
                        message[(j * 3) + 1] = auxMap[i][j].getGreen();
                        message[(j * 3) + 2] = auxMap[i][j].getBlue();
                    }

                    MPI.COMM_WORLD.send(message, section * 3, MPI.INT, 0, tag);
                }
            // Servidor recebe as linhas calculadas
            } else {
                for (int client = 1; client < nClients; client ++) {
                    for (int i = section * (client - 1); i < section * client; i ++) {
                        MPI.COMM_WORLD.recv(message, section * 3, MPI.INT, client, tag);

                        for (int j = 0; j < size; j ++) {
                            map[i][j] = new Color(
                                message[j * 3],
                                message[(j * 3) + 1],
                                message[(j * 3) + 2]
                            );
                        }
                    }
                }
            }
        }

        MPI.Finalize();
    }
}
