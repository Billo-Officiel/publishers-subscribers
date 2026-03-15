package pubsub;

public class Publisher implements Runnable {

    private final String name;

    private final Broker broker;

    public Publisher(final String app, final int id, final Broker broker) {
        this.name = app + ".publisher." + id;
        this.broker = broker;
    }
    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                System.out.println(name + " SUPPLY");

                broker.publish(name);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}