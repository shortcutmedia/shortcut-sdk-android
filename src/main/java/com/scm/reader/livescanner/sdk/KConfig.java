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

/**
 * Config Singleton for the scanner. Provides getters and setters for all parameters used by
 * the SDK.
 */
public class KConfig {
  // configuration
  private String accessKey;
  private String secretKey;
  private String requestedMetadata;
  private Float  histogramThreshold;

  private String  server;
  private Integer port;
  private String  path;
  private Integer scale;
  private Integer jpegQuality;
  private Integer uploadJpegQuality;
  private Integer uploadJpegMaxWidthHeight;

  protected static final Object configLock = new Object();

  private static KConfig ref;

  private KConfig() {
    synchronized (configLock) {
      accessKey          = "1b98e2311ac23927b58a96c5e687f99f997e5bee";
      secretKey          = "e2c3f161916b2f931d9197dab78475190d4b46fd";
      requestedMetadata  = "external-references,minimal,extended";
      histogramThreshold = 0.5f;
      server             = "query-api.kooaba.com";
//      server             = "192.168.0.106:9000";
      port               = 80;
      path               = "/v4/query";
      scale              = 640;
      jpegQuality        = 50;
      uploadJpegQuality  = 75;
      uploadJpegMaxWidthHeight = 800;

    }
  }

  /**
   * Get and instance of the configuration object in the SDK.
   * @return KConfig object - the configuration object in the SDK.
   */
  public static synchronized KConfig getConfig() {
    if (ref == null)
      ref = new KConfig();
    return ref;
  }

  public String getAccessKey() {
    return accessKey;
  }

  public String getSecretKey() {
    return secretKey;
  }

  public String getRequestedMetadata() {
    return requestedMetadata;
  }

  public Float getHistogramThreshold() {
    return histogramThreshold;
  }

  public void setAccessKey(String accessKey) {
    this.accessKey = accessKey;
  }

  public void setSecretKey(String secretKey) {
    this.secretKey = secretKey;
  }

  public void setRequestedMetadata(String requestedMetadata) {
    this.requestedMetadata = requestedMetadata;
  }

  public void setHistogramThreshold(Float histogramThreshold) {
    this.histogramThreshold = histogramThreshold;
  }

  public String getServer() {
    return server;
  }

  public void setServer(String server) {
    this.server = server;
  }

  public Integer getPort() {
    return port;
  }

  public void setPort(Integer port) {
    this.port = port;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public Integer getScale() {
    return scale;
  }

  public void setScale(Integer scale) {
    this.scale = scale;
  }

  public Integer getJpegQuality() {
    return jpegQuality;
  }

  public void setJpegQuality(Integer jpegQuality) {
    this.jpegQuality = jpegQuality;
  }

  public Integer getUploadJpegQuality() {
    return uploadJpegQuality;
  }

  public void setUploadJpegQuality(Integer uploadJpegQuality) {
    this.uploadJpegQuality = uploadJpegQuality;
  }

  public Integer getUploadJpegMaxWidthHeight() {
    return uploadJpegMaxWidthHeight;
  }

  public void setUploadJpegMaxWidthHeight(Integer uploadJpegMaxWidthHeight) {
    this.uploadJpegMaxWidthHeight = uploadJpegMaxWidthHeight;
  }
}
