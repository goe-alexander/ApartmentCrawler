package crawler.mappers;

import crawler.entities.Apartment;
import crawler.enums.ApartmentSource;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static crawler.utils.HttpUtils.USER_AGENT;

public class ImobiliareRoAppartmentMapper extends BaseApartmentMapper {

  private static final Pattern latPattern = Pattern.compile("/lat/(.*?)/lon/", Pattern.DOTALL);
  private static final Pattern lonPattern = Pattern.compile("/lon/(.*?)/loc/", Pattern.DOTALL);
  private static final Pattern lastUpdatedDatePattern = Pattern.compile("^\\d{2}-\\d{2}-\\d{4}$");

  public Double getPriceFromHeader(Element apartmentDIVHeader) {
    return getDoubleIfpresent(
        getNumberFromElement(apartmentDIVHeader.getAllElements(), "span.pret-mare"));
  }

  // gets all details of apartments from inital condensed view, and goes to details for each
  // afterwards
  public Apartment getFromHeaderDiv(Element apartmentDIVHeader) throws IOException, ParseException {
    BaseContactMapper contactMapper = new ImobiliareRoContactMapper();
    Elements apartmentDetailsContainer = apartmentDIVHeader.select("a[itemprop]");
    Connection webConnection =
        Jsoup.connect(apartmentDetailsContainer.attr("href")).userAgent(USER_AGENT);
    Document htmlDetailsDoc = webConnection.get();
    // submitForm to GetPhone number only AFTER you have gotten the response;
    // EDIT: Still not working for whatever reason, will followUp
    submitFormToGetPhoneNumber(webConnection);
    Apartment apartment = new Apartment();
    if (webConnection.response().statusCode() == 200) {
      org.jsoup.select.Elements characteristics =
          htmlDetailsDoc.select("#b_detalii_caracteristici");
      htmlDetailsDoc.select("div.pret.first.blue > span").remove();
      String price = htmlDetailsDoc.select("div.pret.first.blue").text().replaceAll("\\D+", "");
      String title = htmlDetailsDoc.select("div.titlu").select("h1").first().text();
      String details = htmlDetailsDoc.select("#b_detalii_text").select("p").text();
      String noOfBathrooms =
          getElementsIfExists(characteristics, "li:contains(băi:)")
              .map(elements -> elements.select("span").text())
              .orElseGet(null);
      String noOfRooms =
          getElementsIfExists(characteristics, "li:contains(camere)")
              .map(elements -> getNumberFromElement(elements, "span"))
              .orElseGet(null);
      String builtSurface =
          getElementsIfExists(characteristics, "li:contains(Suprafaţă)")
              .map(elements -> getNumberFromElement(elements, "span"))
              .orElseGet(null);
      String noOfBalconies =
          getElementsIfExists(characteristics, "li:contains(balcoane)")
              .map(elements -> getNumberFromElement(elements, "span"))
              .orElseGet(null);
      String yearBuilt =
          getElementsIfExists(characteristics, "li:contains(An construcţie)")
              .map(elements -> getNumberFromElement(elements, "span"))
              .orElseGet(null);
      String floor =
          getElementsIfExists(characteristics, "li:contains(Etaj)")
              .map(elements -> getNumberFromElement(elements, "span"))
              .orElseGet(null);
      apartment =
          Apartment.builder()
              .externalId(apartmentDIVHeader.attr("data-id-cod"))
              .source(ApartmentSource.IMOBILIARE_RO.name())
              .directLink(apartmentDetailsContainer.attr("href"))
              .bathrooms(getIntegerIfPresent(noOfBathrooms))
              .builtSurface(getDoubleIfpresent(builtSurface))
              .balconies(getIntegerIfPresent(noOfBalconies))
              .yearBuilt(getIntegerIfPresent(yearBuilt))
              .floor(floor)
              .rooms(getIntegerIfPresent(noOfRooms))
              .price(getDoubleIfpresent(price))
              .title(title)
              .details(details)
              .dateCreated(new Date())
              .contact(contactMapper.getContact(htmlDetailsDoc))
              .active(true)
              .build();
      getAddressAndPositioning(apartment, htmlDetailsDoc);
      getLastUpdated(apartment, htmlDetailsDoc);
    }
    return apartment;
  }

  private void getLastUpdated(Apartment apartment, Document htmlDetailsDoc) throws ParseException {
    Elements lastUpdatedContainer = htmlDetailsDoc.select("span.data-actualizare");
    if (lastUpdatedContainer != null) {
      String lastUpdatedString =
          extractStringUsingPattern(lastUpdatedContainer.text(), lastUpdatedDatePattern);
      if (lastUpdatedString != null && !lastUpdatedString.isEmpty()) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-mm-YYYY", Locale.ENGLISH);
        apartment.setLastUpdated(formatter.parse(lastUpdatedString));
      }
    }
  }

  private void getAddressAndPositioning(Apartment apartment, Document htmlDetailsDoc) {
    Elements mapContainer = htmlDetailsDoc.select("#meniu-poi-localizare");
    if (mapContainer != null) {
      String addressLink = mapContainer.attr("rel");
      String lat = extractStringUsingPattern(addressLink, latPattern);
      String lon = extractStringUsingPattern(addressLink, lonPattern);
      // For this provider we don't have an address in the conventional sense
      // so we will save a link:
      apartment.setAddress(addressLink);
      apartment.setLat(getDoubleIfpresent(lat));
      apartment.setLon(getDoubleIfpresent(lon));
    }
  }

  private String extractStringUsingPattern(String targetString, Pattern latPattern) {
    StringBuilder result = new StringBuilder();
    Matcher matcher = latPattern.matcher(targetString);
    while (matcher.find()) {
      result.append(matcher.group(1));
    }
    return result.toString();
  }
  // Currently not working 404
  private void submitFormToGetPhoneNumber(Connection currentWebConnection) {
    HashMap<String, String> cookies = new HashMap<>();
    HashMap<String, String> formData = new HashMap<>();
    cookies.putAll(currentWebConnection.response().cookies());
    formData.put("sIdUnicOferta", "V0H00JNU2B5");
    formData.put("iCategorie", "1");
    formData.put("iTipTranzactie", "1");
    formData.put("iIdAgent", "1171850");
    formData.put("iSursa", "3");
    formData.put("bMobile", "");
    formData.put("sIdStrOferta", "XV0H00T8T");
    formData.put("iTipFormularSiPoz", "132");

    try {
      Connection.Response newPhoneNumberResponse =
          Jsoup.connect("https://www.imobiliare.ro/oferta/vezi-telefon")
              .cookies(cookies)
              .data(formData)
              .method(Connection.Method.POST)
              .userAgent(USER_AGENT)
              .execute();
      Document htmlResponse = newPhoneNumberResponse.parse();
    } catch (IOException e) {
      System.out.println("Unable to get Phone Number for Apartment: " + "");
    }
  }
}
