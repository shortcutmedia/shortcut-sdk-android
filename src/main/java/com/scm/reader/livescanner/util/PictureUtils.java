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
