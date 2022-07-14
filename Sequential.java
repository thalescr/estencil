import java.awt.Color;

public class Sequential {
    public static Color[][] sequential(Color[][] input, int iter) {
        int SIZE = input.length;
        int iterCount = 0;
        while (iter > iterCount) {
            Color[][] aux = new Color[input.length][input[0].length];

            for (int i = 1; i < SIZE - 1; i = i + 1) {
                for (int j = 1; j < SIZE - 1; j = j + 1) {
                    aux[i][j] = Stencil.avgColor(
                        input[i][j],
                        input[i-1][j],
                        input[i][j-1],
                        input[i+1][j],
                        input[i][j+1]
                    );
                }
            }

            for (int i = 1; i < SIZE - 1; i = i + 1) {
                for (int j = 1; j < SIZE - 1; j = j + 1) {
                    input[i][j] = aux[i][j];
                }
            }

            iterCount = iterCount + 1;
        }
        return input;
    }

    public static void main(String[] args) {
        int SIZE = 768;
        Color[][] map = new Color[SIZE][SIZE];

        for (int i = 0; i < SIZE; i = i + 1) {
            for (int j = 0; j < SIZE; j = j + 1) {
                if (i == 0 || j == 0 || i == SIZE - 1 || j == SIZE - 1) {
                    map[i][j] = new Color(127, 127, 127);
                } else {
                    map[i][j] = new Color(0, 0, 0);
                }
            }
        }

        StopWatch stopWatch = new StopWatch();
        sequential(map, 1000);
        Stencil.outputToFile(map);
        stopWatch.printElapsedTime();
    }
}
