package br.com.modware.transrv.repository;

import br.com.modware.transrv.model.AIUsage;
import br.com.modware.transrv.model.WAMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AIUsageRepository extends JpaRepository<AIUsage, Long>{
    boolean existsByMessage(WAMessage message);
}
