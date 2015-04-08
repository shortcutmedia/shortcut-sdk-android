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

package com.scm.reader.livescanner.sdk;


import android.app.Activity;
import android.location.Location;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;

import com.scm.reader.livescanner.sdk.camera.CameraManager;
import com.scm.shortcutreadersdk.R;

import java.io.IOException;

/**
 * <p>KooabaScanner is the main object which encapsulates the entire scanning process. It should be started from
 * within an activity.<br />
 * First one needs to instantiate an object within an activity.</p>
 *
 * <code>KooabaScanner kScanner = new KooabaScanner(Activity);</code>
 *
 * <p>It should be started when the scanning process takes place.</p>
 *
 * <code>kScanner.Start();</code>
 *
 * <p>The activity needs to implement the scanning events:</p>
 *
 * <ul>
 *  <li>onImageRecognized()</li>
 *  <li>onImageNotRecognized()</li>
 *  <li>onError()</li>
 *  <li>onInfo() - might not be needed</li>
 * </ul>
 */
public class KooabaScanner implements SurfaceHolder.Callback {
  private final static String TAG = SurfaceHolder.class.getSimpleName();

  private Activity activity;
  private SurfaceView surfaceView;
  private Location mLocation;

  private boolean hasSurface;
  private CameraManager cameraManager;
  private ScanHandler scanHandler;

  private KConfig config = KConfig.getConfig();

  private KEventListener kEventListener;

  /**
   * Initializes a KooabaScanner object with an activity as a reference. This object will be
   * used to start, stop and resume the recognition process.
   * @param act the activity from which the scanner has been initialized.
   * @param location the location which is retrieved in ScanActivity
   */
  public KooabaScanner(Activity act, Location location) {
    activity   = act;
    mLocation = location;
    hasSurface = false;

    prepareWindow();
  }

  private void prepareWindow() {
    // Hide the window title
    activity.requestWindowFeature(Window.FEATURE_NO_TITLE);
    activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

    // keep window while user is scanning
    Window window = activity.getWindow();
    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
  }

  /**
   * Returns a reference to the configuration object for the current KooabaScanner.
   * @return a KConfig object.
   */
  public KConfig getConfig() {
    return config;
  }

  /**
   * Sets the event listener which will handle the recognition events.
   * @see KEventListener
   * @param eventListener the listener which will handler recognition events.
   */
  public void setKEventListener(KEventListener eventListener) {
    kEventListener = eventListener;
  }

  /**
   * Starts the actual recognition process. A new activity will be started which will start the camera
   * preview on the whole screen.
   * @param surfaceView the surface view to draw the camera preview frames on.
   */
  public void start(SurfaceView surfaceView, Location location) {
    this.surfaceView = surfaceView;
    //this.surfaceView.bringToFront();

    // CameraManager must be initialized here, not in onCreate(). This is necessary because we don't
    // want to open the camera driver and measure the screen size if we're going to show the help on
    // first launch. That led to bugs where the scanning rectangle was the wrong size and partially
    // off screen.
    cameraManager = new CameraManager(activity.getApplication());

    // SurfaceView surfaceView = (SurfaceView) activity.findViewById(R.id.preview_view);
    SurfaceHolder surfaceHolder = surfaceView.getHolder();
    if (hasSurface) {
      // The activity was paused but not stopped, so the surface still exists. Therefore
      // surfaceCreated() won't be called, so init the camera here.
      initCamera(surfaceHolder);
    } else {
      // Install the callback and wait for surfaceCreated() to init the camera.
      surfaceHolder.addCallback(this);
      surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }
  }

  /**
   * Synchronously stop the recognition process. Return to the previous activity. This is a clean
   * way to stop the recognition process.
   */
  public void stop() {
    if (scanHandler != null) {
      scanHandler.quitSynchronously();
      scanHandler = null;
    }
    cameraManager.closeDriver();
    if (!hasSurface) {
      SurfaceHolder surfaceHolder = surfaceView.getHolder();
      surfaceHolder.removeCallback(this);
    }
  }

  public void resume() {
    if (scanHandler != null) {
      Message msg = Message.obtain(scanHandler, R.id.restart_recognition);
      msg.sendToTarget();
    }
  }

  /**
   * This function should not be public...
   * @param holder
   */
  @Override
  public void surfaceCreated(SurfaceHolder holder) {
    if (holder == null) {
      Log.e(TAG, "*** WARNING *** surfaceCreated() gave us a null surface!");
    }
    if (!hasSurface) {
      hasSurface = true;
      initCamera(holder);
    }
  }

  @Override
  public void surfaceDestroyed(SurfaceHolder holder) {
    hasSurface = false;
  }

  @Override
  public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

  }
  
  private void initCamera(SurfaceHolder surfaceHolder) {
    if (surfaceHolder == null) {
      throw new IllegalStateException("No SurfaceHolder provided");
    }
    try {
      cameraManager.openDriver(surfaceHolder);
      // Creating the handler starts the preview, which can also throw a RuntimeException.
      if (scanHandler == null) {
        scanHandler = new ScanHandler(cameraManager, kEventListener, activity, mLocation);
      }
      // decodeOrStoreSavedBitmap(null, null);
    } catch (IOException ioe) {
      Log.w(TAG, ioe);
      // TODO fix these exceptions - send messages to users     displayFrameworkBugMessageAndExit();
    } catch (RuntimeException e) {
      // Barcode Scanner has seen crashes in the wild of this variety:
      // java.?lang.?RuntimeException: Fail to connect to camera service
      Log.w(TAG, "Unexpected error initializing camera", e);
      //displayFrameworkBugMessageAndExit();
    }
  }
}
