import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.awt.Color;

public class Stencil {
    public static int calculateAvg(
        int value1,
        int value2,
        int value3,
        int value4,
        int value5
    ) {
        return (int) Math.round(
            (float) (value1 + value2 + value3 + value4 + value5) / 5
        );
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
        int SIZE = map.length;
        try {
            FileOutputStream fileOutput = new FileOutputStream("output.txt");
            BufferedWriter fileWriter = new BufferedWriter(new OutputStreamWriter(fileOutput));
            for (int i = 0; i < SIZE; i = i + 1) {
                String newLine = "";
                for (int j = 65; j < 129; j = j + 1) {
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

    public static String colorToString(Color color) {
        return (
            '<' + String.valueOf(color.getRed()) + ',' +
            String.valueOf(color.getGreen()) + ',' +
            String.valueOf(color.getBlue()) + '>'
        );
    }

    public static void printColor(Color color) {
        System.out.println(colorToString(color));
    }
}
