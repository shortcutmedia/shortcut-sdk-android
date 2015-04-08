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

package com.scm.reader.livescanner.sdk;


import android.os.Handler;
import android.os.Looper;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.scm.reader.livescanner.sdk.zxing.DecodeFormatManager;

import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * This thread has an attached handler (queryHandler) which is in charge with making the
 * actual queries.
 * @see QueryHandler on how is that actually done.
 */
public class QueryThread extends Thread implements Runnable{
  protected ScanHandler scanActivityHandler = null;
  protected QueryHandler queryHandler = null;
  private final Map<DecodeHintType,Object> hints;

  private final CountDownLatch handlerInitLatch;

  public QueryThread(ScanHandler handler) {
    scanActivityHandler = handler;
    handlerInitLatch = new CountDownLatch(1);

    Collection<BarcodeFormat> decodeFormats = EnumSet.noneOf(BarcodeFormat.class);
    decodeFormats.addAll(DecodeFormatManager.ONE_D_FORMATS);
    decodeFormats.addAll(DecodeFormatManager.QR_CODE_FORMATS);
    decodeFormats.addAll(DecodeFormatManager.DATA_MATRIX_FORMATS);

    hints = new EnumMap<DecodeHintType,Object>(DecodeHintType.class);
  }

  @Override
  public void run() {
    Looper.prepare();
    queryHandler = new QueryHandler(scanActivityHandler, hints);
    handlerInitLatch.countDown();
    Looper.loop();
  }
  
  public Handler getHandler() {
    try {
      handlerInitLatch.await();
    } catch (InterruptedException ie) {
      // continue?
    }
    return queryHandler;
  }

}