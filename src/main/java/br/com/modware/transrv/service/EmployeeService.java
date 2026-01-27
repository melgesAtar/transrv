package br.com.modware.transrv.service;

import br.com.modware.transrv.model.Department;
import br.com.modware.transrv.model.Employee;
import br.com.modware.transrv.repository.DepartmentRepository;
import br.com.modware.transrv.repository.EmployeeRepository;
import br.com.modware.transrv.dto.employee.EmployeeCreateRequest;
import br.com.modware.transrv.dto.employee.EmployeeUpdateRequest;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;

    public EmployeeService(EmployeeRepository employeeRepository,
                          DepartmentRepository departmentRepository) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
    }

    @Transactional
    public Employee createEmployee(EmployeeCreateRequest request) {

        if (request.name() == null || request.name().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do funcionário é obrigatório");
        }


        Department department = null;
        if (request.departmentId() != null) {
            department = departmentRepository.findById(request.departmentId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Departamento com ID " + request.departmentId() + " não encontrado"));
        }

  
        Employee employee = new Employee();
        employee.setName(request.name().trim());
        employee.setDepartment(department);
        employee.setActive(true);

        return employeeRepository.save(employee);
    }

    @Transactional
    public Employee updateEmployee(Long id, EmployeeUpdateRequest request) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Funcionário não encontrado com ID: " + id));

      
        if (request.name() != null) {
            if (request.name().trim().isEmpty()) {
                throw new IllegalArgumentException("Nome do funcionário não pode ser vazio");
            }
            employee.setName(request.name().trim());
        }

       
        if (request.departmentId() != null) {
            Department department = departmentRepository.findById(request.departmentId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Departamento com ID " + request.departmentId() + " não encontrado"));
            employee.setDepartment(department);
        } else if (request.departmentId() == null && request.name() != null) {
            employee.setDepartment(null);
        }

       
        if (request.isActive() != null) {
            employee.setActive(request.isActive());
        }

        return employeeRepository.save(employee);
    }

    boolean isEmployeeAvailable(Employee e, LocalDateTime now) {
        return true;
    }

    
}
