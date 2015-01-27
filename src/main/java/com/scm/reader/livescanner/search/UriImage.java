/*
 * Copyright (c) 2015 Shortcut Media AG - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */

package com.scm.reader.livescanner.search;

import android.content.Context;
import android.net.Uri;



import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import static com.scm.reader.livescanner.util.LogUtils.logError;


//import java.io.File;
//import android.os.Environment;

public class UriImage extends ImageScaler {

  public Uri uri;
  private Context context;

  public UriImage(Uri uri, Context context) throws FileNotFoundException {
    this.uri = uri;
    this.context = context;
//     File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
////        + "/manori.jpg"
//        + "/20MIN_BE_20101210_011.pdf.png"
//     );
//    this.uri = Uri.fromFile(file);
    //checking if file exists
    InputStream inputStream = null;
    try {
      inputStream = openImageInputStream();
    } finally {
      if (inputStream != null) {
        try {
          inputStream.close();
        } catch (IOException e) {
          logError("Failed to close image input stream", e);
        }
      }
    }
  }

  @Override
  protected InputStream openImageInputStream() throws FileNotFoundException {
    return context.getContentResolver().openInputStream(uri);
  }

  
}
