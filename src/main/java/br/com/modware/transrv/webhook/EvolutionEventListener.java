package br.com.modware.transrv.webhook;

import br.com.modware.transrv.service.EvolutionEventService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.swagger.v3.oas.annotations.Hidden;

@RestController
@RequestMapping("/webhook/evolution")
@Hidden
public class EvolutionEventListener {

    private static final Logger log = LoggerFactory.getLogger(EvolutionEventListener.class);

    private final EvolutionEventService evolutionEventService;

    public EvolutionEventListener(EvolutionEventService evolutionEventService) {
        this.evolutionEventService = evolutionEventService;
    }

    @PostMapping("/messages-upsert")
    public ResponseEntity<Void> receiveMessageUpsertEvent(@RequestBody String payload) {
        log.info("Evolution payload recebido no webhook");
        try {
            evolutionEventService.processEvent(payload);
        } catch (Exception e) {
            log.error("Erro ao processar evento Evolution", e);
        }
        return ResponseEntity.ok().build();
    }
}
