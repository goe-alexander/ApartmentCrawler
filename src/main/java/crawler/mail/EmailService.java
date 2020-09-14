package crawler.mail;

import crawler.entities.Apartment;
import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.List;

@Component
public class EmailService implements EmailServiceInterface {
  @Autowired public JavaMailSender emailSender;

  @Autowired public MailContentBuilder mailContentBuilder;

  @Override
  public void sendSimpleMessage(String to, String subject, String text) {
    // TODO
  }

  @Override
  public void sendComplexMessageWithNewApartments(
      List<String> recipients, String subject, String text, List<Apartment> newFoundApartments)
      throws MessagingException {
    for (String recipient : recipients) {
      MimeMessage message = createMimeMessage(subject, newFoundApartments, recipient, mailContentBuilder.buildMainNewApartmentsTable(newFoundApartments));

      emailSender.send(message);
      System.out.println("Email Sent for new apartments! to: " + recipient);
    }
  }

  @Override
  public void sendComplexMessageWithModifiedPrices(
      List<String> recipients, String subject, String text, List<Apartment> oldModifiedApartments) throws MessagingException{
    for (String recipient : recipients) {
      MimeMessage message = createMimeMessage(subject, oldModifiedApartments, recipient, mailContentBuilder.buildChangedPriceApartmentsTable(oldModifiedApartments));

      emailSender.send(message);
      System.out.println("Email Sent for changed apartments! to: " + recipient);
    }
  }

  private MimeMessage createMimeMessage(String subject, List<Apartment> newFoundApartments, String recipient, String textContent) throws MessagingException {
    String uniMessage = "\uD83D\uDC1B\uD83E\uDD8B";
    String emojis = StringEscapeUtils.unescapeJava(uniMessage);
    StringBuilder htmlContent = new StringBuilder();

    MimeMessage message = emailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(message, true);
    helper.setTo(recipient);
    helper.setSubject(subject + emojis);
    helper.setText(
        textContent,
        true);
    return message;
  }
}
