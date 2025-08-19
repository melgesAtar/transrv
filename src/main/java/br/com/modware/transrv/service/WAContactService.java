package br.com.modware.transrv.service;



import br.com.modware.transrv.model.WAContact;
import br.com.modware.transrv.repository.WAContactRepository;
import org.springframework.stereotype.Service;

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
}
