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
import java.lang.IllegalArgumentException;

// Classe Stencil define vários métodos úteis para o algoritmo
public class Stencil {

    // Inicializa uma imagem de um dado tamanho
    public static Color[][] initMap(int size) {
        size = size + 2; // Adiciona as bordas da imagem
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

    // Calcula a média de dados 5 valores
    public static int calculateAvg(
        int value1,
        int value2,
        int value3,
        int value4,
        int value5
    ) {
        return (value1 + value2 + value3 + value4 + value5) / 5;
    }

    // Calcula a cor média a partir das 5 cores adjacentes
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

    // Escreve uma imagem em um arquivo de texto
    public static void outputToFile(Color[][] map, String filename) {
        int size = map.length;
        try {
            FileOutputStream fileOutput = new FileOutputStream(filename);
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

    // Transforma um ponto (coordenadas e cor) em uma string
    public static String pointToLine(int x, int y, Color color) {
        return String.valueOf(x) + " " +
            String.valueOf(y) + " " +
            String.valueOf(color.getRed()) + " " +
            String.valueOf(color.getGreen()) + " " +
            String.valueOf(color.getBlue());
    }

    // Transforma uma string de 5 inteiros em um ponto com coordenadas e uma cor
    public static Map<String, Object> lineToPoint(String line) {
        if (line == null) {
            throw new IllegalArgumentException();
        }

        Map<String, Object> point = new HashMap<String, Object>();
        String[] splittedLine = line.split(" ");
        if (splittedLine.length == 5) {
            int xCoord = Integer.parseInt(splittedLine[0]);
            int yCoord = Integer.parseInt(splittedLine[1]);
    
            int red = Integer.parseInt(splittedLine[2]);
            int green = Integer.parseInt(splittedLine[3]);
            int blue = Integer.parseInt(splittedLine[4]);

            // Validação dos valores da cor
            if (red > 255 || green > 255 || blue > 255) {
                System.out.println("Erro ao ler linha: " + line);
                throw new IllegalArgumentException();
            }

            // Retorna as coordenadas e a cor em uma estrutura de dados chamada Map
            point.put("x", xCoord);
            point.put("y", yCoord);
            point.put("color", new Color(red, green, blue));
            return point;

        } else {
            System.out.println("Linha faltando argumentos: " + String.valueOf(line));
            throw new IllegalArgumentException();
        }
    }

    // Lê o arquivo de entrada
    public static Map<String, Object> inputFileToMap(String filename) {
        Color[][] map = new Color[1][1];
        List<int[]> fixedPoints = new ArrayList<int[]>();
        Map<String, Object> result = new HashMap<String, Object>();

        try {
            // Cria um leitor do arquivo "input.dat"
            FileReader fileReader = new FileReader(filename);
            BufferedReader reader = new BufferedReader(fileReader);
            String currentLine = reader.readLine();

            // Guarda o tamanho e o número de pontos fixos
            int size = Integer.parseInt(currentLine.split(" ")[0]);
            int nFixedPoints = Integer.parseInt(currentLine.split(" ")[1]);
            map = initMap(size);

            // Itera sobre o número de pontos fixos ao ler as linhas seguintes
            for (int i = 0; i < nFixedPoints; i = i + 1) {
                // Converte a string de 5 inteiros em um par de coordenadas e uma cor
                Map<String, Object> point = lineToPoint(reader.readLine());
                int xCoord = (int) point.get("x");
                int yCoord = (int) point.get("y");

                // Insere o ponto fixo lido na imagem
                map[xCoord][yCoord] = (Color) point.get("color");

                // Adiciona as coordenadas do ponto fixo na lista de pontos fixos
                int[] coord = {xCoord, yCoord};
                fixedPoints.add(coord);
            }
            reader.close();
        } catch (IOException | NumberFormatException  err) {
            err.printStackTrace();
        }

        // Converte a lista de pontos fixos em uma matriz de inteiros
        int[][] fixedPointsArr = new int[fixedPoints.size()][2];
        Arrays.setAll(fixedPointsArr, fixedPoints::get);

        // Retorna os valores em uma estrutura de dados chamada Map
        result.put("map", map);
        result.put("fixedPoints", fixedPointsArr);
        return result;
    }

    // Transforma uma cor em uma string do formato "< R, G, B >"
    public static String colorToString(Color color) {
        return (
            "< " + String.valueOf(color.getRed()) + ", " +
            String.valueOf(color.getGreen()) + ", " +
            String.valueOf(color.getBlue()) + " > "
        );
    }

    // Verifica se uma coordenada está presenta em uma lista de pontos fixos
    public static boolean isPointFixed(int[][] fixedPoints, int xCoord, int yCoord) {
        List<int[]> points = Arrays.asList(fixedPoints);
        return points.stream().filter(o -> o[0] == xCoord && o[1] == yCoord).findFirst().isPresent();
    }

    // Printa uma cor
    public static void printColor(Color color) {
        System.out.println(colorToString(color));
    }
}
