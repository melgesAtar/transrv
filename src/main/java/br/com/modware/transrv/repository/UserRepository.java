package br.com.modware.transrv.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import br.com.modware.transrv.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
    
}
