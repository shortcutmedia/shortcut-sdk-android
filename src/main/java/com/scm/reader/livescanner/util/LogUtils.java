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

import android.util.Log;

public class LogUtils {
  public static final String LOG_TAG = "Shortcut";

  public static void logInfo(String msg) {
    Log.i(LOG_TAG, msg);
  }

  public static void logWarn(String msg) {
    Log.w(LOG_TAG, msg);
  }

  public static void logWarn(String msg, Throwable cause) {
    Log.w(LOG_TAG, msg, cause);
  }

  public static void logError(String msg, Throwable cause) {
    Log.e(LOG_TAG, msg, cause);
  }

  public static void logDebug(String msg) {
    Log.d(LOG_TAG, msg);
  }

  public static void setTestLogging(boolean testLogging) {
    LogUtils.debugLogEnabled = testLogging;
  }

  private static boolean debugLogEnabled = false;

  public static boolean isDebugLog() {
    return debugLogEnabled;
  }

  public static void setDebugLog(boolean debugLogEnabled) {
    LogUtils.debugLogEnabled = debugLogEnabled;
  }
}
