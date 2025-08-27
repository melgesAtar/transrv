package br.com.modware.transrv.repository;

import br.com.modware.transrv.model.AlertTerm;
import br.com.modware.transrv.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

}
