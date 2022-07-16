import java.awt.Color;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

public class Sequential {
    public static boolean isPointFixed(List<int[]> fixedPoints, int xCoord, int yCoord) {
        return fixedPoints.stream().filter(o -> o[0] == xCoord && o[1] == yCoord).findFirst().isPresent();
    }

    public static Color[][] sequential(Color[][] input, int[][] fixedPoints, int iter) {
        int SIZE = input.length;
        int iterCount = 0;
        // for (int i = 0; i < fixedPoints.length; i = i + 1) {
        //     String aaaaa = "";
        //     for (int j = 0; j < fixedPoints[i].length; j = j + 1) {
        //         aaaaa = aaaaa + " " + String.valueOf(fixedPoints[i][j]);
        //     }
        //    System.out.println("Line: " + aaaaa);
        // }
        while (iter > iterCount) {
            Color[][] aux = new Color[input.length][input[0].length];
            for (int i = 1; i < SIZE - 1; i = i + 1) {
                for (int j = 1; j < SIZE - 1; j = j + 1) {
                    if (isPointFixed(Arrays.asList(fixedPoints), i, j)) {
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
        Map<String, Object> result = Stencil.inputFileToMap();
        Color[][] map = (Color[][]) result.get("map");
        int[][] fixedPoints = (int[][]) result.get("fixedPoints");
        StopWatch stopWatch = new StopWatch();
        sequential(map, fixedPoints, 10000);
        stopWatch.printElapsedTime();
        Stencil.outputToFile(map);
    }
}
