package br.com.modware.transrv.service;

import br.com.modware.transrv.dto.user.UserPageResponse;
import br.com.modware.transrv.dto.user.UserResponse;
import br.com.modware.transrv.model.User;
import br.com.modware.transrv.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserQueryService {

    private final UserRepository userRepository;

    public UserQueryService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public UserPageResponse findAll(int page, int size, String sortBy, String sortDirection) {
        if (size > 100) {
            size = 100;
        }

        Sort sort = Sort.by(
            "DESC".equalsIgnoreCase(sortDirection) 
                ? Sort.Direction.DESC 
                : Sort.Direction.ASC,
            sortBy != null && !sortBy.isEmpty() ? sortBy : "id"
        );
        
        Pageable pageable = PageRequest.of(page, size, sort);

        
        Page<User> userPage = userRepository.findAll(pageable);

        return new UserPageResponse(
            userPage.getContent().stream()
                .map(UserResponse::from)
                .toList(),
            userPage.getNumber(),
            userPage.getSize(),
            userPage.getTotalElements(),
            userPage.getTotalPages(),
            userPage.isFirst(),
            userPage.isLast()
        );
    }
}