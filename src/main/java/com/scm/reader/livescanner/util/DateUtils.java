/*
 * Copyright (c) 2014 Shortcut Media AG - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */

package com.scm.reader.livescanner.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import static java.util.Locale.ENGLISH;
import static java.util.TimeZone.getTimeZone;

public class DateUtils {

  private static final String RFC2616_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss z";
  private static final SimpleDateFormat dateFormatter = new SimpleDateFormat(RFC2616_DATE_FORMAT, ENGLISH);
  
  static {
    dateFormatter.setTimeZone(getTimeZone("GMT"));
  }

  public static String workAroundDateFormatterBug(String date) {
    // Date format "z" works as "zzz" in Android Java code
    return date.replace("GMT+00:00", "GMT");
  }

  public static String formatDate(Date date) {
    String result;
    synchronized (dateFormatter) {
      result = dateFormatter.format(date);
    }
    result = workAroundDateFormatterBug(result);
    return result;
  }

}
