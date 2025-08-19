package br.com.modware.transrv.repository;

import br.com.modware.transrv.model.WAContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WAContactRepository extends JpaRepository<WAContact, Long> {
    Optional<WAContact> findByPhoneNumber(String phoneNumber);
}
