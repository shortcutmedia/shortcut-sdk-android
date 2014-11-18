package com.scm.reader.livescanner.sdk;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.scm.reader.livescanner.sdk.recognizers.KooabaRecognizer;
import com.scm.reader.livescanner.sdk.recognizers.ZXingRecognizer;
import com.scm.reader.livescanner.search.Search;
import com.scm.reader.livescanner.sensors.AccelerationListener;
import com.scm.shortcutreadersdk.R;

import java.util.Map;

/**
 * Handler for the QueryThread. This handler sends query images received from ScanShandler
 * for recognition. Then it communicates the recognition status or an error back to the ScanHandler.
 * It is the entity which deals with recognition on a high level basis.
 */
public class QueryHandler extends Handler {
  protected static final String TAG = QueryHandler.class.getSimpleName();

  protected ScanHandler scanHandler;
  protected boolean running = true;

  protected SharedPreferences preferences;
  protected AccelerationListener accelerationListener;
  
  // recognition methods
  protected ZXingRecognizer  zXingRecognizer;
  protected KooabaRecognizer kooabaRecognizer;
  
  protected Message message;

  protected KInfo kInfo; // reuse same info object for all infos which need to be sent.
  
  public boolean waiting = false;

  /**
   * Initialize a new QueryHandler.
   * @param handler a reference to the ScanHandler, to which it communicates the recognition
   *                responses.
   * @param hints object needed by the ZXing recognition. See the ZXing project source for
   *              more info
   */
  
  
  QueryHandler(ScanHandler handler, Map<DecodeHintType,Object> hints) {
	  
    scanHandler = handler;
    kInfo = new KInfo();
    
    MultiFormatReader multiFormatReader = new MultiFormatReader();
    multiFormatReader.setHints(hints);
    
    accelerationListener = new AccelerationListener(scanHandler.getContext());
    accelerationListener.register();

    zXingRecognizer = new ZXingRecognizer(scanHandler.getCameraManager(), multiFormatReader);
    kooabaRecognizer = new KooabaRecognizer(scanHandler.getContext(), scanHandler.getLocation());
    
  }

  /**
   * Handler messages from the ScanHandler. These can be "RECOGNIZE" = try to recognized an image
   * or "STOP_SCANNING" = instructs the handler to stop sending recognition messages.
   * add ONLY_QR
   * @param message the message received from ScanHandler.
   */
  
