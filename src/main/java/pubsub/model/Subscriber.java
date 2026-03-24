package pubsub.model;

public class Subscriber implements Runnable {

    private final String name;

    private final Broker broker;

    public Subscriber(final String app, final int id, final Broker broker) {
        this.name = app + ".subscriber." + id;
        this.broker = broker;
    }
    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                broker.subscribe(name);

                System.out.println(name + " CONSUME");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}