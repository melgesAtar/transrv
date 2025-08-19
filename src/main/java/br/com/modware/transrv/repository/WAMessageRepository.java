package br.com.modware.transrv.repository;

import br.com.modware.transrv.model.WAMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WAMessageRepository extends JpaRepository<WAMessage, Long> {

    Optional<WAMessage> findByEvolutionMessageId(String id);
}
