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

package com.scm.reader.livescanner.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.scm.reader.livescanner.sdk.KConfig;
import com.scm.reader.livescanner.sdk.KEvent;
import com.scm.reader.livescanner.sdk.camera.LegacyCamera;
import com.scm.reader.livescanner.sdk.recognizers.ZXingRecognizer;
import com.scm.reader.livescanner.search.ImageRecognizer;
import com.scm.reader.livescanner.search.ImageScaler;
import com.scm.reader.livescanner.search.Search;
import com.scm.reader.livescanner.search.UriImage;
import com.scm.reader.livescanner.util.LogUtils;
import com.scm.reader.livescanner.util.Utils;
import com.scm.shortcutreadersdk.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

import static android.graphics.Bitmap.CompressFormat.JPEG;
import static android.graphics.BitmapFactory.decodeByteArray;
import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static android.view.KeyEvent.KEYCODE_DPAD_CENTER;
import static android.view.Surface.ROTATION_0;
import static android.view.Surface.ROTATION_270;
import static android.view.Surface.ROTATION_90;
import static android.view.SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS;
import static com.scm.reader.livescanner.util.LogUtils.isDebugLog;
import static com.scm.reader.livescanner.util.LogUtils.logDebug;
import static com.scm.reader.livescanner.util.LogUtils.logError;

/**
 * Created by franco on 09/12/14.
 */
public class CameraView extends ShortcutSearchView implements SurfaceHolder.Callback {

    public static final String TAG = "com.scm.reader.livescanner.CameraView";

    public static final String TMP_FILE_PREFIX = "ShortcutCamera";

    private SurfaceView mSurfaceView;
    private ImageView mPreviewView;
    private int mScreenWidth;
    private int mScreenHeight;

    private LegacyCamera mCamera;
    private Uri rawCameraResultUri;
    protected OrientationEventListener orientationListener;
    private SearchTask mSearchTask;
    private Handler handler = new Handler();


    public CameraView(Activity holdingActivity) {
        this(holdingActivity, null);
    }

    public CameraView(Activity holdingActivity, Location location) {
        super(holdingActivity, location);
    }

    //region LIFECYCLE methods
    @Override
    public void onResume() {
        super.onResume();

        // CameraManager must be initialized here, not in onCreate(). This is necessary because we don't
        // want to open the camera driver and measure the screen size if we're going to show the help on
        // first launch. That led to bugs where the scanning rectangle was the wrong size and partially
        // off screen.
        //cameraManager = new CameraManager(getApplication());

        mCamera = new LegacyCamera(mSurfaceView, mScreenWidth, mScreenHeight, mJPEGCallback );

        showAllViews();
    }

    @Override
    public void onPause() {
        super.onPause();
        mCamera.stopCamera();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mCamera.onDestroy();
    }
    //endregion

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (!mCamera.isCameraStarted()) {
            return false;
        }

        if (keyCode == KEYCODE_DPAD_CENTER) {
            mCamera.doFocusAndTakePicture();
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_FOCUS) {
            mCamera.doFocusAndWaitForConfirmation();
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_CAMERA) {
            mCamera.takePicture();
            return true;
        }
        return false;
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (!mCamera.isCameraStarted()) {
            return false;
        }

