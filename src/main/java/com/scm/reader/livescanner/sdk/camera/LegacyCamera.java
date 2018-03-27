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
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.view.Display;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.TextureView;
import android.view.WindowManager;

import com.scm.reader.livescanner.sdk.KConfig;
import com.scm.reader.livescanner.util.Size;
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

    private TextureView mTextureView;
    private int mSurfaceWidth;
    private int mSurfaceHeight;

    private Context mContext;

    private int lastOrientation = ORIENTATION_UNKNOWN;
    private KooabaCameraOrientationEventListener orientationListener;

    private Camera.PictureCallback mPictureCallback;
    private Camera.PictureCallback mLocalPictureCallback;
    private MediaPlayer clickSound;

    public LegacyCamera(TextureView textureView, Context context, Camera.PictureCallback callback) {
        mTextureView = textureView;
        mPictureCallback = callback;
        mSurfaceWidth = mTextureView.getWidth();
        mSurfaceHeight = mTextureView.getHeight();
        mContext = context;

        orientationListener = new KooabaCameraOrientationEventListener(mContext);
        mLocalPictureCallback = new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                isPicutreTakingInProgress = false;
                mPictureCallback.onPictureTaken(data, camera);
            }
        };
        new LoadShutterSoundThread().start();
    }

    public void startCamera() {
        if (isDebugLog()) {
            logDebug("startCamera() cameraStarted=" + isCameraStarted);
        }

        if (isCameraStarted) {
            stopCamera();
        }


        mCamera = Camera.open();
        orientationListener.enable();
        Camera.Parameters p = mCamera.getParameters();

        boolean isSensorRotated = configureRotation(mCamera);
        Size previewSize = configurePreviewSize(mSurfaceWidth, mSurfaceHeight, p);
        mTextureView.setTransform(
                calculateTextureTransform(
                        new Size(mSurfaceWidth, mSurfaceHeight),
                        previewSize, isSensorRotated)
        );

        configureFrameRate(p);
        configureFlashMode(p);
        configureFocusMode(p);
        configureJPEGQuality(p);
        configurePictureSize(p);

        mCamera.setParameters(p);
        setPreviewDisplay(mTextureView.getSurfaceTexture());

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
        mCamera.takePicture(
                new ShutterSound(),
                null,
                mLocalPictureCallback);
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

    protected Size configurePreviewSize(int surfaceWidth, int surfaceHeight, Camera.Parameters p) {
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
        return new Size(previewWidth, previewHeight);
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
        if (isSupported(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE, p.getSupportedFocusModes())) {
            logInfo("Device supports continuous focus, enabling it");
            p.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        } else {
            if (isSupported(Camera.Parameters.FOCUS_MODE_AUTO, p.getSupportedFocusModes())) {
                logInfo("Device supports auto focus, enabling it");
                p.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            }
        }
    }

    protected boolean configureRotation(Camera camera) {
        Display display = ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int rotation = calculateRotation(display);
        camera.setDisplayOrientation(rotation);
        return rotation % 180 != 0;
    }

    protected Matrix calculateTextureTransform(Size textureSize, Size previewSize, boolean isSensorRotated) {
        float ratioTexture = textureSize.width / textureSize.height;
        float ratioPreview = previewSize.width / previewSize.height;
        if (isSensorRotated) {
            ratioPreview = previewSize.height / previewSize.width;
        }

        float scaleX;
        float scaleY;

        // We scale so that either width or height fits exactly in the TextureView, and the other
        // is bigger (cropped).
        if (ratioTexture < ratioPreview) {
            scaleX = ratioPreview / ratioTexture;
            scaleY = 1;
        } else {
            scaleX = 1;
            scaleY = ratioTexture / ratioPreview;
        }

        Matrix matrix = new Matrix();

        matrix.setScale(scaleX, scaleY);

        // Center the preview
        float scaledWidth = textureSize.width * scaleX;
        float scaledHeight = textureSize.height * scaleY;
        float dx = (textureSize.width - scaledWidth) / 2;
        float dy = (textureSize.height - scaledHeight) / 2;

        // Perform the translation on the scaled preview
        matrix.postTranslate(dx, dy);

        return matrix;
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

        int finalRotation = (info.orientation - rotation + 360) % 360;
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


    private void setPreviewDisplay(SurfaceTexture holder) {
        try {
            mCamera.setPreviewTexture(holder);
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
                if (orientation == ORIENTATION_UNKNOWN) return;
                android.hardware.Camera.CameraInfo info =
                        new android.hardware.Camera.CameraInfo();
                android.hardware.Camera.getCameraInfo(0, info);
                orientation = (orientation + 45) / 90 * 90;
                int rotation = 0;
                if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    rotation = (info.orientation - orientation + 360) % 360;
                } else {  // back-facing camera
                    rotation = (info.orientation + orientation) % 360;
                }
                lastOrientation = rotation;
                Camera.Parameters p = mCamera.getParameters();
                p.setRotation(lastOrientation);
                mCamera.setParameters(p);
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
                AssetFileDescriptor afd = mContext.getResources().openRawResourceFd(R.raw.shortcut_sdk_camera_click);
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
