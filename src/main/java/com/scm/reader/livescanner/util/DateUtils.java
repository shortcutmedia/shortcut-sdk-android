/*
 * Copyright 2015 Shortcut Media AG.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
