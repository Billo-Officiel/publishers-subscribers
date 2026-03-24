package pubsub.model;
public class Broker {

    private int count = 0;

    private final int capacity;

    public Broker(int capacity) {
        this.capacity = capacity;
    }

    public int getCount() {
        return count;
    }

    public synchronized void publish(final String publisherName) throws InterruptedException {
        while (count >= capacity) {
            wait();
        }
        System.out.println(publisherName + " CONNECT_PUB");
        System.out.println(publisherName + " PUB");
        count++;
        notifyAll();
        System.out.println(publisherName + " CLOSE_PUB");
    }

    public synchronized void subscribe(final String subscriberName) throws InterruptedException {
        while (count <= 0) {
            wait();
        }
        System.out.println(subscriberName + " CONNECT_SUB");
        System.out.println(subscriberName + " SUB");
        count--;
        notifyAll();
        System.out.println(subscriberName + " CLOSE_SUB");
    }
}