import java.util.Map;

public class Sequential {
    public static Color[][] sequential(Color[][] input, int[][] fixedPoints, int iter) {
        int SIZE = input.length;
        int iterCount = 0;

        while (iter > iterCount) {
            Color[][] aux = new Color[input.length][input[0].length];
            for (int i = 1; i < SIZE - 1; i = i + 1) {
                for (int j = 1; j < SIZE - 1; j = j + 1) {
                    if (Stencil.isPointFixed(fixedPoints, i, j)) {
                        continue;
                    }
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
                    if (aux[i][j] != null) {
                        input[i][j] = aux[i][j];
                    }
                }
            }

            iterCount = iterCount + 1;
        }
        return input;
    }

    public static void main(String[] args) {
        Map<String, Object> result = Stencil.inputFileToMap("input.dat");
        Color[][] map = (Color[][]) result.get("map");
        int[][] fixedPoints = (int[][]) result.get("fixedPoints");
        StopWatch stopWatch = new StopWatch();
        sequential(map, fixedPoints, 1000);
        stopWatch.printElapsedTime();
        Stencil.outputToFile(map, "output.dat");
    }
}
