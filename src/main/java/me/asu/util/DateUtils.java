package me.asu.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Suk.
 * @since 2018/7/11
 */
public class DateUtils {

    /**
     * 1 Day in Millis
     */
    public static final long DAY = 24L * 60L * 60L * 1000L;

    /**
     * 1 Week in Millis
     */
    public static final long WEEK = 7 * DAY;


    /** The Default Timezone to be used */
    private static final TimeZone TIMEZONE = TimeZone.getDefault();

    /** Create Date Formats */
    private static final String[] POSSIBLE_DATE_FORMATS = {
                /* RFC 1123 with 2-digit Year */"EEE, dd MMM yy HH:mm:ss z",
				/* RFC 1123 with 4-digit Year */"EEE, dd MMM yyyy HH:mm:ss z",
				/* RFC 1123 with no Timezone */"EEE, dd MMM yy HH:mm:ss",
				/* Variant of RFC 1123 */"EEE, MMM dd yy HH:mm:ss",
				/* RFC 1123 with no Seconds */"EEE, dd MMM yy HH:mm z",
				/* Variant of RFC 1123 */"EEE dd MMM yyyy HH:mm:ss",
				/* RFC 1123 with no Day */"dd MMM yy HH:mm:ss z",
				/* RFC 1123 with no Day or Seconds */"dd MMM yy HH:mm z",
				/* ISO 8601 slightly modified */"yyyy-MM-dd'T'HH:mm:ssZ",
				/* ISO 8601 slightly modified */"yyyy-MM-dd'T'HH:mm:ss'Z'",
				/* ISO 8601 slightly modified */"yyyy-MM-dd'T'HH:mm:sszzzz",
				/* ISO 8601 slightly modified */"yyyy-MM-dd'T'HH:mm:ss z",
				/* ISO 8601 */"yyyy-MM-dd'T'HH:mm:ssz",
				/* ISO 8601 slightly modified */"yyyy-MM-dd'T'HH:mm:ss.SSSz",
				/* ISO 8601 slightly modified */"yyyy-MM-dd'T'HHmmss.SSSz",
				/* ISO 8601 slightly modified */"yyyy-MM-dd'T'HH:mm:ss",
				/* ISO 8601 w/o seconds */"yyyy-MM-dd'T'HH:mmZ",
				/* ISO 8601 w/o seconds */"yyyy-MM-dd'T'HH:mm'Z'",
				/* RFC 1123 without Day Name */"dd MMM yyyy HH:mm:ss z",
				/* RFC 1123 without Day Name and Seconds */"dd MMM yyyy HH:mm z",
				/* Simple Date Format */"yyyy-MM-dd",
                /* Simple Date Format */"MMM dd, yyyy"};

    /**
     * Tries different date formats to parse against the given string
     * representation to retrieve a valid Date object.
     *
     * @param strdate Date as String
     * @return Date The parsed Date
     */
    public static Date parseDate(String strdate) {

        /* Return in case the string date is not set */
        if (strdate == null || strdate.length() == 0) {
            return null;
        }

        Date result = null;
        strdate = strdate.trim();
        if (strdate.length() > 10) {

            /* Open: deal with +4:00 (no zero before hour) */
            boolean signAtBegin = signPos(strdate, "+") == 0 || signPos(strdate, "-") == 0;
            boolean colonAtTwo = signPos(strdate, ":") == 2;
            if (signAtBegin && colonAtTwo) {
                String sign = strdate.substring(strdate.length() - 5, strdate.length() - 4);
                strdate = strdate.substring(0, strdate.length() - 5) + sign + "0" + strdate
                        .substring(strdate.length() - 4);
            }

            String dateEnd = strdate.substring(strdate.length() - 6);

            /*
             * try to deal with -05:00 or +02:00 at end of date replace with -0500 or
             * +0200
             */
            signAtBegin = dateEnd.indexOf("-") == 0 || dateEnd.indexOf("+") == 0;
            boolean colonAtThree = dateEnd.indexOf(":") == 3;
            if (signAtBegin && colonAtThree) {
                boolean hasGMT = "GMT"
                        .equals(strdate.substring(strdate.length() - 9, strdate.length() - 6));
                if (!hasGMT) {
                    String oldDate = strdate;
                    String newEnd = dateEnd.substring(0, 3) + dateEnd.substring(4);
                    strdate = oldDate.substring(0, oldDate.length() - 6) + newEnd;
                }
            }
        }

        /* Try to parse the date */
        SimpleDateFormat[] dfsArr = createSimpleDataFormats(POSSIBLE_DATE_FORMATS);
        int i = 0;
        while (i < dfsArr.length) {
            try {

                /*
                 * This Block needs to be synchronized, because the parse-Method in
                 * SimpleDateFormat is not Thread-Safe.
                 */
                synchronized (dfsArr[i]) {
                    return dfsArr[i].parse(strdate);
                }
            } catch (ParseException e) {
                i++;
            } catch (NumberFormatException e) {
                i++;
            }
        }
        return result;
    }

    private static int signPos(String strdate, String str) {
        return strdate.substring(strdate.length() - 5).indexOf(str);
    }


    public static SimpleDateFormat[] createSimpleDataFormats(String[] formats) {
		/* Create the dateformats */
        SimpleDateFormat[] dfsArr = new SimpleDateFormat[formats.length];

        for (int i = 0; i < formats.length; i++) {
            dfsArr[i] = new SimpleDateFormat(formats[i], Locale.getDefault());
            dfsArr[i].setTimeZone(TIMEZONE);
        }

        return dfsArr;
    }
}
