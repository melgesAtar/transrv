package br.com.modware.transrv.repository;

import br.com.modware.transrv.model.Employee;
import br.com.modware.transrv.model.EmployeeWAContact;
import br.com.modware.transrv.model.WAContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeWAContactRepository extends JpaRepository<EmployeeWAContact, Long> {
    List<EmployeeWAContact> findByEmployee(Employee employee);
    List<EmployeeWAContact> findByWaContact(WAContact waContact);
    Optional<EmployeeWAContact> findByEmployeeAndWaContact(Employee employee, WAContact waContact);
    List<EmployeeWAContact> findByWaContact_PhoneNumber(String phoneNumber);
    List<EmployeeWAContact> findByEmployeeIn(Collection<Employee> employees);
}


