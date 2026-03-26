package pubsub.service;

import pubsub.model.Broker;

import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class BrokerService {
    private final Map<String, Broker> brokers = new ConcurrentHashMap<>();

    public Broker createBroker(String topic, int capacity) {
        Broker broker = new Broker(capacity);
        brokers.put(topic, broker);
        return broker;
    }
    
    public record BrokerInfo(int count, int capacity) {}

    public Map<String, BrokerInfo> getBrokerStatuses() {
        return brokers.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey, 
                e -> new BrokerInfo(e.getValue().getCount(), e.getValue().getCapacity())
            ));
    }
    
    public Broker getBroker(String topic) {
        return brokers.get(topic);
    }
}
