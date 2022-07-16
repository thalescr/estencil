import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.awt.Color;

public class Stencil {
    public static Color[][] initMap(int size) {
        size = size + 2;
        Color[][] map = new Color[size][size];
        for (int i = 0; i < size; i = i + 1) {
            for (int j = 0; j < size; j = j + 1) {
                if (i == 0 || j == 0 || i == size - 1 || j == size - 1) {
                    map[i][j] = new Color(127, 127, 127);
                } else {
                    map[i][j] = new Color(0, 0, 0);
                }
            }
        }
        return map;
    }

    public static int calculateAvg(
        int value1,
        int value2,
        int value3,
        int value4,
        int value5
    ) {
        return (value1 + value2 + value3 + value4 + value5) / 5;
    }

    public static Color avgColor(
        Color color1,
        Color color2,
        Color color3,
        Color color4,
        Color color5
    ) {
        Color result = new Color(
            calculateAvg(color1.getRed(), color2.getRed(), color3.getRed(), color4.getRed(), color5.getRed()),
            calculateAvg(color1.getGreen(), color2.getGreen(), color3.getGreen(), color4.getGreen(), color5.getGreen()),
            calculateAvg(color1.getBlue(), color2.getBlue(), color3.getBlue(), color4.getBlue(), color5.getBlue())
        );
        return result;
    }

    public static void outputToFile(Color[][] map) {
        int size = map.length;
        try {
            FileOutputStream fileOutput = new FileOutputStream("output.dat");
            BufferedWriter fileWriter = new BufferedWriter(new OutputStreamWriter(fileOutput));
            for (int i = 1; i < size - 1; i = i + 1) {
                String newLine = "";
                for (int j = 1; j < size - 1; j = j + 1) {
                    newLine = newLine + colorToString(map[i][j]);
                }
                fileWriter.write(newLine);
                fileWriter.newLine();
            }
            fileWriter.close();
        } catch (IOException err) {
            System.out.println("Erro ao exportar para arquivo");
            err.printStackTrace();
        }
    }

    public static Map<String, Object> inputFileToMap() {
        Color[][] map = new Color[1][1];
        List<int[]> fixedPoints = new ArrayList<int[]>();
        Map<String, Object> result = new HashMap<String, Object>();
        try {
            FileReader fileReader = new FileReader("input.dat");
            BufferedReader reader = new BufferedReader(fileReader);
            String currentLine = reader.readLine();
            int size = Integer.parseInt(currentLine.split(" ")[0]);
            int nFixedPoints = Integer.parseInt(currentLine.split(" ")[1]);
            map = initMap(size);

            for (int i = 0; i < nFixedPoints; i = i + 1) {
                currentLine = reader.readLine();
                if (currentLine == null) {
                    break;
                }

                int xCoord = Integer.parseInt(currentLine.split(" ")[0]);
                int yCoord = Integer.parseInt(currentLine.split(" ")[1]);

                int red = Integer.parseInt(currentLine.split(" ")[2]);
                int green = Integer.parseInt(currentLine.split(" ")[3]);
                int blue = Integer.parseInt(currentLine.split(" ")[4]);

                if (xCoord + 1 > size || yCoord + 1 > size || red > 255 || green > 255 || blue > 255) {
                    System.out.println("Nao foi possivel incluir o ponto fixo: " + currentLine);
                    break;
                }
                map[xCoord][yCoord] = new Color(red, green, blue);
                int[] coord = {xCoord, yCoord};
                fixedPoints.add(coord);
            }
            reader.close();
        } catch (IOException | NumberFormatException  err) {
            err.printStackTrace();
        }
        int[][] fixedPointsArr = new int[fixedPoints.size()][2];
        Arrays.setAll(fixedPointsArr, fixedPoints::get);
        result.put("map", map);
        result.put("fixedPoints", fixedPointsArr);
        return result;
    }

    public static String colorToString(Color color) {
        return (
            "< " + String.valueOf(color.getRed()) + ", " +
            String.valueOf(color.getGreen()) + ", " +
            String.valueOf(color.getBlue()) + " > "
        );
    }

    public static void printColor(Color color) {
        System.out.println(colorToString(color));
    }
}
