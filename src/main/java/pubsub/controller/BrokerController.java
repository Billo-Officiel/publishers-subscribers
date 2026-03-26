package pubsub.controller;

import pubsub.service.BrokerService;
import pubsub.model.Broker;

import org.springframework.http.HttpStatus;
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
    public Map<String, BrokerService.BrokerInfo> listBrokers() {
        return brokerService.getBrokerStatuses();
    }

    public record CreateBrokerRequest(int capacity) {}

    @PostMapping("/{topic}")
    public ResponseEntity<String> createBroker(@PathVariable String topic, @RequestBody CreateBrokerRequest request) {
        brokerService.createBroker(topic, request.capacity());
        return ResponseEntity.ok("Courtier pour le sujet " + topic + " créé avec la capacité " + request.capacity());
    }

    public record PublishRequest(String publisherName) {}

    @PostMapping("/{topic}/publish")
    public ResponseEntity<String> publish(@PathVariable String topic, @RequestBody PublishRequest request) {
        Broker broker = brokerService.getBroker(topic);
        if (broker == null) {
            return ResponseEntity.notFound().build();
        }
        boolean success = broker.tryPublish(request.publisherName());
        if (!success) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body("Erreur : La file du courtier est pleine (Capacité : " + broker.getCapacity() + ")");
        }
        return ResponseEntity.ok("Message publié par " + request.publisherName() + " sur " + topic);
    }

    @GetMapping("/{topic}/subscribe")
    public ResponseEntity<String> subscribe(@PathVariable String topic, @RequestParam String subscriberName) {
        Broker broker = brokerService.getBroker(topic);
        if (broker == null) {
            return ResponseEntity.notFound().build();
        }
        boolean success = broker.trySubscribe(subscriberName);
        if (!success) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body("Erreur : La file du courtier est vide. Aucun message à consommer.");
        }
        return ResponseEntity.ok("Message consommé par " + subscriberName + " depuis " + topic);
    }
}
