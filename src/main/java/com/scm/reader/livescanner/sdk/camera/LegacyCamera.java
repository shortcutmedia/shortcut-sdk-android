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

package com.scm.reader.livescanner.sdk.camera;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Matrix;
import android.graphics.Point;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.view.Display;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.scm.reader.livescanner.sdk.KConfig;
import com.scm.reader.livescanner.util.PictureUtils;
import com.scm.shortcutreadersdk.R;

import java.util.Collections;
import java.util.List;

import static android.media.AudioManager.STREAM_SYSTEM;
import static android.view.OrientationEventListener.ORIENTATION_UNKNOWN;
import static com.scm.reader.livescanner.util.LogUtils.isDebugLog;
import static com.scm.reader.livescanner.util.LogUtils.logDebug;
import static com.scm.reader.livescanner.util.LogUtils.logError;
import static com.scm.reader.livescanner.util.LogUtils.logInfo;
import static com.scm.reader.livescanner.util.LogUtils.logWarn;

/**
 * Controls the camera
 */
public class LegacyCamera {

    private Camera mCamera;
    private boolean isCameraStarted;
    private boolean isPicutreTakingInProgress;

    private SurfaceView mSurfaceView;
    private int mSurfaceWidth;
    private int mSurfaceHeight;

    private int lastOrientation = ORIENTATION_UNKNOWN;
    private KooabaCameraOrientationEventListener orientationListener;

    private Camera.PictureCallback mPictureCallback;
    private MediaPlayer clickSound;

    public LegacyCamera(SurfaceView surfaceView, int surfaceWidth, int surfaceHeight, Camera.PictureCallback callback) {
        mSurfaceView = surfaceView;
        mSurfaceWidth = surfaceWidth;
        mSurfaceHeight = surfaceHeight;
        mPictureCallback = callback;

        orientationListener = new KooabaCameraOrientationEventListener(mSurfaceView.getContext());
        orientationListener.enable();
        new LoadShutterSoundThread().start();
    }

    public void startCamera() {
        if (isDebugLog()) {
            logDebug("startCamera() cameraStarted=" + isCameraStarted);
        }

        if (isCameraStarted) {
            stopCamera();
        }

        orientationListener.enable();

        mCamera = Camera.open();
        Camera.Parameters p = mCamera.getParameters();

        configurePreviewSize(mSurfaceWidth, mSurfaceHeight, p);
        configureFrameRate(p);
        configureFlashMode(p);
        configureFocusMode(p);
        configureRotation(p, mCamera);
        configureJPEGQuality(p);
        configurePictureSize(p);

        mCamera.setParameters(p);

        setPreviewDisplay(mSurfaceView.getHolder());

        mCamera.startPreview();
        mCamera.setErrorCallback(new Camera.ErrorCallback() {

            public void onError(int i, Camera camera) {
                if (Camera.CAMERA_ERROR_SERVER_DIED == i) {
                    logWarn("Camera server died, restarting camera");
                    stopCamera();
                    startCamera();
                }
            }
        });

        isCameraStarted = true;
    }

    public void stopCamera() {
        if (isDebugLog()) {
            logDebug("stopCamera");
        }

        if (isCameraStarted) {

            cancelFocus();

            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
            orientationListener.disable();
            isCameraStarted = false;

        }
    }

    public void onDestroy() {
        if (clickSound != null) {
            clickSound.release();
        }
    }

    public void doFocusAndWaitForConfirmation() {
        doFocus(new AutoFocusCallback() {

            public void onAutoFocus(boolean flag, Camera camera) {
            }

        });
    }


    public void doFocusAndTakePicture() {
        doFocus(new AutoFocusCallback());
    }

    public void takePicture() {
//        hideAllViews(); // FIXME: is this needed? The view does seem just fine without hiding.
        mCamera.takePicture(
                new ShutterSound(),
                null,
                mPictureCallback);
    }

    public boolean isCameraStarted() {
        return isCameraStarted;
    }

    private void doFocus(AutoFocusCallback callback) {
        if (isPicutreTakingInProgress) {
            if (isDebugLog()) {
                logDebug("Cancelling picure taking, as it is already in progress");
            }
            return;
        }

        isPicutreTakingInProgress = true;

        if (Camera.Parameters.FOCUS_MODE_AUTO.equals(mCamera.getParameters().getFocusMode())) {
            mCamera.autoFocus(callback);
            return;
        }

        callback.onAutoFocus(false, mCamera);
    }

    protected void configurePreviewSize(int surfaceWidth, int surfaceHeight, Camera.Parameters p) {
        int previewWidth = surfaceWidth;
        int previewHeight = surfaceHeight;

        List<Camera.Size> supportedSizes = p.getSupportedPreviewSizes();

        if (supportedSizes != null) {
            Camera.Size optimalSize = getOptimalPreviewSize(supportedSizes, previewWidth, previewHeight);
            if (optimalSize != null) {
                previewWidth = optimalSize.width;
                previewHeight = optimalSize.height;
            }
        }

        if (isDebugLog()) {
            logDebug("surface size: " + surfaceWidth + "/" + surfaceHeight + ", image size: " + previewWidth + "/" + previewHeight);
        }
        p.setPreviewSize(previewWidth, previewHeight);
        new Point(previewWidth, previewHeight);
    }

    protected void configureFrameRate(Camera.Parameters p) {
        List<Integer> frameRates = p.getSupportedPreviewFrameRates();
        if (frameRates != null) {
            Integer max = Collections.max(frameRates);
            p.setPreviewFrameRate(max);
        }
    }

