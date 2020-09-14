package crawler.mappers;

import crawler.entities.Contact;
import org.jsoup.nodes.Document;

public abstract class BaseContactMapper {
  protected abstract Contact getContact(Document htmlDetailsDoc);
}
