package com.scm.reader.livescanner.sdk;

/*
Copyright (c) 2012, kooaba AG
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

  * Redistributions of source code must retain the above copyright notice, this
    list of conditions and the following disclaimer.
  * Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.
  * Neither the name of the kooaba AG nor the names of its contributors may be
    used to endorse or promote products derived from this software without
    specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

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