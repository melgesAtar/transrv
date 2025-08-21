package br.com.modware.transrv.repository;

import br.com.modware.transrv.model.AIUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsageRepository extends JpaRepository<AIUsage, Long> {
}
