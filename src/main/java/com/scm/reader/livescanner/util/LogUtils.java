/*
 * Copyright (c) 2014 Shortcut Media AG - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
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
