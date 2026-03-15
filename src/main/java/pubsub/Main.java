package pubsub;

import java.util.ArrayList;
import java.util.List;

public class Main {

    private static final String[] APP_TYPES = {"i", "t"};

    public static void main(String[] args) throws InterruptedException {
        int n = Integer.parseInt(System.getProperty("n", "2"));
        int p = Integer.parseInt(System.getProperty("p", "2"));
        int s = Integer.parseInt(System.getProperty("s", "3"));
        int t = Integer.parseInt(System.getProperty("t", "100"));

        System.out.println("Démarrage : n=" + n + " p=" + p + " s=" + s + " t=" + t + "ms");
        System.out.println("---");

        List<Thread> threads = new ArrayList<>();
        
        for (String app : APP_TYPES) {
            Broker broker = new Broker(n);

            for (int id = 1; id <= p; id++) {
                Thread pubThread = new Thread(new Publisher(app, id, broker));
                pubThread.setDaemon(true);
                threads.add(pubThread);
            }

            for (int id = 1; id <= s; id++) {
                Thread subThread = new Thread(new Subscriber(app, id, broker));
                subThread.setDaemon(true);
                threads.add(subThread);
            }
        }

        for (Thread thread : threads) {
            thread.start();
        }

        Thread.sleep(t);

        System.out.println("---");
        System.out.println("Fin de l'exécution après " + t + "ms.");
        System.exit(0);
    }
}