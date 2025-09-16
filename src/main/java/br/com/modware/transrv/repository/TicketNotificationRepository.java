package br.com.modware.transrv.repository;

import br.com.modware.transrv.model.TicketNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TicketNotificationRepository extends JpaRepository<TicketNotification, Long> {
    List<TicketNotification> findTop50ByOrderByNotifiedAtDesc();
}
