
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

public class KInfo {
  private long queryTimeMillis;
  private long queryFileSize;
  private long imgCompressMillis;

  public KInfo() {}

  public KInfo(long queryTimeMillis, long queryFileSize, long imgCompressMillis) {
    setQueryTimeMillis(queryTimeMillis);
    setQueryFileSize(queryFileSize);
    setImgCompressMillis(imgCompressMillis);
  }

  /**
   * Return number of milliseconds it took for the query to execute (round-trip to server and back).
   * @return number of milliseconds it took for query to execute
   */
  public long getQueryTimeMillis() {
    return queryTimeMillis;
  }

  /**
   * Return the size of the file which was sent as the query.
   * @return the number of bytes in the file which was sent as a query.
   */
  public long getQueryFileSize() {
    return queryFileSize;
  }

  public long getImgCompressMillis() {
    return imgCompressMillis;
  }

  public void setImgCompressMillis(long imgCompressMillis) {
    this.imgCompressMillis = imgCompressMillis;
  }

  public void setQueryTimeMillis(long queryTimeMillis) {
    this.queryTimeMillis = queryTimeMillis;
  }

  public void setQueryFileSize(long queryFileSize) {
    this.queryFileSize = queryFileSize;
  }
}
