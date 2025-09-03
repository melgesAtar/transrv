package br.com.modware.transrv.repository;

import br.com.modware.transrv.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Optional<Ticket> findByMessageResponsibleForOpeningTheCall_EvolutionMessageId(String evolutionMessageId);



    boolean existsByContactResponsibleForOpeningTheCallAndWaGroupAndAlertTermAndStatus(
                WAContact contactResponsibleForOpeningTheCall,
                WAGroup waGroup,
                AlertTerm alertTerm,
                Ticket.Status status
        );


    boolean existsByMessageResponsibleForOpeningTheCall(WAMessage waMessage);

    long countByStatus(Ticket.Status status);

    @Query("select count(t) from Ticket t where t.status = :status and t.createdAt >= CURRENT_DATE")
    long countOpenedToday(@org.springframework.data.repository.query.Param("status") Ticket.Status status);

    java.util.List<Ticket> findTop20ByOrderByCreatedAtDesc();
}


