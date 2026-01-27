package br.com.modware.transrv.service;

import br.com.modware.transrv.model.Employee;
import br.com.modware.transrv.model.EmployeeWAContact;
import br.com.modware.transrv.model.WAContact;
import br.com.modware.transrv.repository.EmployeeRepository;
import br.com.modware.transrv.repository.EmployeeWAContactRepository;
import br.com.modware.transrv.repository.WAContactRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class EmployeeWAContactService {

    private final EmployeeWAContactRepository employeeWAContactRepository;
    private final EmployeeRepository employeeRepository;
    private final WAContactRepository waContactRepository;

    public EmployeeWAContactService(
            EmployeeWAContactRepository employeeWAContactRepository,
            EmployeeRepository employeeRepository,
            WAContactRepository waContactRepository) {
        this.employeeWAContactRepository = employeeWAContactRepository;
        this.employeeRepository = employeeRepository;
        this.waContactRepository = waContactRepository;
    }

    @Transactional(readOnly = true)
    public List<EmployeeWAContact> findByEmployee(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Funcionário não encontrado com ID: " + employeeId));
        return employeeWAContactRepository.findByEmployee(employee);
    }

    @Transactional
    public EmployeeWAContact linkWAContact(Long employeeId, Long waContactId) {
        
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Funcionário não encontrado com ID: " + employeeId));

        WAContact waContact = waContactRepository.findById(waContactId)
                .orElseThrow(() -> new IllegalArgumentException("Contato WhatsApp não encontrado com ID: " + waContactId));

        Optional<EmployeeWAContact> existing = employeeWAContactRepository
                .findByEmployeeAndWaContact(employee, waContact);
        
        if (existing.isPresent()) {
            throw new IllegalArgumentException(
                    "Já existe um vínculo entre o funcionário e o contato WhatsApp");
        }

        EmployeeWAContact employeeWAContact = new EmployeeWAContact();
        employeeWAContact.setEmployee(employee);
        employeeWAContact.setWaContact(waContact);

        return employeeWAContactRepository.save(employeeWAContact);
    }

    @Transactional
    public void unlinkWAContact(Long employeeId, Long waContactId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Funcionário não encontrado com ID: " + employeeId));

        WAContact waContact = waContactRepository.findById(waContactId)
                .orElseThrow(() -> new IllegalArgumentException("Contato WhatsApp não encontrado com ID: " + waContactId));

        EmployeeWAContact employeeWAContact = employeeWAContactRepository
                .findByEmployeeAndWaContact(employee, waContact)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Vínculo não encontrado entre o funcionário e o contato WhatsApp"));

        employeeWAContactRepository.delete(employeeWAContact);
    }
}