package com.scm.reader.livescanner.sdk;/*
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
