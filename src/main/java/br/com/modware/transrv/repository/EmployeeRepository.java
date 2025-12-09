package br.com.modware.transrv.repository;

import br.com.modware.transrv.model.Employee;
import br.com.modware.transrv.model.WAContact;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    @Query("""
    select e from Employee e
    join EmployeeWAContact lw on lw.employee = e
    where lw.waContact.phoneNumber = :phone
      and e.isActive = true
""")
    List<Employee> findByLinkedPhone(@Param("phone") String phoneNumber);


    @Query("""
    select e from Employee e
    join EmployeeWAContact lw on lw.employee = e
    where lower(e.name) = lower(:name)
      and lw.waContact.phoneNumber = :phone
      and e.isActive = true
""")
    List<Employee> findByLinkedPhoneAndName(
            @Param("phone") String phoneNumber,
            @Param("name") String name
    );


    @Query("""
    select e from Employee e
    join EmployeeWAContact lw on lw.employee = e
    where lw.waContact = :waContact
      and e.isActive = true
""")
    Optional<Employee> findByLinkedWAContact(@Param("waContact") WAContact waContact);

}
