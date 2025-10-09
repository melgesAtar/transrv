package br.com.modware.transrv.repository;

import br.com.modware.transrv.model.Employee;
import br.com.modware.transrv.model.EmployeeWAContact;
import br.com.modware.transrv.model.WAContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query("select distinct w.waContact.phoneNumber from EmployeeWAContact w where w.employee.id = :employeeId and w.waContact.phoneNumber is not null")
    List<String> findDistinctPhoneNumbersByEmployeeId(@Param("employeeId") Long employeeId);

    @Query("select w from EmployeeWAContact w join fetch w.waContact join fetch w.employee where w.employee in :employees and w.waContact.phoneNumber is not null")
    List<EmployeeWAContact> findWithContactByEmployeeIn(@Param("employees") Collection<Employee> employees);
}


