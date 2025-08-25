package br.com.modware.transrv.repository;

import br.com.modware.transrv.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
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
}


