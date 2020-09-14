package crawler.repositories;

import crawler.entities.Contact;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactRepository extends CrudRepository<Contact, Long> {
    Contact findByPhoneNumberOrName(String phoneNumber, String name);
    List<Contact> findByName(String name);
}
