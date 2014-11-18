package com.scm.reader.livescanner.sdk.image;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;

/**
 * Compares two images using their HSV histograms.
 * On quantization we use 162 buckets for the colors (18 x 3 x 3 = H x S v V).
 */
public class ImageComparer {

  // Number of bins for HSV representation: 18 * 3 * 3  (H * S * V)
  private static final int NO_BUCKETS = 162;

  // Sampling of the image when computing the histogram. It's too slow if we don't sample and with sampling we
  // lose almost no accuracy
  private static final int IN_SAMPLING_SIZE = 4;

  private static Bitmap sampleImage(byte image[]) {
    // First decode with inJustDecodeBounds=true to check dimensions
    final BitmapFactory.Options options = new BitmapFactory.Options();

    options.inJustDecodeBounds = true;
    BitmapFactory.decodeByteArray(image, 0, image.length, options);

    options.inSampleSize = IN_SAMPLING_SIZE;
    options.inJustDecodeBounds = false;
    return BitmapFactory.decodeByteArray(image, 0, image.length, options);
  }
  
  /**
   * Compute the normalized HSV histogram of a Bitmap.
   * @param image the image in byte array format we need to compute the image histogram for
   * @return a vector of ints, each element containing how many pixels with that color are contained in the image
   */
  public static float[] computeHistogram(byte image[]) {
    Bitmap img = sampleImage(image);
    
    long startTime = System.currentTimeMillis();

    int xRange = img.getWidth();
    int yRange = img.getHeight();

    float mHSV[]     = new float[NO_BUCKETS];
    float hsv[]      = new float[3];   // H in [0, 360) , S in [0, 1] , V in [0, 1]
    int colorBytes[] = new int[xRange * yRange + 1];

    img.getPixels(colorBytes, 0, xRange, 0, 0, xRange, yRange);

    for (int color : colorBytes) {
      Color.RGBToHSV(((color >> 16) & 0xFF), ((color >> 8) & 0xFF), (color & 0xFF), hsv);
      mHSV[quantizationH(hsv[0]) * 9 + quantizationS(hsv[1]) * 3 + quantizationV(hsv[2])] ++;
    }

    Log.d("Histogram computation", "Operation took: " + (System.currentTimeMillis() - startTime) + " ms");
    return mHSV;
  }

  /**
   * Computes the distance between an image and the histogram of another image
   * @param firstImage a stream of bytes representing the image
   * @param secondImageHist vector of ints representing the histogram of another image
   * @return
   */
  public static float imageDistance(byte firstImage[], float secondImageHist[]) {
    float firstImageHist[]  = computeHistogram(firstImage);

    return histIntersectionDistance(firstImageHist, secondImageHist);
  }

  /**
   * Compute L2 distance between two histograms of two images
   * @param a histogram of first image
   * @param b histogram for second image
   * @return the euclidian distance between the two images
   */
  public static double L2distance(float a[], float b[]) {
    float term = 0f, sum = 0f;

    for (int i = 0; i < a.length; i++) {
      term = (b[i] - a[i]) * (b[i] - a[i]);
      sum += term;
    }

    return Math.sqrt(sum);
  }

  public static double L1Distance(float a[], float b[]) {
    float sum = 0f;

    for (int i = 0; i < a.length; i++) {
      sum += Math.abs(b[i] - a[i]);
    }

    return sum;
  }

  /**
   * Uses histogram intersection similarity: bigger means more similar (values are between 0, 1).
   * @param a histogram of first image
   * @param b histogram for second image
   * @return the distance between the two images
   */
  public static float histIntersectionDistance(float a[], float b[]) {
    int i;
    float cTemp = 0.0f;
    float denominator = 0.0f;

    for(i = 0; i < a.length; i++){
      cTemp += Math.min(a[i], b[i]);
      // normalization happens when dividing by the denominator;
      // both images have the same number of pixels = sum(a[i]) || sum(b[i])
      // Even if it might be not necessary, it is usefull to see that they add
      // up to 1.
      denominator += a[i];
    }

    return denominator == 0f ? 0f : (cTemp / denominator);
  }

  private static int quantizationH(float value) {
    return (int) value / 20; // will fall in one of the 18 bins of Hue color
  }

  private static int quantizationS(float value) {
    if (value > 0 && value < 0.33) { return 0; }
    else if (value > 0.32 && value < 0.66 ) { return 1; }
    else { return 2; }
  }

  private static int quantizationV(float value) {
    if (value > 0 && value < 0.33) { return 0; }
    else if (value > 0.32 && value < 0.66 ) { return 1; }
    else { return 2; }
  }

  public static String printHistogram(int[] hist) {
    StringBuilder sb = new StringBuilder();
    for (int el: hist) {
      sb.append(el).append(" ");
    }
    return sb.toString();
  }
}
