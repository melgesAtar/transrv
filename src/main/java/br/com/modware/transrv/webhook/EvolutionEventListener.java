package br.com.modware.transrv.webhook;

import br.com.modware.transrv.service.EvolutionEventService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/webhook/evolution")
public class EvolutionEventListener {

    private final  EvolutionEventService evolutionEventService;

    public EvolutionEventListener(EvolutionEventService evolutionEventService) {
        this.evolutionEventService = evolutionEventService;
    }

    @PostMapping("/messages-upsert")
    public ResponseEntity<String> receiveMessageUpsertEvent(@RequestBody String payloadEvent) {
        evolutionEventService.processEvent(payloadEvent);
        return ResponseEntity.ok("EVENT_RECEIVED");
    }
}