  @Override
  public void handleMessage(Message message) {
    if (!running) {
      return;
    }
    
    if(accelerationListener.isPhoneMoving()){
    	Log.d(TAG, "Device is moving");
    	//do not change the message we send, affects to ScanActivity
    	pauseKooabaRecognition("moving");
    	return;
    }
    Log.d(TAG, "Device is not moving");
    
    if (message.what == R.id.recognize) {
      Log.d(TAG, "Received message 'RECOGNiZE'. Proceed");

      int width    = message.arg1;
      int height   = message.arg2;
      byte[] image = (byte[]) message.obj;
      
      
      try {
        Search searchResult = null;
        
        //send thumbnail picture for zXing
        Bitmap barcodeBitmap = BitmapFactory.decodeResource(scanHandler.getContext().getResources(), R.drawable.barcode_thumnbail);
        
        // try to recognize with ZXing
        searchResult = zXingRecognizer.recognize(image, width, height, barcodeBitmap);
       
        if (!searchResult.isRecognized()) {
          // try to recognize with kooaba
          searchResult = kooabaRecognizer.recognize(image, width, height);
        }

        if(searchResult != null) {
	       	if (searchResult.isRecognized()) {
	           	replyWithRecognitionSucceeded(new KEvent(searchResult));
	         } else {
	        	 //replywithStopKrecog
	        	 pauseKooabaRecognition("SENT " + kooabaRecognizer.getNoImgSent() + " images");
	        	 return;
	         }
        } 
        //else reply with recognition failed

        replyWithInfo("SENT " + kooabaRecognizer.getNoImgSent() + " images" + "; \nD = " + kooabaRecognizer.getLastHistogramDistance());

      } catch (Exception e) {
        replyWithError(e);
        e.printStackTrace();
      }

    } else if (message.what == R.id.recognize_qr_only){
    	Log.d(TAG, "Received message 'RECOGNiZE QR ONLY'. Proceed");

        int width    = message.arg1;
        int height   = message.arg2;
        byte[] image = (byte[]) message.obj;
        
        Search searchResult = null;
        
        //send thumbnail picture for zXing
        Bitmap barcodeBitmap = BitmapFactory.decodeResource(scanHandler.getContext().getResources(), R.drawable.barcode_thumnbail);
        
        // try to recognize with ZXing
        searchResult = zXingRecognizer.recognize(image, width, height, barcodeBitmap);
        
        byte[] compressedImage = kooabaRecognizer.compressImage(image, width, height);
        
        if(kooabaRecognizer.shouldSendForRecognition(compressedImage)){
    		continueKooabaRecognition("SENT " + kooabaRecognizer.getNoImgSent() + " images");
    		return;
    	}else if (searchResult.isRecognized()) {
           	replyWithRecognitionSucceeded(new KEvent(searchResult));
        }else {
        	pauseKooabaRecognition("SENT " + kooabaRecognizer.getNoImgSent() + " images");
        }
        	
       
    	
    }else if (message.what == R.id.stop_scanning) {
      accelerationListener.unregister();
      running = false;
      Looper.myLooper().quit();
      Log.d(TAG, "Received stop_scanning. Stopping scanning");
    } 
    /**else if message.what == stopKrecog
     * do qr recognition
     * if(KooabaRecognizer.shouldSendImage()){
     * 		replyWithStartKrecog
     * }
     */
  }

  /**
   * Reply with some information text back to the ScanHandler.
   * @param info text information to send to the ScanHandler.
   */
  public void replyWithInfo(String info) {
    if (scanHandler != null) {
      Message message = Message.obtain(scanHandler, R.id.recognition_info);
      message.obj = info;
      message.sendToTarget();
      Log.d(TAG, "Replying with info");
    }
  }

  /**
   * Reply with an exception back to the ScanHandler.
   * @param e the exception to send to the ScanHandler.
   */
  
  public void replyWithError(Exception e) {
    if (scanHandler != null) {
      Message message = Message.obtain(scanHandler, R.id.recognition_error);
      message.obj = e;
      message.sendToTarget();
      Log.e(TAG, "Error: " + e.getClass().getSimpleName() + ": " + e.getMessage());
    }
  }

  /**
   * Reply to ScanHandler that recognition was succeeded.
   * @param event contains information about what was recognized and some other information
   *              (in the getKInfo()).
   */
  public void replyWithRecognitionSucceeded(KEvent event) {
    if (scanHandler != null) {
      Message message = Message.obtain(scanHandler, R.id.recognition_succeeded);
      message.obj = event;
      message.sendToTarget();
      Log.d(TAG, "Recognition succeeded: " + event.getSearch().getTitle());
    }
  }

  /**
   * Reply to ScanHandler that image was not recognized.
   * @param event information that image was not recognized.
   */
  public void replyWithRecognitionFailed(KEvent event) {
    if (scanHandler != null) {
      Message message = Message.obtain(scanHandler, R.id.recognition_failed);
      message.obj = event;
      message.sendToTarget();
      Log.d(TAG, "Recognition failed");
    }
  }
  
  public void pauseKooabaRecognition(String info){
	  if (scanHandler != null) {
	      Message message = Message.obtain(scanHandler, R.id.pause_kooaba_recognition);
	      message.obj = info;
	      message.sendToTarget();
	      Log.d(TAG, "Pause Kooaba recognition");
	    }
  }
  
  public void continueKooabaRecognition(String info){
	  if (scanHandler != null) {
	      Message message = Message.obtain(scanHandler, R.id.continue_kooaba_recognition);
	      message.obj = info;
	      message.sendToTarget();
	      Log.d(TAG, "Continue Kooaba recognition");
	    }
  }
}
