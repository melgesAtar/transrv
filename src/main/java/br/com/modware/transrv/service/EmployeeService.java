package br.com.modware.transrv.service;

import br.com.modware.transrv.model.Department;
import br.com.modware.transrv.model.Employee;
import br.com.modware.transrv.repository.DepartmentRepository;
import br.com.modware.transrv.repository.EmployeeRepository;
import br.com.modware.transrv.dto.employee.EmployeeCreateRequest;
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

    boolean isEmployeeAvailable(Employee e, LocalDateTime now) {
        return true;
    }

}
