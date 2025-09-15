package br.com.modware.transrv.repository;

import br.com.modware.transrv.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    List<Employee> findByWaContact_PhoneNumber(String phoneNumber);
    List<Employee> findByWaContact_PhoneNumberAndNameIgnoreCase(String phoneNumber, String name);
}
