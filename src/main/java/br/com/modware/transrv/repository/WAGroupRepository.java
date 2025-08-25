package br.com.modware.transrv.repository;

import br.com.modware.transrv.model.AlertTerm;
import br.com.modware.transrv.model.WAContact;
import br.com.modware.transrv.model.WAGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WAGroupRepository extends JpaRepository<WAGroup, Long> {
    Optional<WAGroup> findByEvolutionGroupId(String groupId);

}
