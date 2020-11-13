package crawler;

import crawler.comparator.PriceHistoryUtils;
import crawler.entities.Apartment;
import crawler.entities.Contact;
import crawler.entities.PriceHistory;
import crawler.enums.ApartmentSource;
import crawler.mail.EmailService;
import crawler.mappers.ImobiliareRoAppartmentMapper;
import crawler.repositories.ApartmentRepository;
import crawler.repositories.ContactRepository;
import crawler.repositories.PriceHistoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.mail.MessagingException;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static crawler.utils.HttpUtils.USER_AGENT;

@Slf4j
@Configuration
public class ApplicationConfiguration {
  @Autowired ApartmentRepository apartmentRepository;
  @Autowired ContactRepository contactRepository;
  @Autowired PriceHistoryRepository priceHistoryRepository;
  @Autowired EmailService emailService;
  @Autowired PriceHistoryUtils priceHistoryUtils;

  List<Apartment> changedPriceApartments = new ArrayList<>();
  List<Apartment> foundNewApartments = new ArrayList<>();
  List<Apartment> inactiveApartments = new ArrayList<>();
  List<String> URLS =
      Arrays.asList(
          "https://www.imobiliare.ro/vanzare-apartamente/brasov/racadau?id=26607433", // Racadau 3
          // camere
          "https://www.imobiliare.ro/vanzare-apartamente/brasov/racadau?id=37385225", // Racadau 4
          // camere
          "https://www.imobiliare.ro/vanzare-apartamente/brasov/astra?id=52649222" // Astra 4 camere
          );

  private final List<String> recipients =
      Stream.of("your.eamil.list@here.com").collect(Collectors.toList());

  private List<Apartment> getAppartments(String finalURL) throws IOException, ParseException {
    Connection webConnection = Jsoup.connect(finalURL).userAgent(USER_AGENT);
    Document htmlDoc = webConnection.get();
    List<Apartment> apartments = new ArrayList<>();

    if (webConnection.response().statusCode() == 200) {
      System.out.println("Connected to Website: " + finalURL);
      Elements apartmentContainer = htmlDoc.select("#container-lista-rezultate");
      for (Element apartmentHTML : apartmentContainer.first().select("div[data-id-cod]")) {
        try {
          mapHTMLtoEntity(apartmentHTML).ifPresent(apartment -> apartments.add(apartment));
        } catch (IOException ioE) {
          log.error(
              "Issue processing apartment in: \n"
                  + finalURL
                  + " \n Apartment: "
                  + apartmentHTML.attr("data-id-cod"));
        }
      }
    }
    return apartments;
  }

  private void checkIfApartmentsStillExist(List<Apartment> apartments)
      throws IOException, ParseException {
    for (Apartment apartment : apartments) {
      Connection webConnection = Jsoup.connect(apartment.getDirectLink());
      Document htmlDoc = webConnection.get();
      Elements ofertaExpirata = htmlDoc.select("#oferta_expirata");
      if (ofertaExpirata.size() == 0) {
        continue;
      } else {
        System.out.println("Found inactive apartment: " + apartment.getId());
        apartment.setActive(false);
        apartmentRepository.save(apartment);
        inactiveApartments.add(apartment);
      }
    }
  }

  private Optional<Apartment> mapHTMLtoEntity(Element apartmentDIVHeader)
      throws IOException, ParseException {
    String externalId = apartmentDIVHeader.attr("data-id-cod");
    // TBD: entity condition of nonExistance based on externalId
    // Here we will have some comparisson logic and update in case of change and email sending.
    Apartment existingApartment =
        apartmentRepository.findByExternalIdAndSource(
            externalId, ApartmentSource.IMOBILIARE_RO.name());
    ImobiliareRoAppartmentMapper apartmentMapper = new ImobiliareRoAppartmentMapper();
    if (existingApartment != null) {
      Double currentPrice = apartmentMapper.getPriceFromHeader(apartmentDIVHeader);
      if (!currentPrice.equals(existingApartment.getPrice())) {
        existingApartment.setLastUpdated(new Date());
        existingApartment
            .getPriceHistories()
            .add(generateAndSavePriceHistory(existingApartment, currentPrice));
        existingApartment.setPrice(currentPrice);
        // save the changes
        apartmentRepository.save(existingApartment);
        changedPriceApartments.add(existingApartment);
        // and return nothing because we have it in a different list
      }
      return Optional.empty();
    }
    Apartment apartment = apartmentMapper.getFromHeaderDiv(apartmentDIVHeader);
    apartment.setPriceHistories(Arrays.asList(generateAndSavePriceHistory(apartment, null)));
    checkIfContactAlreadyExists(apartment);
    apartmentRepository.save(apartment);
    return Optional.ofNullable(apartment);
  }

  private PriceHistory generateAndSavePriceHistory(Apartment apartment, Double newPrice) {
    PriceHistory priceHistory;
    if (newPrice == null) {
      priceHistory = priceHistoryUtils.generatePriceHistoryForNewApartment(apartment);
    } else {
      priceHistory = priceHistoryUtils.generatePriceHistoryForExistingApp(apartment, newPrice);
    }
    // priceHistoryRepository.save(priceHistory);
    return priceHistory;
  }

  private void checkIfContactAlreadyExists(Apartment apartment) {
    if (apartment.getContact().getPhoneNumber() != null
        && !apartment.getContact().getPhoneNumber().isEmpty()) {
      Contact existingContact =
          contactRepository.findByPhoneNumberOrName(
              apartment.getContact().getPhoneNumber(), apartment.getContact().getName());
      if (existingContact != null) {
        apartment.setContact(existingContact);
      }
    } else {
      contactRepository.findByName(apartment.getContact().getName()).stream()
          .findFirst()
          .ifPresent(contact -> apartment.setContact(contact));
    }
    // In case entity does not exist we save the contact(parent) first
    contactRepository.save(apartment.getContact());
  }

  @Bean
  public CommandLineRunner crawlForAppartments() {
    return (args) -> {
      List<Apartment> apartmentsForTest = new ArrayList<>();
      System.out.println("Creepy Crawlies!\n ~ \n ~ \n ~ \n ~ \n ~ \n ~");

      System.out.println("Verifying invalid links:");
      checkIfApartmentsStillExist(apartmentRepository.findAllByActiveTrue());
      emailService.sendComplexMessageWithInactiveAccounts(
          recipients,
          "The following apartments are inactive!",
          "These were taken off the market",
          inactiveApartments);

      System.out.println("Searching for new apartments: ");
      System.out.println("");
      for (String url : URLS) {
        foundNewApartments.addAll(getAppartments(url));
      }
      sortCollectionsByRoomsAndPrice();
      sendEmails();
    };
  }

  private void sortCollectionsByRoomsAndPrice() {
    Comparator standardComparator =
        Comparator.comparing(Apartment::getRooms).thenComparing(Apartment::getPrice);

    foundNewApartments =
        (List<Apartment>)
            foundNewApartments.stream().sorted(standardComparator).collect(Collectors.toList());
    changedPriceApartments =
        (List<Apartment>)
            changedPriceApartments.stream().sorted(standardComparator).collect(Collectors.toList());
  }

  private void sendEmails() throws MessagingException {
    if (!foundNewApartments.isEmpty()) {
      emailService.sendComplexMessageWithNewApartments(
              recipients, "Crawling for a home, Found New Appartments ", "", foundNewApartments);
    }
    if (!changedPriceApartments.isEmpty()) {
      emailService.sendComplexMessageWithModifiedPrices(
              recipients,
              "Crawling for a home, Apartments that changed price",
              "",
              changedPriceApartments);
    }
  }
}
