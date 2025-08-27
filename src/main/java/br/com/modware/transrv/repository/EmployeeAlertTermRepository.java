package br.com.modware.transrv.repository;

import br.com.modware.transrv.model.AlertTerm;
import br.com.modware.transrv.model.EmployeeAlertTerm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeAlertTermRepository extends JpaRepository<EmployeeAlertTerm, Long> {
    List<EmployeeAlertTerm> findByAlertTermAndPriorityLevel(AlertTerm alertTerm, Integer priorityLevel);
}
