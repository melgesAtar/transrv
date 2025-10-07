package br.com.modware.transrv.repository;

import br.com.modware.transrv.model.WAConversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import br.com.modware.transrv.model.WAContact;
import java.util.Optional;

@Repository
public interface WAConversationRepository extends JpaRepository<WAConversation, Long> {
    Optional<WAConversation> findByWaContact(WAContact waContact);
}
