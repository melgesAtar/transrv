package br.com.modware.transrv.repository;

import br.com.modware.transrv.model.Employee;
import br.com.modware.transrv.model.WAContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    List<Employee> findByWaContact_PhoneNumber(String phoneNumber);
    List<Employee> findByWaContact_PhoneNumberAndNameIgnoreCase(String phoneNumber, String name);

    Optional<Employee> findByWaContact(WAContact waContact);
}
