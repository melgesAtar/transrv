package br.com.modware.transrv.service;

import br.com.modware.transrv.dto.dashboard.NotificationDTO;
import br.com.modware.transrv.model.TicketNotification;
import br.com.modware.transrv.repository.TicketNotificationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationsService {

    private final TicketNotificationRepository repository;

    public NotificationsService(TicketNotificationRepository repository) {
        this.repository = repository;
    }

    public List<NotificationDTO> getRecent() {
        return repository.findTop50ByOrderByNotifiedAtDesc().stream().map(this::map).collect(Collectors.toList());
    }

    private NotificationDTO map(TicketNotification tn) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(tn.getId());
        dto.setTicketId(tn.getTicket() != null ? tn.getTicket().getId() : null);
        dto.setGroupName(tn.getTicket() != null && tn.getTicket().getWaGroup() != null ? tn.getTicket().getWaGroup().getGroupName() : null);
        dto.setAlertCode(tn.getTicket() != null && tn.getTicket().getAlertTerm() != null ? tn.getTicket().getAlertTerm().getCode() : null);
        dto.setEmployeeName(tn.getEmployee() != null ? tn.getEmployee().getName() : null);
        dto.setEmployeePhone(tn.getEmployee() != null && tn.getEmployee().getWaContact() != null ? tn.getEmployee().getWaContact().getPhoneNumber() : null);
        dto.setEscalationLevel(tn.getEscalationLevel());
        dto.setNotifiedAt(tn.getNotifiedAt());
        return dto;
    }
}


