package br.com.modware.transrv.repository;

import br.com.modware.transrv.model.WAMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface WAMessageRepository extends JpaRepository<WAMessage, Long> {

    Optional<WAMessage> findByEvolutionMessageId(String id);

    @Query("select count(m) from WAMessage m where m.sentAt >= :start and m.sentAt <= :end")
    long countBySentAtBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
