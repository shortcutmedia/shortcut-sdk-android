package com.scm.reader.livescanner.sdk.recognizers;

/*
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

//import com.kooaba.ir.KQuery;
import static android.graphics.Bitmap.CompressFormat.JPEG;
import static android.view.Surface.ROTATION_0;
import static android.view.Surface.ROTATION_270;
import static android.view.Surface.ROTATION_90;
import static com.scm.reader.livescanner.util.LogUtils.isDebugLog;
import static com.scm.reader.livescanner.util.LogUtils.logDebug;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Location;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.scm.reader.livescanner.sdk.KConfig;
import com.scm.reader.livescanner.sdk.image.ImageComparer;
import com.scm.reader.livescanner.sdk.image.ImageScaler;
import com.scm.reader.livescanner.search.ImageRecognizer;
import com.scm.reader.livescanner.search.Search;
import com.scm.shortcutreadersdk.R;

//import org.apache.http.entity.mime.content.ByteArrayBody;
import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

/**
 * This class performs the actual recognition. It implements a {@link Recognizer} interface
 * so it must implement the <code>recognize</code> function.
 */
public class KooabaRecognizer implements Recognizer {  
  private float[] histogramOfLastSentImage = null;
  private float   lastHistogramDistance = 0f;

  //private DatabaseAdapterDecorator databaseAdapter;
  //private Search searchResult;
  
  private Context context;
  private Location location;
  private ImageScaler imageScaler;

  // TODO to delete
  private int NO_IMG_SENT = 0;

  public KooabaRecognizer(Context context, Location location) {
	  this.location = location;
	  this.context = context;
	  imageScaler = new ImageScaler(KConfig.getConfig().getScale(), KConfig.getConfig().getJpegQuality());
	  //databaseAdapter = new DatabaseAdapterDecorator(new SimpleDatabaseAdapter(context));
  }

  /**
   * Try to recognize an images.
   * @param image byte array representation of the image
   * @param width width of the image, in pixels
   * @param height height of the image, in pixels
   * @return a Search containing data about what was recognized
   * @throws IOException
   * @throws NoSuchAlgorithmException
   * @throws JSONException
   */
  
  public Search recognize(byte[] image, int width, int height)
          throws IOException, NoSuchAlgorithmException, JSONException{

    byte[] compressedImage = compressImage(image, width, height);

    if (shouldSendForRecognition(compressedImage)) {
      NO_IMG_SENT++;
      histogramOfLastSentImage = ImageComparer.computeHistogram(compressedImage);
      return sendForRecognition(compressedImage);
    }
    /**
     * Sends null if the image is not sent for recognition
     */
    return null;
  }

  /**
   * Not every image needs to be sent for recognition. For example, if the previous image which was sent is very similar
   * to the image we want to send now, there's no point in sending it.
   * @param currentImage the byte[] representation of the image
   * @return true if image should be sent to recognition, false otherwise
   * @see com.scm.reader.livescanner.sdk.image.ImageComparer
   */
  public boolean shouldSendForRecognition(byte[] currentImage) {
    if (histogramOfLastSentImage == null) {
      histogramOfLastSentImage = ImageComparer.computeHistogram(currentImage);
      return true;
    } else {
      lastHistogramDistance = ImageComparer.imageDistance(currentImage, histogramOfLastSentImage);
      return lastHistogramDistance < KConfig.getConfig().getHistogramThreshold(); // the images are very different
    }
  }

  /**
   * Tries to recognize the image. It first performs a rotation of image,
   * then creates new Search object and executes the imageRecognition
   */
  //remove this?
  public Search sendForRecognition(byte image[])
          throws IOException, NoSuchAlgorithmException, JSONException{
	  
	  	Log.d("KooabaRecognizer", "send for recognition");	  
	  	Search search = null;
		ImageRecognizer imageRecognizer = new ImageRecognizer();
		
		Bitmap img = null; 
		img =  BitmapFactory.decodeByteArray(image, 0, image.length);
		
		try {
			img = fixRotation(img);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		search = createNewSearch(img, search);
		return executeSearch(search, imageRecognizer);
  }

  	/**
   * Helper function to compress an image.
   */
  public byte[] compressImage(byte[] imageData, int resolutionX, int resolutionY) {
    byte[] image = imageScaler.compress(imageData, resolutionX, resolutionY);
    return image;
  }

  // TODO temp methods
  public int getNoImgSent() {
    return NO_IMG_SENT;
  }

  public float getLastHistogramDistance() {
    return lastHistogramDistance;
  }
  
  private Search createNewSearch(Bitmap img, Search search) {
	  if (isDebugLog()) {
	    logDebug("Creating and saving new search");
	  }
	  ByteArrayOutputStream imgBytes = new ByteArrayOutputStream();
	  img.compress(JPEG, KConfig.getConfig().getUploadJpegQuality(), imgBytes);
	  search = new Search(context.getString(R.string.image_not_sent), imgBytes.toByteArray(), new Date(), true);
	  
	  if (location != null) {
		 search.setLatitude(location.getLatitude());
		 search.setLongitude(location.getLongitude());
	  }
	  return search;
  }
  
  private Bitmap fixRotation(Bitmap img) throws IOException {
	    //get WINDOW_SERVICE
	     Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

	      if (isDebugLog()) {
	        logDebug("Display orientation is " + display.getOrientation());
	      }

	      Matrix m = new Matrix();

	      int angle = 0;

	      //TODO: refactor
	      if (img.getWidth() > img.getHeight()) {
	         if (display.getOrientation() == ROTATION_0) {
	           angle = 90;
	         } else if (display.getOrientation() == ROTATION_270) {
	           angle = 180;
	         }
	      } else {
	        if (display.getOrientation() == ROTATION_90) {
	          angle = -90;
	        } else if (display.getOrientation() == ROTATION_270) {
	          angle = 90;
	        }
	      }

	      if (isDebugLog()) {
	        logDebug("Preview image will be rotated by " + angle + " degrees");
	      }

	      m.postRotate(angle);
	      return Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), m, true);
  }
  
  private Search executeSearch(Search search, ImageRecognizer imageRecognizer) {
      if (isDebugLog()) {
        logDebug("Searching");
      }
      try {
        search = imageRecognizer.query(context, search);
      } catch (IOException e) {
        return null;
      }
      if (isDebugLog()) {
          logDebug("Updating search");
      }
      if (search == null) {
        return null;
      }
      return search;
  }
  
 }
