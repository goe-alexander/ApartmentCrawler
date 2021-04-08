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

    public String buildHtmlTable(List<Apartment> source, String template_name){
        Context thymeleafContext = new Context();
        thymeleafContext.setVariable("apartments", source);
        return templateEngine.process(template_name, thymeleafContext);
    }
}
