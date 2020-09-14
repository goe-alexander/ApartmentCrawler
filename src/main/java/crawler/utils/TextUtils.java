package crawler.utils;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class TextUtils {
  public static final Pattern DIACRITICS_AND_FRIENDS =
      Pattern.compile("[\\p{InCombiningDiacriticalMarks}\\p{IsLm}\\p{IsSk}]+");

  public static String removeDiacritics(String initialText) {
    String normalizedText = Normalizer.normalize(initialText, Normalizer.Form.NFD);
    normalizedText = DIACRITICS_AND_FRIENDS.matcher(normalizedText).replaceAll("");
    return normalizedText;
  }
}
