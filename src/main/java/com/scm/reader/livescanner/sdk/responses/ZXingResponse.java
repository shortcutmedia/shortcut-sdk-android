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

package com.scm.reader.livescanner.sdk.responses;

import com.google.zxing.Result;
import com.scm.reader.livescanner.sdk.KInfo;

/**
 * Wrapper for Result class in ZXing SDK
 */
public class ZXingResponse implements Response {
  private Result zxingResult = null;
  private KInfo info;

  public ZXingResponse(Result zxingResult) {
    this.zxingResult = zxingResult;
    this.info = new KInfo();
  }

  @Override
  public boolean isRecognized() {
    return zxingResult != null;
  }

  @Override
  public String getTitle() {
    return zxingResult == null ? "" : zxingResult.getText();
  }

  @Override
  public KInfo getInfo() {
    return info;
  }
}