    protected void configureFlashMode(Camera.Parameters p) {
        if (isSupported(Camera.Parameters.FLASH_MODE_AUTO, p.getSupportedFlashModes())) {
            p.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
        }
    }

    protected void configureFocusMode(Camera.Parameters p) {
        if (isSupported(Camera.Parameters.FOCUS_MODE_AUTO, p.getSupportedFocusModes())) {
            logInfo("Device supports auto focus, enabling it");
            p.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        }
    }

    protected void configureRotation(Camera.Parameters p, Camera camera) {
        p.setRotation(PictureUtils.roundOrientation(lastOrientation + 90));

        Display display = ((WindowManager) mSurfaceView.getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        camera.setDisplayOrientation(calculateRotation(display));
    }

    private int calculateRotation(Display display) {
        int rotation = 0;
        switch (display.getRotation()) {
            case Surface.ROTATION_0:
                rotation = 0;
                break;
            case Surface.ROTATION_90:
                rotation = 90;
                break;
            case Surface.ROTATION_180:
                rotation = 180;
                break;
            case Surface.ROTATION_270:
                rotation = 270;
                break;
        }
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(0, info);

        int finalRotation;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            finalRotation = (info.orientation + rotation) % 360;
            finalRotation = (360 - finalRotation) % 360;  // compensate the mirror
        } else {  // back-facing
            finalRotation = (info.orientation - rotation + 360) % 360;
        }
        return finalRotation;
    }

    protected void configureJPEGQuality(Camera.Parameters p) {
        p.setJpegQuality(KConfig.getConfig().getUploadJpegQuality());
    }

    protected void configurePictureSize(Camera.Parameters p) {
        // according to this http://stackoverflow.com/questions/6982366/the-picture-taken-by-camera-of-android-2-3-3-cant-display-normally
        // setPictureSize always needs to be set in Android 2.3.3;

        // This condition returns the url where the image will be stored. I don't see any connection between
        // it and setPictureSize. Picture size needs to be set anyways.
        //if (getIntent().getParcelableExtra(MediaStore.EXTRA_OUTPUT) == null) {
        Camera.Size picSize = p.getPictureSize();
        List<Camera.Size> supportedPictureSizes = p.getSupportedPictureSizes();
        if (supportedPictureSizes != null) {
            int diff = Integer.MAX_VALUE;
            //int sideSize = PaperboyApplication.UPLOADED_JPG_MAX_WIDTH_HEIGHT;
            int sideSize = 800;
            for (Camera.Size size : supportedPictureSizes) {
                if (size.width == sideSize) {
                    picSize = size;
                    break;
                }
                if (Math.abs(size.width - sideSize) < diff) {
                    diff = Math.abs(size.width - sideSize);
                    picSize = size;
                }
            }
            p.setPictureSize(picSize.width, picSize.height);
        }
    }

    public void cancelFocus() {
        if (isPicutreTakingInProgress) {
            if (isDebugLog()) {
                logDebug("Cancelling focus");
            }
            mCamera.cancelAutoFocus();
            isPicutreTakingInProgress = false;
        }
    }


    private void setPreviewDisplay(SurfaceHolder holder) {
        try {
            mCamera.setPreviewDisplay(holder);
        } catch (Throwable ex) {
            stopCamera();
            logError("setPreviewDisplay failed", ex);
            throw new RuntimeException("setPreviewDisplay failed", ex);
        }
    }


    /*
     * Original camera code from SDK 2.0.
     */
    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;
        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        // Try to find an size match aspect ratio and size
        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    private boolean isSupported(String value, List<String> supported) {
        return supported == null ? false : supported.indexOf(value) >= 0;
    }


    class KooabaCameraOrientationEventListener extends OrientationEventListener {

        public KooabaCameraOrientationEventListener(Context context) {
            super(context);
        }

        @Override
        public void onOrientationChanged(int orientation) {
            if (orientation != lastOrientation && orientation != ORIENTATION_UNKNOWN) {
                lastOrientation = orientation;

                int roundedOrientattion = PictureUtils.roundOrientation(orientation);
                if (isDebugLog()) {
                    logDebug("onOrientationChanged:" + orientation + ", rounded: " + roundedOrientattion);
                }
                int imageRotation = 0;
                if (roundedOrientattion == 0) {
                    imageRotation = 270;
                } else if (roundedOrientattion == 90) {
                    imageRotation = 180;
                } else if (roundedOrientattion == 180) {
                    imageRotation = 90;
                }

                Matrix matrix = new Matrix();
                matrix.postRotate(imageRotation);

                // ImageButton button = (ImageButton) findViewById(R.id.take_picture_button);
                // button.setCompoundDrawables(rotateDrawable(R.drawable.ic_menu_camera, matrix), null, null, null);
            }
        }
    }

    class AutoFocusCallback implements Camera.AutoFocusCallback {
        public void onAutoFocus(boolean success, Camera cam) {
            takePicture();
        }
    }

    class ShutterSound implements Camera.ShutterCallback {
        public void onShutter() {
            if (clickSound != null) {
                clickSound.seekTo(0);
                clickSound.start();
            }
        }
    }

    final class LoadShutterSoundThread extends Thread {
        @Override
        public void run() {

            try {
                clickSound = new MediaPlayer();
                AssetFileDescriptor afd = mSurfaceView.getContext().getResources().openRawResourceFd(R.raw.shortcut_sdk_camera_click);
                if (clickSound != null) {
                    clickSound.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                    clickSound.setAudioStreamType(STREAM_SYSTEM);
                    clickSound.prepare();
                }
            } catch (Exception e) {
                logWarn("Could not create click sound", e);
            }
        }
    }


}
