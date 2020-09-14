package crawler.mail;

import crawler.entities.Apartment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.List;

@Service
public class MailContentBuilder {
    private TemplateEngine templateEngine;

    @Autowired
    public MailContentBuilder(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public String buildMainNewApartmentsTable(List<Apartment> apartments) {
        Context thymeleafContext = new Context();
        thymeleafContext.setVariable("apartments", apartments);
        return templateEngine.process("new_apartments", thymeleafContext);
    }

    public String buildChangedPriceApartmentsTable(List<Apartment> changePriceApartments) {
        Context thymeleafContext = new Context();
        thymeleafContext.setVariable("apartments", changePriceApartments);
        return templateEngine.process("changed_apartments", thymeleafContext);
    }
}
