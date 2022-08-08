// Classe StopWatch define um cronômetro
public class StopWatch {
    public long startedTime;

    // Instancia a classe guardando o valor do tempo no momento inicial
    public StopWatch() {
        this.startedTime = System.currentTimeMillis();
    }

    // Printa o tempo decorrido armazenando o tempo atual e subtraindo do tempo inicial
    public void printElapsedTime() {
        long elapsedTime = System.currentTimeMillis() - this.startedTime;
        System.out.println("TIME: " + String.valueOf((float) elapsedTime / 1000));
    }
}
