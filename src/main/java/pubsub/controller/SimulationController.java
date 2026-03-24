package pubsub.controller;

import pubsub.service.BrokerService;
import pubsub.model.Broker;
import pubsub.model.Publisher;
import pubsub.model.Subscriber;

import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/simulation")
public class SimulationController {

    private final BrokerService brokerService;

    public SimulationController(BrokerService brokerService) {
        this.brokerService = brokerService;
    }

    public record SimulationRequest(int n, int p, int s, int t) {}

    @PostMapping("/start")
    public String startSimulation(@RequestBody SimulationRequest req) {
        String[] APP_TYPES = {"i", "t"};
        List<Thread> threads = new ArrayList<>();
        
        for (String app : APP_TYPES) {
            Broker broker = brokerService.getBroker(app);
            if (broker == null) {
                broker = brokerService.createBroker(app, req.n());
            }

            for (int id = 1; id <= req.p(); id++) {
                Thread pubThread = new Thread(new Publisher(app, id, broker));
                pubThread.setDaemon(true);
                threads.add(pubThread);
            }

            for (int id = 1; id <= req.s(); id++) {
                Thread subThread = new Thread(new Subscriber(app, id, broker));
                subThread.setDaemon(true);
                threads.add(subThread);
            }
        }

        for (Thread thread : threads) {
            thread.start();
        }

        return "Simulation started with parameters (n=" + req.n() + ", p=" + req.p() + ", s=" + req.s() + ")";
    }
}
