package pubsub.controller;

import pubsub.service.BrokerService;
import pubsub.model.Broker;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/brokers")
public class BrokerController {

    private final BrokerService brokerService;

    public BrokerController(BrokerService brokerService) {
        this.brokerService = brokerService;
    }

    @GetMapping
    public Map<String, Integer> listBrokers() {
        return brokerService.getBrokerStatuses();
    }

    public record CreateBrokerRequest(int capacity) {}

    @PostMapping("/{topic}")
    public ResponseEntity<String> createBroker(@PathVariable String topic, @RequestBody CreateBrokerRequest request) {
        brokerService.createBroker(topic, request.capacity());
        return ResponseEntity.ok("Broker for topic " + topic + " created with capacity " + request.capacity());
    }

    public record PublishRequest(String publisherName) {}

    @PostMapping("/{topic}/publish")
    public ResponseEntity<String> publish(@PathVariable String topic, @RequestBody PublishRequest request) {
        Broker broker = brokerService.getBroker(topic);
        if (broker == null) {
            return ResponseEntity.notFound().build();
        }
        try {
            broker.publish(request.publisherName());
            return ResponseEntity.ok("Message published by " + request.publisherName() + " to " + topic);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.internalServerError().body("Interrupted");
        }
    }

    @GetMapping("/{topic}/subscribe")
    public ResponseEntity<String> subscribe(@PathVariable String topic, @RequestParam String subscriberName) {
        Broker broker = brokerService.getBroker(topic);
        if (broker == null) {
            return ResponseEntity.notFound().build();
        }
        try {
            broker.subscribe(subscriberName);
            return ResponseEntity.ok("Message consumed by " + subscriberName + " from " + topic);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.internalServerError().body("Interrupted");
        }
    }
}
