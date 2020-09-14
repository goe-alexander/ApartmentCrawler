package crawler.mappers;

import org.jsoup.select.Elements;

import java.util.Optional;

public abstract class BaseApartmentMapper {
    protected String getNumberFromElement(Elements elements, String htmlElement) {
        if(elements != null) {
            return elements.select(htmlElement).text().replaceAll("\\D+", "");
        }
        return null;
    }

    protected Optional<Elements> getElementsIfExists(Elements pieceOfHtml, String cssSelector) {
        return Optional.ofNullable(pieceOfHtml.select(cssSelector));
    }

    protected Long getLongIfPresent(String value) {
        if (value != null && !value.isEmpty()) {
            return(Long.valueOf(value));
        }
        return null;
    }

    protected Double getDoubleIfpresent(String value) {
        if (value != null && !value.isEmpty()) {
            return Double.valueOf(value);
        }
        return null;
    }

    protected Integer getIntegerIfPresent(String value) {
        if (value != null && !value.isEmpty()) {
            return Integer.valueOf(value);
        }
        return null;
    }
}