        if (keyCode == KeyEvent.KEYCODE_FOCUS) {
            mCamera.cancelFocus();
            return true;
        }
        return false;
    }

    protected void initializeWindow() {

        WindowManager manager = (WindowManager) mHoldingActivity.getSystemService(Context.WINDOW_SERVICE);
        mScreenWidth = Utils.getScreenResolution(manager).x;
        mScreenHeight = Utils.getScreenResolution(manager).y;

        LayoutInflater inflater = mHoldingActivity.getLayoutInflater();
        mHoldingActivity.setContentView(R.layout.shortcut_sdk_camera);

        mSurfaceView = (SurfaceView) mHoldingActivity.findViewById(R.id.camerasurface);
        mSurfaceView.getHolder().addCallback(this);
        mSurfaceView.getHolder().setType(SURFACE_TYPE_PUSH_BUFFERS);

        mPreviewView = (ImageView)  mHoldingActivity.findViewById(R.id.upload_image);

        final ImageButton button = (ImageButton) mHoldingActivity.findViewById(R.id.take_picture_button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                mCamera.doFocusAndTakePicture();
            }
        });

        super.initializeWindow();

        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    Thread.sleep(9000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                mHoldingActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mHoldingActivity.findViewById(R.id.take_picture_instructions).setVisibility(View.GONE);
                    }
                });
            }
        }).start();

    }

    public void surfaceCreated(SurfaceHolder holder) {
        if (isDebugLog()) {
            logDebug("CameraActivity surfaceCreated");
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        if (isDebugLog()) {
            logDebug("CameraActivity surfaceDestroyed");
        }
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int surfaceWidth, int surfaceHeight) {
        if (isDebugLog()) {
            logDebug("CameraActivity surfaceChanged" +
                    ": cameraStarted=" + mCamera.isCameraStarted() +
                    ", width=" + surfaceWidth +
                    ", height=" + surfaceHeight +
                    ", holder surface=" + holder.getSurface() +
                    ", holder surface frame=" + holder.getSurfaceFrame());
        }

        startCamera();

    }

    private void showAllViews() {
        View instructionsView = mHoldingActivity.findViewById(R.id.take_picture_instructions);
        instructionsView.setVisibility(View.VISIBLE);
        instructionsView.bringToFront();
        mHoldingActivity.findViewById(R.id.camera_view).setVisibility(View.VISIBLE);
    }


    private Camera.PictureCallback mJPEGCallback = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera callbackCamera) {

            Uri rawImageURI = rawCameraResultUri;

            if (rawImageURI != null) {
                try {
                    OutputStream outputStream = mHoldingActivity.getContentResolver().openOutputStream(rawImageURI);
                    try {
                        outputStream.write(data);
                    } finally {
                        outputStream.close();
                    }

                    mSearchTask = (SearchTask) new SearchTask(rawImageURI).execute();

//                    showSearchScreen();
                } catch (IOException ex) {
                    logError("Could not save full sized image to " + rawImageURI, ex);
                }

            }

        }
    };

    void showSearchScreen(byte[] img) {
        showSearchScreen(decodeByteArray(img, 0, img.length));
    }

    void showSearchScreen(final Bitmap img) {
        handler.post(new Runnable() {
            public void run() {
                View takePictureBar = mHoldingActivity.findViewById(R.id.take_picture_layout);
                takePictureBar.setVisibility(View.GONE);
                View cameraUploading = mHoldingActivity.findViewById(R.id.camera_uploading);
                cameraUploading.setVisibility(View.VISIBLE);

                mPreviewView.setImageBitmap(img);
                mPreviewView.setVisibility(View.VISIBLE);
                mSurfaceView.setVisibility(View.GONE);
            }
        });
    }

    void hideSearchScreen() {
        handler.post(new Runnable() {
            public void run() {

                View takePictureBar = mHoldingActivity.findViewById(R.id.take_picture_layout);
                takePictureBar.setVisibility(View.VISIBLE);
                View cameraUploading = mHoldingActivity.findViewById(R.id.camera_uploading);
                cameraUploading.setVisibility(View.GONE);


                mPreviewView.setVisibility(View.GONE);
                mSurfaceView.setVisibility(View.VISIBLE);
            }
        });

    }

    private void startCamera() {
        if (createCameraResult()) {
            mCamera.startCamera();
        }
    }

    private boolean createCameraResult() {
        try {
            rawCameraResultUri = Uri.fromFile(File.createTempFile(TMP_FILE_PREFIX, null));
        } catch (IOException e) {
            logError("Could not create temp file", e);
            return false;
        }

        return true;
    }

    private void deleteRawCameraResult() {
        if (rawCameraResultUri == null) {
            return;
        }

        if (isDebugLog()) {
            logDebug("Removing full sized camera result from " + rawCameraResultUri);
        }

        File file = new File(rawCameraResultUri.getPath());
        if (!file.delete()) {
            Log.i(TAG, "Could not delete file" + rawCameraResultUri.getPath());
        }

        rawCameraResultUri = null;
    }



    private class SearchTask extends AsyncTask<Void, Void, Search> {

        private Search search;
        private Object mutex = new Object();
        private ImageRecognizer imageRecognizer = new ImageRecognizer();
        private ZXingRecognizer zXingRecognizer = new ZXingRecognizer();
        private byte[] rawCameraResult;
        private Uri mRawImageURI;


        public SearchTask() {

        }

        public SearchTask(Search search) {
            this.search = search;
        }

        public SearchTask(byte[] data) {
            rawCameraResult = data;
        }

        public SearchTask(Uri rawImageURI)  {
            mRawImageURI = rawImageURI;
        }

        @Override
        protected Search doInBackground(Void... data) {

            try {


                synchronized (mutex) {

                    if (search == null) {
                        Bitmap img = null;

                        img = scale();

                        img = fixRotation(img);

                        showSearchScreen(img);
                        createAndSaveNewSearch(img);
                    } else {
                        if (isDebugLog()) {
                            logDebug("Resuming " + this.getClass().getSimpleName() + " with " + search);
                        }
                        showSearchScreen(search.getImage());
                    }
                }

                if (isCancelled()) {
                    if (isDebugLog()) {
                        logDebug("Cancelling task after scaling");
                    }
                    hideSearchScreen();
                    return null;
                }

                if (search.isPending() && !executeSearch()) {
                    hideSearchScreen();
                    return null;
                }

                if (isCancelled()) {
                    if (isDebugLog()) {
                        logDebug("Cancelling task after search");
                    }
                    hideSearchScreen();
                    return null;
                }

                return search;
            } catch (IOException fnfe) {
                logError("Exception scaling original image", fnfe);
                hideSearchScreen();
                return null;
            }
        }

        public void cancel() {
            logDebug("Cancelling " + this.getClass().getSimpleName());
            cancel(true);
            imageRecognizer.cancelRequest();
        }

        private boolean executeSearch() {
            if (isDebugLog()) {
                logDebug("Searching");
            }
            try {
                Search qrSearch = zXingRecognizer.recognize(search.getImage());
                if(qrSearch.isRecognized()){
                    //Bitmap thumbnail for barcode
                    Bitmap barcodeBitmap = BitmapFactory.decodeResource(mHoldingActivity.getBaseContext().getResources(), R.drawable.shortcut_sdk_barcode_thumnbail);
                    search.modifyToQRSearch(qrSearch, barcodeBitmap);
                }else{
                    search = imageRecognizer.query(mHoldingActivity, search);
                }

            } catch (IOException e) {
                return false;
            }

            return true;
        }

        private Bitmap scale() throws FileNotFoundException {
            ImageScaler imageScaler = new UriImage(mRawImageURI, mHoldingActivity);
            return imageScaler.getScaledImage(KConfig.getConfig().getUploadJpegMaxWidthHeight());
        }

        private Bitmap fixRotation(Bitmap img) throws IOException {

            Display display = ((WindowManager) mHoldingActivity.getSystemService(mHoldingActivity.WINDOW_SERVICE)).getDefaultDisplay();

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

        private void createAndSaveNewSearch(Bitmap img) {
            if (isDebugLog()) {
                logDebug("Creating and saving new search");
            }
            ByteArrayOutputStream imgBytes = new ByteArrayOutputStream();
            img.compress(JPEG, KConfig.getConfig().getUploadJpegQuality(), imgBytes);
            search = new Search(mHoldingActivity.getString(R.string.shortcut_sdk_image_not_sent), imgBytes.toByteArray(), new Date(), true);

            if (mLocation != null) {
                search.setLocation(mLocation);
            }

            deleteRawCameraResult();
        }

        @Override
        protected void onPostExecute(Search result) {
            super.onPostExecute(result);

            if (isCancelled()) { return; }

            KEvent event = new KEvent(search);

            if (result != null && result.isRecognized()) {
                mRecognitionCallbacks.onImageRecognized(event);
            } else {
                hideSearchScreen();
                mRecognitionCallbacks.onImageNotRecognized(event);
            }
        }

        public Search getSearch() {
            synchronized (mutex) {
                return search;
            }
        }
    }

}
