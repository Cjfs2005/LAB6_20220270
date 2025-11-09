package com.example.lab6_20220270.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtils {
    private static final String DATE_FORMAT = "dd/MM/yyyy";
    private static final SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.forLanguageTag("es-ES"));

    public static String formatDate(long timestamp) {
        return sdf.format(new Date(timestamp));
    }

    public static long parseDate(String dateString) throws ParseException {
        Date date = sdf.parse(dateString);
        return date != null ? date.getTime() : 0;
    }

    public static long getCurrentTimestamp() {
        return System.currentTimeMillis();
    }
}
