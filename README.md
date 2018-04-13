
[ ![Download](https://api.bintray.com/packages/shortcutmedia/shortcut-sdk/ShortcutSDK/images/download.svg) ](https://bintray.com/shortcutmedia/shortcut-sdk/ShortcutSDK/_latestVersion)

# Overview

This SDK provides two components that allow your app to interact with the Shortcut Image Recognition Service:
- The first component consists of two classes, `CameraView` and `ScannerView`, which are using the camera to 
capture image data which then is submitted to the image recognition service. 
It reports image recognition results back to you. 
- The second component is a WebView (class `ItemView`) that displays a simple rendition of a (recognized) item. 

You can easily combine these two components by using the Camera/Scanner view to get an item and then pass it on to the Item view to display it. Or you can create a customized view to present a recognized item.

In addition to these two components the SDK also provides a lower-level interface to submit image recognition queries without using the scanner view.

The SDK requires minimum API level 14 (Android 4.0)

To see the SDK in action check out this [example app](https://github.com/shortcutmedia/shortcut-sdk-android-example).

There is also an [iOS version of the Shortcut SDK](https://github.com/shortcutmedia/shortcut-sdk-ios).


# Installation

See the instructions bellow how to integrate the SDK into your project. 
1. Add jCenter repo to your project level build.gradle (it's the default repo for Android so it should be there already)
```gradle
allprojects {
    repositories {
        ...
        jcenter()
    }
}
```
2. Open the build.gradle file for your app or module and add the Shortuct SDK artifact.
```gradle
implementation 'com.shortcutmedia.shortcut.sdk:shortcut-sdk-android:1.1.1'
```
3. Add Shortcut API keys to your Android Manifest. The ones you see bellow are for testing purposes. With these test keys, you can scan [the standard test image 'Lenna'](http://en.wikipedia.org/wiki/Lenna)
```xml
<manifest
    ...
    <application
    ...
         <meta-data
            android:name="com.shortcutmedia.shortcut.sdk.API_KEY"
            android:value="40552bf6b886ab0a89a50712b256bb423dd9e180" />
         <meta-data
            android:name="com.shortcutmedia.shortcut.sdk.API_SECRET"
            android:value="13679446ee03264934bf97e2b29b9dfc74428ab9" />
```

# Getting started

**NOTE: Runtime permission checks are not shown in the following code for brevity, but they are required. Check out [example app](https://github.com/shortcutmedia/shortcut-sdk-android-example) to see the complete code.**

To get a feeling for the different parts of the SDK this section walks you through the process of building a very simple app that displays the camera view on start up. When an item is recognized, the app dismisses the camera view and displays the recognized item in an item view.

First, we have to create a new project in Android Studio. Select the most basic of the available templates (this would be a *Blank Activity* template). We'll name our activity `CameraActivity`. You don't need the layouts for the simplest setup. Follow the steps in the Installation section above to add the SDK code to your project.

We want to display a Camera view as soon as the app starts:

**Step 1:** Initialize `CameraView` in `CameraActivity`:

```java
public class CameraActivity extends AppCompatActivity {

    private CameraView mCameraView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCameraView = new CameraView(this);
    }
}
```
**Step 2:** Next we need to let the CameraView know about the lifecycle of our Activity. You have two options:
- CameraView is [Lifecycle-Aware component](https://developer.android.com/topic/libraries/architecture/lifecycle.html), so you can just register it as an observer of lifecycle events. Note that our Activity is extending AppCompatActivity:
```java
public class CameraActivity extends AppCompatActivity {

    private CameraView mCameraView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCameraView = new CameraView(this);
        getLifecycle().addObserver(mCameraView);
    }
}
```
- or you can do it the old way by calling lifecycle methods on CameraView manually:
```java
public class CameraActivity extends AppCompatActivity {

    private CameraView mCameraView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCameraView = new CameraView(this);
        mCameraView.onCreate();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mCameraView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCameraView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCameraView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mCameraView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCameraView.onDestroy();
    }
}
```


**Step 3:** In order to receive the results of the recognition you need to implement `ShortcutSearchView.RecognitionCallbacks`, and register it with `CameraView` In this example we'll let the Activity implement the interface: 

```java
public class CameraActivity extends AppCompatActivity
        implements ShortcutSearchView.RecognitionCallbacks{

    private CameraView mCameraView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCameraView = new CameraView(this);
        mCameraView.setRecognitionCallbacks(this);
        mCameraView.onCreate();
    }

    
    @Override
    public void onImageRecognized(KEvent event) {
        Search result = event.getSearch();
        Log.d("ScanActivity", "Image recognized. Result title = " + result.getTitle());
    }

    @Override
    public void onImageNotRecognized(KEvent event) {
        Log.d("ScanActivity", "image not recognized");
    }
}
```

At this point you can launch the app and the camera view will open. If
you have used the demo API keys you can take a picture of 
[the standard test image 'Lenna'](http://en.wikipedia.org/wiki/Lenna)
which should result in a match. Check the app's log to see the response. 
 The `KEvent` object contains the metadata of the recognized item.

**Step 4:** If the image is recognized you can optionally pass the result to the ItemViewActivity provided by the SDK in order to display the result (see section ItemView for more details). Change the `onImageRecognized(KEvent)` handler method like following:  

```java
    public void onImageRecognized(KEvent event) {
        Search result = event.getSearch();
        Log.d("ScanActivity", "Image recognized. Result title = " +
result.getTitle());

        Intent i = new Intent(this, ItemViewActivity.class);
        i.setData(Uri.parse(result.getUrl()));
        startActivity(i);
    }
```

`onImageNotRecognized(KEvent)` is called if the image could
not be recognized.


**Step 5:** In addition of the camera view for taking still pictures there 
is also a scanner view. 'ScannerView' continuously scans the image. Implementing an activity using the `ScannerView` is very similar to the `CameraView`.

Create a new blank activity

```java
public class ScannerActivity extends AppCompatActivity
        implements ShortcutSearchView.RecognitionCallbacks {

    private ScannerView mScannerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mScannerView = new ScannerView(this);
        mScannerView.setRecognitionCallbacks(this);
        getLifecycle().addObserver(mScannerView);
    }
    
    @Override
    public void onImageRecognized(KEvent event) {
        Search result = event.getSearch();
        Log.d("ScannerActivity", "Image recognized. Result title = " +
                result.getTitle());
    }
    
    @Override
    public void onImageNotRecognized(KEvent event) {
        Toast.makeText(this, "Not Recognized", Toast.LENGTH_SHORT).show();
    }
}
```
**Step 6:** You can add the ability to switch between the scanner and camera view by implementing the `ShortcutSearchView.ChangeCameraModeCallback`:

```java
public class CameraActivity extends Activity
        implements ShortcutSearchView.RecognitionCallbacks {

    ...

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCameraView = new CameraView(this);
        mCameraView.setChangeCameraModeCallback(new ShortcutSearchView.ChangeCameraModeCallback() {
            @Override
            public void onChangeCameraMode() {
                Intent i = new Intent(CameraActivity.this, ScannerActivity.class);
                startActivity(i);
                finish();
            }
        });
        getLifecycle().addObserver(mCameraView);
    }

    ...
```

```java
public class ScannerActivity extends Activity
    implements ShortcutSearchView.RecognitionCallbacks {

    ...

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mScannerView = new ScannerView(this);
        mScannerView.setChangeCameraModeCallback(new ShortcutSearchView.ChangeCameraModeCallback() {
            @Override
            public void onChangeCameraMode() {
                Intent i = new Intent(ScannerActivity.this, CameraActivity.class);
                startActivity(i);
                finish();
            }
        });
        getLifecycle().addObserver(mCameraView);
    }

    ...
```

# License
This project is released under the MIT license. See included LICENSE.txt file for details.

This project bundles parts of the zxing v2.0 library (https://github.com/zxing/zxing), which is available under an Apache-2.0 license. For details, see http://www.apache.org/licenses/LICENSE-2.0.
