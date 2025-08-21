package br.com.modware.transrv.repository;

import br.com.modware.transrv.model.AlertTerm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlertTermRepository extends JpaRepository<AlertTerm, Long> {
    List<AlertTerm> findAllByStatus(AlertTerm.Status status);
    Optional<AlertTerm> findByCodeAndStatus(String code, AlertTerm.Status status);
    Optional<AlertTerm> findByCode(String code);
    // (Opcional) se quiser buscar pelo label editável:
    Optional<AlertTerm> findByNameAndStatus(String name, AlertTerm.Status status);
}


