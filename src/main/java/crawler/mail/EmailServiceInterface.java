package crawler.mail;

import crawler.entities.Apartment;

import javax.mail.MessagingException;
import java.util.List;

public interface EmailServiceInterface {
    public void sendSimpleMessage(String to, String subject, String text);
    public void sendComplexMessageWithNewApartments(List<String> to, String subject, String text, List<Apartment> newFoundApartments) throws MessagingException;
    public void sendComplexMessageWithModifiedPrices(List<String> to, String subject, String text, List<Apartment> oldModifiedApartments) throws MessagingException;
    public void sendComplexMessageWithInactiveAppartments(List<String> recipients, String subject, String text, List<Apartment> inactiveApartments) throws MessagingException;
    public void sendComplexMessageWithReactivatedAppartments(List<String> recipients, String subject, String text, List<Apartment> inactiveApartments) throws MessagingException;
}