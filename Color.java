public class Color {
    private byte red;
    private byte green;
    private byte blue;

    private byte intToByte(int number) throws NumberFormatException {
        if (number < 0 || number > 255) {
            throw new NumberFormatException();
        }
        return (byte) (number - 128);
    }

    private int byteToInt(byte number) {
        return (int) number + 128;
    }

    public Color(int red, int green, int blue) {
        try {
            this.red = this.intToByte(red);
            this.green = this.intToByte(green);
            this.blue = this.intToByte(blue);
        } catch (NumberFormatException err) {
            err.printStackTrace();
        }

    }

    public int getRed() {
        return byteToInt(this.red);
    }

    public int getGreen() {
        return byteToInt(this.green);
    }

    public int getBlue() {
        return byteToInt(this.blue);
    }
}
