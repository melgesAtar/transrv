package br.com.modware.transrv.repository;

import br.com.modware.transrv.model.WAConversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WAConversationRepository extends JpaRepository<WAConversation, Long> {

}
