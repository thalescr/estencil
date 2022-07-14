public class StopWatch {
    public long startedTime;

    public StopWatch() {
        this.startedTime = System.currentTimeMillis();
    }

    public void printElapsedTime() {
        long elapsedTime = System.currentTimeMillis() - this.startedTime;
        System.out.println("TIME: " + String.valueOf(elapsedTime));
    }
}
