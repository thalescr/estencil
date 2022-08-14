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

        // Salva o rank do processo atual, a seção, o índice de início e fim no
        // conjunto de trabalho, declara um vetor int de tamanho * 3 para a mensagem
        int[] linesToCalculate;
        int myRank = MPI.COMM_WORLD.getRank();
        int nClients = MPI.COMM_WORLD.getSize();
        int section = size / (nClients - 1);
        int startIndex = ((myRank - 1) * section) + 1;
        int stopIndex = startIndex + section;
        int[] message = new int[size * 3];
        Color[][] auxMap = Stencil.initMap(size);

        // Inicia o cronômetro
        StopWatch sw = new StopWatch();

        // Roda cada iteração
        for (int iter = 0; iter < 1000; iter ++) {
            // Servidor envia as linhas para cada cliente
            if (myRank == 0) {
                for (int client = 1; client < nClients; client ++) {
                    // Calcula o índice de início e fim do cliente
                    int clientStartIndex = ((client - 1) * section) + 1;
                    int clientStopIndex = clientStartIndex + section;

                    // Envia a linha toda como um vetor de inteiros
                    for (int i = clientStartIndex; i < clientStopIndex; i ++) {
                        for (int j = 0; j < size; j ++) {
                            message[j * 3] = map[i][j].getRed();
                            message[(j * 3) + 1] = map[i][j].getGreen();
                            message[(j * 3) + 2] = map[i][j].getBlue();
                        }

                        MPI.COMM_WORLD.send(message, section * 3, MPI.INT, client, i);
                    }
                }

            // Cliente recebe as linhas do servidor
            } else {
                for (int i = startIndex; i < stopIndex; i ++) {
                    // Recebe a linha toda como um vetor de inteiros
                    MPI.COMM_WORLD.recv(message, section * 3, MPI.INT, 0, i);

                    for (int j = 0; j < size; j ++) {
                        map[i][j] = new Color(
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
            }

            // Cliente envia as linhas calculadas
            if (myRank != 0) {

                // Também envia como um vetor de inteiros
                for (int i = startIndex; i < stopIndex; i ++) {
                    for (int j = 0; j < size; j ++) {
                        message[j * 3] = auxMap[i][j].getRed();
                        message[(j * 3) + 1] = auxMap[i][j].getGreen();
                        message[(j * 3) + 2] = auxMap[i][j].getBlue();
                    }

                    MPI.COMM_WORLD.send(message, section * 3, MPI.INT, 0, i);
                }
            // Servidor recebe as linhas calculadas
            } else {
                for (int client = 1; client < nClients; client ++) {
                    // Calcula o índice de início e fim do cliente
                    int clientStartIndex = ((client - 1) * section) + 1;
                    int clientStopIndex = clientStartIndex + section;

                    // Também recebe como um vetor de inteiros
                    for (int i = clientStartIndex; i < clientStopIndex; i ++) {
                        MPI.COMM_WORLD.recv(message, section * 3, MPI.INT, client, i);

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

        if (myRank == 0) {
            // Para o cronômetro e printa o tempo decorrido
            sw.printElapsedTime();

            // Exporta o resultado em um arquivo
            Stencil.outputToFile(map, "output.dat");
        }

        MPI.Finalize();
    }
}
