package br.com.modware.transrv.service;

import br.com.modware.transrv.model.AlertTerm;
import br.com.modware.transrv.model.Employee;
import br.com.modware.transrv.model.EmployeeAlertTerm;
import br.com.modware.transrv.repository.AlertTermRepository;
import br.com.modware.transrv.repository.EmployeeAlertTermRepository;
import br.com.modware.transrv.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


@Service
public class EmployeeAlertTermService {

    private final EmployeeAlertTermRepository employeeAlertTermRepository;
    private final EmployeeRepository employeeRepository;
    private final AlertTermRepository alertTermRepository;

    public EmployeeAlertTermService(
            EmployeeAlertTermRepository employeeAlertTermRepository,
            EmployeeRepository employeeRepository,
            AlertTermRepository alertTermRepository) {
        this.employeeAlertTermRepository = employeeAlertTermRepository;
        this.employeeRepository = employeeRepository;
        this.alertTermRepository = alertTermRepository;
    }

    @Transactional(readOnly = true)
    public List<EmployeeAlertTerm> findByEmployee(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Funcionário não encontrado com ID: " + employeeId));
        return employeeAlertTermRepository.findByEmployee(employee);
    }

    @Transactional
    public EmployeeAlertTerm linkAlertTerm(Long employeeId, Long alertTermId, Integer priorityLevel) {
       
        if (priorityLevel == null || priorityLevel < 1 || priorityLevel > 3) {
            throw new IllegalArgumentException("Nível de prioridade deve ser 1, 2 ou 3");
        }

        
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Funcionário não encontrado com ID: " + employeeId));

        
        AlertTerm alertTerm = alertTermRepository.findById(alertTermId)
                .orElseThrow(() -> new IllegalArgumentException("Termo de alerta não encontrado com ID: " + alertTermId));

        
        Optional<EmployeeAlertTerm> existing = employeeAlertTermRepository
                .findByEmployeeAndAlertTermAndPriorityLevel(employee, alertTerm, priorityLevel);
        
        if (existing.isPresent()) {
            throw new IllegalArgumentException(
                    "Já existe um vínculo entre o funcionário e o alerta com o mesmo nível de prioridade");
        }

        
        EmployeeAlertTerm employeeAlertTerm = new EmployeeAlertTerm();
        employeeAlertTerm.setEmployee(employee);
        employeeAlertTerm.setAlertTerm(alertTerm);
        employeeAlertTerm.setPriorityLevel(priorityLevel);

        return employeeAlertTermRepository.save(employeeAlertTerm);
    }

    @Transactional
    public void unlinkAlertTerm(Long employeeId, Long alertTermId, Integer priorityLevel) {
       
        if (priorityLevel == null || priorityLevel < 1 || priorityLevel > 3) {
            throw new IllegalArgumentException("Nível de prioridade deve ser 1, 2 ou 3");
        }

        
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Funcionário não encontrado com ID: " + employeeId));

        
        AlertTerm alertTerm = alertTermRepository.findById(alertTermId)
                .orElseThrow(() -> new IllegalArgumentException("Termo de alerta não encontrado com ID: " + alertTermId));

        
        EmployeeAlertTerm employeeAlertTerm = employeeAlertTermRepository
                .findByEmployeeAndAlertTermAndPriorityLevel(employee, alertTerm, priorityLevel)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Vínculo não encontrado entre o funcionário e o alerta com o nível de prioridade especificado"));

        
        employeeAlertTermRepository.delete(employeeAlertTerm);
    }
}