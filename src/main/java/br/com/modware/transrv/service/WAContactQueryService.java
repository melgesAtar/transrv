package br.com.modware.transrv.service;

import br.com.modware.transrv.dto.wacontact.WAContactPageResponse;
import br.com.modware.transrv.dto.wacontact.WAContactResponse;
import br.com.modware.transrv.model.WAContact;
import br.com.modware.transrv.repository.WAContactRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WAContactQueryService {

    private final WAContactRepository waContactRepository;

    public WAContactQueryService(WAContactRepository waContactRepository) {
        this.waContactRepository = waContactRepository;
    }

    @Transactional(readOnly = true)
    public WAContactPageResponse findAll(int page, int size, String sortBy, String sortDirection) {
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

        Page<WAContact> waContactPage = waContactRepository.findAll(pageable);

        return new WAContactPageResponse(
            waContactPage.getContent().stream()
                .map(WAContactResponse::from)
                .toList(),
            waContactPage.getNumber(),
            waContactPage.getSize(),
            waContactPage.getTotalElements(),
            waContactPage.getTotalPages(),
            waContactPage.isFirst(),
            waContactPage.isLast()
        );
    }
}