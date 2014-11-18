package com.scm.reader.livescanner.sdk.image;

import android.graphics.*;
import android.util.Log;

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
    Log.d(TAG, "Original image size: " + (data.length / 1024) + "kB");

    byte[] jdata = convertToJPEGEncoded(data, width, height);
    Bitmap bm = BitmapFactory.decodeByteArray(jdata, 0, jdata.length);
    Log.d(TAG, "Before scaling: " + bm.getWidth() + "x" + bm.getHeight());

    bm = scaleBitmap(bm, width, height);
    Log.d(TAG, "After scaling: " + bm.getWidth() + "x" + bm.getHeight());
    
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    bm.compress(Bitmap.CompressFormat.JPEG, jpegQuality, out);

    Log.d(TAG, "After compression: " + (out.toByteArray().length / 1024) + "Kb");

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
