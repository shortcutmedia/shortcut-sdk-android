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

package com.scm.reader.livescanner.sdk.image;

import android.graphics.*;

import com.scm.reader.livescanner.util.LogUtils;

import java.io.ByteArrayOutputStream;

/**
 * ImageScaler allows to scale and compress raw frames from the camera into compressed
 * JPEGs.
 */
public class ImageScaler {
  public static final String TAG = ImageScaler.class.getSimpleName();

  // params
  private int scale;        // number of pixels to rescale the image to
  private int jpegQuality;  // jpeg quality for the new image

  /**
   * Constructor.
   * @param scale number of pixels you want to scale the image to. Either the height or width,
   *              what is bigger.
   * @param jpegQuality jpeg compression (1-100) of the resulting image
   */
  public ImageScaler(int scale, int jpegQuality) {
    this.scale = scale;
    this.jpegQuality = jpegQuality;
  }

  /**
   * Compress and scale an image and return the byte[] representation at the image. The image
   * needs to be converted to JPEG during this process.
   * @param data byte[] array representation of the image
   * @param width width in pixels
   * @param height height in pixels
   * @return a byte[] array representing the scaled down image
   */
  public byte[] compress(byte[] data, int width, int height) {
    LogUtils.logDebug(TAG, "Original image size: " + (data.length / 1024) + "kB");

    byte[] jdata = convertToJPEGEncoded(data, width, height);
    Bitmap bm = BitmapFactory.decodeByteArray(jdata, 0, jdata.length);
    LogUtils.logDebug(TAG, "Before scaling: " + bm.getWidth() + "x" + bm.getHeight());

    bm = scaleBitmap(bm, width, height);
    LogUtils.logDebug(TAG, "After scaling: " + bm.getWidth() + "x" + bm.getHeight());
    
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    bm.compress(Bitmap.CompressFormat.JPEG, jpegQuality, out);

    LogUtils.logDebug(TAG, "After compression: " + (out.toByteArray().length / 1024) + "Kb");

    bm.recycle();
    return out.toByteArray();
  }

  /**
   * Helper function to resize a bitmap
   * @param bm the bitmap which needs to be resized
   * @param width width of the bitmap
   * @param height height of the bitmap
   * @return a new bitmap scaled down to {#link scale}
   */
  private Bitmap scaleBitmap(Bitmap bm, int width, int height) {
    int dstWidth  = scale;
    int dstHeight = (height * scale) / width;
    return Bitmap.createScaledBitmap(bm, dstWidth, dstHeight, false);
  }

  /**
   * convert the data to JPEG because BitmapFactory.decodeStream doesn't know how
   * to handle NV21 streams which is what we get from the camera
   * @param data byte[] array image representation of the image to be converted. This
   *             should be in NV21 format, usually the raw frame from the camera
   * @param width width of the image in pixels
   * @param height height of the image in pixels
   */
  public byte[] convertToJPEGEncoded(byte[] data, int width, int height) {
    YuvImage yuvimage = new YuvImage(data, ImageFormat.NV21,
            width, height, null);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    yuvimage.compressToJpeg(new Rect(0, 0, width, height), getJpegQuality(), baos);

    return baos.toByteArray();
  }

  /**
   * Get the jpegQuality of the image scaler.
   * @return int the jpegQuality of the image scaler.
   */
  public int getJpegQuality() {
    return jpegQuality;
  }
}
