/*
 * Copyright (c) 2014 Shortcut Media AG - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */

package com.scm.reader.livescanner.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import java.io.ByteArrayOutputStream;

import static android.graphics.Bitmap.CompressFormat.JPEG;

public final class PictureUtils {


  /**
   * From Google Camera application source, class com.android.camera.ImageManager
   */
  public static int roundOrientation(int orientationInput) {
    int orientation = orientationInput;
    if (orientation == -1) {
      orientation = 0;
    }

    orientation = orientation % 360;
    if (orientation < (0 * 90) + 45) {
      return 0;
    }
    if (orientation < (1 * 90) + 45) {
      return 90;
    }
    if (orientation < (2 * 90) + 45) {
      return 180;
    }
    if (orientation < (3 * 90) + 45) {
      return 270;
    }
    return 0;
  }

}
