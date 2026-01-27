package br.com.modware.transrv.service;

import br.com.modware.transrv.model.WAContact;
import br.com.modware.transrv.repository.WAContactRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class WAContactService {

   private final WAContactRepository waContactRepository;

    public WAContactService(WAContactRepository waContactRepository) {
        this.waContactRepository = waContactRepository;
    }

    WAContact findOrCreateWaContact(String phoneNumber, String pushName) {
        return waContactRepository.findByPhoneNumber(phoneNumber)
                .orElseGet(() -> {
                    WAContact newContact = new WAContact();
                    newContact.setPhoneNumber(phoneNumber);
                    newContact.setName(pushName);
                    return waContactRepository.save(newContact);
                });
    }

    public WAContact save(WAContact contact) {
        return waContactRepository.save(contact);
    }

    @Transactional
    public WAContact create(String name, String phoneNumber) {
       
        if (waContactRepository.findByPhoneNumber(phoneNumber).isPresent()) {
            throw new IllegalArgumentException("Já existe um contato com o número de telefone: " + phoneNumber);
        }

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome não pode ser vazio");
        }

        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Número de telefone não pode ser vazio");
        }

        WAContact newContact = new WAContact();
        newContact.setName(name.trim());
        newContact.setPhoneNumber(phoneNumber.trim());
        return waContactRepository.save(newContact);
    }
}
