package cz.muni.fi.hotel;
import java.math.BigDecimal;
import java.text.Collator;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by User on 9.5.2017.
 */
public class LocalDemo {
    public LocalDemo() {
    }

    Locale slovencina = Locale.forLanguageTag("sk-SK");
    Locale english = Locale.forLanguageTag("en-US");
    Locale francais = Locale.forLanguageTag("fr-FR");

    NumberFormat csFormat = NumberFormat.getCurrencyInstance(slovencina);
    NumberFormat usaFormat = NumberFormat.getCurrencyInstance(english);

    public static void main(String[] args) {
        showLocalTime(Locale.getDefault(), TimeZone.getTimeZone("Europe/Prague"));

    }

    public static void showLocalTime(Locale locale, TimeZone tz) {
        Date now = new Date();
        String zoneName = tz.getDisplayName(tz.inDaylightTime(now), 1, locale);
        DateFormat full = DateFormat.getDateTimeInstance(0, 0, locale);
        full.setTimeZone(tz);
        System.out.println(locale + ": " + full.format(now) + " (" + zoneName + ")");
        System.out.println(full.getClass());
    }


}
