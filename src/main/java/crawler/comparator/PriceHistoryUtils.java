package crawler.comparator;

import crawler.entities.Apartment;
import crawler.entities.PriceHistory;
import org.springframework.stereotype.Component;
import java.util.Date;

@Component
public class PriceHistoryUtils {
  public PriceHistory generatePriceHistoryForExistingApp(Apartment apartment, Double newPrice) {
    return PriceHistory.builder()
        .newPrice(newPrice)
        .oldPrice(apartment.getPrice())
        .changedAt(new Date())
        .apartment(apartment)
        .build();
  }

  public PriceHistory generatePriceHistoryForNewApartment(Apartment apartment){
    return PriceHistory.builder()
        .newPrice(apartment.getPrice())
        .oldPrice(null)
        .changedAt(new Date())
        .apartment(apartment)
        .build();
  }
}
