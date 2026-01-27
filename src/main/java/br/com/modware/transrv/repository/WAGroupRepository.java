package br.com.modware.transrv.repository;

import br.com.modware.transrv.model.WAGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WAGroupRepository extends JpaRepository<WAGroup, Long>, JpaSpecificationExecutor<WAGroup> {
    Optional<WAGroup> findByEvolutionGroupId(String groupId);

    @Query("select g from WAGroup g left join fetch g.agent where g.id = :id")
    Optional<WAGroup> findByIdWithAgent(@Param("id") Long id);

}
