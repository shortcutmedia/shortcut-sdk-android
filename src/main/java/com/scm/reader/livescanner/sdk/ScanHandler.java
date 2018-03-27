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

import android.content.Context;
import android.location.Location;
import android.os.Handler;
import android.os.Message;

import com.scm.reader.livescanner.sdk.camera.CameraManager;
import com.scm.reader.livescanner.util.LogUtils;
import com.scm.shortcutreadersdk.R;

/**
 * Handler of the ScanActivity. Spawns another thread (QueryThread) and sends it frames for recognition.
 * It receives events with responses from this thread and sends them back to the activity.
 */
public class ScanHandler extends Handler {
  private static final String TAG = ScanHandler.class.getSimpleName();
  
  private enum State { PREVIEW, SUCCESS, FINISHED }

  private CameraManager cameraManager;
  private QueryThread queryThread;
  private State state;
  private Context context;
  private Location location;

  // registered event listener to send events (recognition, error etc.) to
  private KEventListener kEventListener;

  public ScanHandler(CameraManager camManager, KEventListener eventListener, Context context, Location location) {
    kEventListener = eventListener;
    queryThread = new QueryThread(this);
    queryThread.start();
    this.context = context;
    this.location = location;
    
    state = State.SUCCESS;

    cameraManager = camManager;
    cameraManager.startPreview();
    restartPreviewAndRecognize();
  }

  /**
   * @param message: can be one of:
   * - auto_focus: handler will request another autofocus from the camera;
   * - recognition_succeeded: an image has been recognized;
   * - recognition_failed: the image was not recognized;
   * - recognition_error: there has been an error; (maybe introduce multiple types of errors)
   * - recognition_info: received some information;
   * - restart_recognition: restarts the recognition process;
   */
  @Override
  public void handleMessage(Message message) {
    if (message.what == R.id.shortcut_sdk_auto_focus) {
        // When one auto focus pass finishes, start another -> continuous AF.
        if (state == State.PREVIEW) {
            cameraManager.requestAutoFocus(this, R.id.shortcut_sdk_auto_focus);
        }
    } else if (message.what == R.id.shortcut_sdk_restart_recognition) {
        restartPreviewAndRecognize();
        LogUtils.logDebug(TAG, "RECEIVED restart recognition. Trying to recognize");
    } else if (message.what == R.id.shortcut_sdk_continue_kooaba_recognition) {
        LogUtils.logDebug(TAG, "RECEIVED continue kooaba recognition");
        cameraManager.requestPreviewFrame(queryThread.getHandler(), R.id.shortcut_sdk_recognize);
        kEventListener.onContinueKooabaRecognition((String) message.obj);
    } else if (message.what == R.id.shortcut_sdk_pause_kooaba_recognition) {
        LogUtils.logDebug(TAG, "RECEIVED continue kooaba recognition");
        cameraManager.requestPreviewFrame(queryThread.getHandler(), R.id.shortcut_sdk_recognize_qr_only);
        kEventListener.onPauseKooabaRecognition((String) message.obj);
    } else if (message.what == R.id.shortcut_sdk_recognition_succeeded) {
        state = State.SUCCESS;
        LogUtils.logDebug(TAG, "RECEIVED recognition succeeded; Should stop!");
        kEventListener.onImageRecognized((KEvent) message.obj);
    } else if (message.what == R.id.shortcut_sdk_recognition_failed) {
        // Decode as fast as possible, so when one decode fails, start another.
        LogUtils.logDebug(TAG, "RECEIVED recognition failed; Trying again");
        state = State.PREVIEW;
        cameraManager.requestPreviewFrame(queryThread.getHandler(), R.id.shortcut_sdk_recognize);
        //cameraManager.requestPreviewFrame(queryThread.getHandler(), R.id.recognize_qr_only);
        kEventListener.onImageNotRecognized((KEvent) message.obj);
    } else if (message.what == R.id.shortcut_sdk_recognition_info) {
        LogUtils.logDebug(TAG, "RECEIVED recognition info; Trying again");
        kEventListener.onInfo((String) message.obj);
        if (state == State.PREVIEW) {
            cameraManager.requestPreviewFrame(queryThread.getHandler(), R.id.shortcut_sdk_recognize);
        }
    } else if (message.what == R.id.shortcut_sdk_recognition_error) {
        LogUtils.logDebug(TAG, "RECEIVED recognition error: Should stop!");
        kEventListener.onError((Exception) message.obj);
        cameraManager.requestPreviewFrame(queryThread.getHandler(), R.id.shortcut_sdk_recognize);
    }
  }

  private void restartPreviewAndRecognize() {
    if (state == State.SUCCESS) {
      state = State.PREVIEW;
      cameraManager.requestPreviewFrame(queryThread.getHandler(), R.id.shortcut_sdk_recognize);
      cameraManager.requestAutoFocus(this, R.id.shortcut_sdk_auto_focus);
    }
  }

  /**
   * Stop the recognition process (the camera preview and the queryThread).
   */
  public void quitSynchronously() {
    state = State.FINISHED;
    cameraManager.stopPreview();
    Message quit = Message.obtain(queryThread.getHandler(), R.id.shortcut_sdk_stop_scanning);
    quit.sendToTarget();
    try {
      // Wait at most half a second; should be enough time, and onPause() will timeout quickly
      queryThread.join(500L);
    } catch (InterruptedException e) {
      // continue
    }
    LogUtils.logDebug(TAG, "The thread should have finished");
    // Be absolutely sure we don't send any queued up messages
    removeMessages(R.id.shortcut_sdk_recognition_failed);
    removeMessages(R.id.shortcut_sdk_recognition_succeeded);
  }

  public CameraManager getCameraManager() {
    return cameraManager;
  }
  
  public Context getContext(){
	  return this.context;
  }
  
  public Location getLocation(){
	  return this.location;
  }
}
