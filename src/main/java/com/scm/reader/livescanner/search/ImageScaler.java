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

package com.scm.reader.livescanner.search;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;


import com.scm.reader.livescanner.util.Size;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import static com.scm.reader.livescanner.util.LogUtils.logError;


public abstract class ImageScaler {

  private Size decodeImageDimensions() throws FileNotFoundException {
    InputStream input = null;
    try {
      input = openImageInputStream();
      BitmapFactory.Options options = new BitmapFactory.Options();
      options.inJustDecodeBounds = true;
      BitmapFactory.decodeStream(input, null, options);
      return new Size(options.outWidth, options.outHeight);
    } finally {
      if (input != null) {
        try {
          input.close();
        } catch (IOException e) {
          logError("Failed to close image input stream", e);
        }
      }
    }
  }

  public Bitmap getScaledImage(int maxSideSize) throws FileNotFoundException {
    Size sourceSize = decodeImageDimensions();
    Size maxSize = new Size(maxSideSize, maxSideSize);
    Size scaledSize = getScaleSize(sourceSize, maxSize);

    InputStream input = null;

    try {
      input = openImageInputStream();

      // do not load the whole image to save memory (however can not scale precisely at this point)
      BitmapFactory.Options options = new BitmapFactory.Options();
      options.inSampleSize = (int) (sourceSize.width / scaledSize.width);
      Bitmap loadedImage = BitmapFactory.decodeStream(input, null, options);

      Bitmap scaledImage = loadedImage;
      if (scaledSize.width != loadedImage.getWidth() || scaledSize.height != loadedImage.getHeight()) {
        scaledImage = Bitmap.createScaledBitmap(loadedImage, (int) scaledSize.width, (int) scaledSize.height, false);
      }

      return scaledImage;

    } finally {
      if (input != null) {
        try {
          input.close();
        } catch (IOException e) {
          logError("Failed to close image input stream", e);
        }
      }
    }
  }

  Size getScaleSize(Size sourceSize, Size frameSize) {
    float sourceRatio = sourceSize.width / sourceSize.height;
    float targetRatio = frameSize.width / frameSize.height;
    float scaledWidth = sourceSize.width;
    float scaledHeight = sourceSize.height;

    if (sourceRatio > targetRatio && sourceSize.width > frameSize.width) {
      scaledWidth = frameSize.width;
      scaledHeight = sourceSize.height * frameSize.width / sourceSize.width;
    } else if (sourceSize.height > frameSize.height) {
      scaledHeight = frameSize.height;
      scaledWidth = sourceSize.width * frameSize.height / sourceSize.height;
    }
    return new Size(scaledWidth, scaledHeight);
  }

  protected abstract InputStream openImageInputStream() throws FileNotFoundException;
}
