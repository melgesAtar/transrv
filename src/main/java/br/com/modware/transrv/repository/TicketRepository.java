package br.com.modware.transrv.repository;

import br.com.modware.transrv.model.AlertTerm;
import br.com.modware.transrv.model.Ticket;
import br.com.modware.transrv.model.WAContact;
import br.com.modware.transrv.model.WAGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {



        boolean existsByContactAndWaGroupAndAlertTermAndStatus(
                WAContact contact,
                WAGroup waGroup,
                AlertTerm alertTerm,
                Ticket.Status status
        );



}
