package crawler.mappers;

import crawler.entities.Contact;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class ImobiliareRoContactMapper extends BaseContactMapper {

  @Override
  protected Contact getContact(Document htmlDetailsDoc) {
    Elements contactContainer = htmlDetailsDoc.select("#b-contact-footer");
    if (contactContainer != null) {
      String phoneNumber = contactContainer.select("p.nrtel").text();
      String contactName = contactContainer.select("div.date-agent").text();
      return Contact
                .builder()
                .name(contactName)
                .phoneNumber(phoneNumber)
                .build();
    }
    return null;
  }
}
