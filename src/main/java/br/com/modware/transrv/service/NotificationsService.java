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
    private final br.com.modware.transrv.repository.EmployeeWAContactRepository employeeWAContactRepository;

    public NotificationsService(TicketNotificationRepository repository,
                                br.com.modware.transrv.repository.EmployeeWAContactRepository employeeWAContactRepository) {
        this.repository = repository;
        this.employeeWAContactRepository = employeeWAContactRepository;
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
        // Employee pode ter múltiplos WAContacts; coletar todos os números
        if (tn.getEmployee() != null) {
            java.util.List<String> phones = employeeWAContactRepository.findByEmployee(tn.getEmployee()).stream()
                    .filter(l -> l.getWaContact() != null && l.getWaContact().getPhoneNumber() != null)
                    .map(l -> l.getWaContact().getPhoneNumber())
                    .distinct()
                    .collect(java.util.stream.Collectors.toList());
            dto.setEmployeePhones(phones);
        }
        dto.setEscalationLevel(tn.getEscalationLevel());
        dto.setNotifiedAt(tn.getNotifiedAt());
        return dto;
    }
}


