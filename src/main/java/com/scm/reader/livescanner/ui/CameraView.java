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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.TextureView;
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
import com.scm.shortcutreadersdk.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

import static android.graphics.Bitmap.CompressFormat.JPEG;
import static android.graphics.BitmapFactory.decodeByteArray;
import static android.view.KeyEvent.KEYCODE_DPAD_CENTER;
import static android.view.Surface.ROTATION_0;
import static android.view.Surface.ROTATION_270;
import static android.view.Surface.ROTATION_90;
import static com.scm.reader.livescanner.util.LogUtils.isDebugLog;
import static com.scm.reader.livescanner.util.LogUtils.logDebug;
import static com.scm.reader.livescanner.util.LogUtils.logError;

/**
 * Created by franco on 09/12/14.
 */
public class CameraView extends ShortcutSearchView implements TextureView.SurfaceTextureListener {
    public static final String TAG = "livescanner.CameraView";
    private static final String TMP_FILE_PREFIX = "ShortcutCamera";
    private TextureView mTextureView;
    private ImageView mPreviewView;
    private LegacyCamera mCamera;
    private Uri rawCameraResultUri;
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
        if (mCamera != null)
            mCamera.startCamera();
        showAllViews();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mCamera != null)
            mCamera.stopCamera();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mCamera != null)
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
        mHoldingActivity.setContentView(R.layout.shortcut_sdk_camera);

        mTextureView = (TextureView) mHoldingActivity.findViewById(R.id.cameratexture);
        mTextureView.setSurfaceTextureListener(this);

        mPreviewView = (ImageView) mHoldingActivity.findViewById(R.id.upload_image);

        final ImageButton button = (ImageButton) mHoldingActivity.findViewById(R.id.take_picture_button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                mCamera.doFocusAndTakePicture();
            }
        });

        super.initializeWindow();

        mTextureView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mHoldingActivity.findViewById(R.id.take_picture_instructions).setVisibility(View.GONE);
            }
        }, 9000);
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
                    new SearchTask(rawImageURI).execute();
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
                mTextureView.setVisibility(View.GONE);
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
                mTextureView.setVisibility(View.VISIBLE);
            }
        });
    }

    private void startCamera() {
        if (createCameraResult()) {
            if (mCamera == null) {
                mCamera = new LegacyCamera(mTextureView, mHoldingActivity, mJPEGCallback);
            }
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

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        startCamera();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        // Ignored, Camera does all the work for us
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        if (mCamera != null) {
            mCamera.stopCamera();
        }
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
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

        public SearchTask(Uri rawImageURI) {
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
        }

        private boolean executeSearch() {
            if (isDebugLog()) {
                logDebug("Searching");
            }
            try {
                Search qrSearch = zXingRecognizer.recognize(search.getImage());
                if (qrSearch.isRecognized()) {
                    //Bitmap thumbnail for barcode
                    Bitmap barcodeBitmap = BitmapFactory.decodeResource(mHoldingActivity.getBaseContext().getResources(), R.drawable.shortcut_sdk_barcode_thumnbail);
                    search.modifyToQRSearch(qrSearch, barcodeBitmap);
                } else {
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
            hideSearchScreen();
            createCameraResult();
            startCamera();
            if (isCancelled()) {
                return;
            }

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
