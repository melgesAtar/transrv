package br.com.modware.transrv.repository;

import br.com.modware.transrv.model.WAMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface WAMessageRepository extends JpaRepository<WAMessage, Long> {

    Optional<WAMessage> findByEvolutionMessageId(String id);
}
