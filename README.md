

# Overview

This SDK provides two components that allow to interact with the Shortcut Image Recognition Service:
- The first component consists of two classes, `CameraView` and `ScannerView`, which are using the camera to 
campture image data which then is submitted to the image recognition service. 
It reports image recognition results back to you. 
- The second compontent is a WebView (class `ItemView`) that displays a simple rendition of a (recognized) item. 

You can easily combine these two components by using the Camera/Scanner view to get an item and then pass it on to the Item view to display it. Or you can create a customized view to present a recognized item.

In addition to these two components the SDK also provides a lower-level interface to submit image recognition queries without using the scanner view.

The SDK works with Android API 8 to 21.

To see the SDK in action check out this [example app](https://github.com/shortcutmedia/shortcut-sdk-android-example).

There is also an [iOS version of the Shortcut SDK](https://github.com/shortcutmedia/shortcut-sdk-ios).


# Installation

See the instructions bellow how to integrate the SDK into your project. 

1. Checkout the latest source code of the SDK from [github](https://github.com/shortcutmedia/shortcut-sdk-android) to a temporary directory or download the latest version of the source code from the [release page](https://github.com/shortcutmedia/shortcut-sdk-android/releases).
2. In Android Studio open the project you would like the integrate the SDK or create a new empty project.
2. In Android Studio select menu 'File' > 'New', 'Import Module...' and select
   the path to the downloaded directory. 
3. Make the SDK classes available to the newly created project. Open 'File' >
   'Project Structure' and select module 'app'. Switch to tab
'Dependencies' and add _shortcutReaderSDK_ as a new "Module Dependency".
4. [Request the demo keys](http://shortcutmedia.com/request_demo_keys.html) and add the declaration to your project's _Manifest.xml_ file. We will immediately send you an email with the keys.

```xml
<manifest ... >
  <application ... >
    <meta-data android:name="com.shortcutmedia.shortcut.sdk.API_KEY" android:value="<DEMO_API_KEY>"/>
    <meta-data android:name="com.shortcutmedia.shortcut.sdk.API_SECRET" android:value="<DEMO_API_SECRET>"/>
  </application>
<manifest>
```
5. Exclude some files from being packaged. 

```gradle
android {
    ...
    packagingOptions {
        exclude 'META-INF/LICENSE.txt'
        exclude 'META-INF/NOTICE.txt'
    }
}
```

# Getting started

To get a feeling for the different parts of the SDK this section walks you through the process of building a very simple app that displays the camera view on start up. When an item is recognized, the app dismisses the camera view and displays the recognized item in an item view.

First, we have to create a new project in Android Studio. Select the most basic of the available templates (this would be a *Blank Activity* template ) and name it CameraActivity. You will not the generated layout file "activity_camera.xml" therefore you can safely delete it.  Then follow the steps in the Installation section above to add the SDK code to your project.

**You need access keys**. [Request the demo keys](http://shortcutmedia.com/request_demo_keys.html). We will immediately send you an email with the keys. These keys will allow you to scan the [Lenna test image](https://en.wikipedia.org/wiki/Lenna). If you plan to upload your own images, send an email to support@shortcutmedia.com to request your individual keys.

We want to display a Camera view as soon as the app starts; so let's implement the CameraActivity and make the following changes:

**Step 1:** Initialize `CameraView` in `CameraActivity` and make sure the necessary Lifecycle callbacks are called on `CameraView`:


```java
  public class CameraActivity extends Activity {

    private CameraView mCameraView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCameraView = new CameraView(this);
        mCameraView.onCreate(savedInstanceState);
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
```

**Step 2:** In order to receive the results of the recognition the Activity has to implement the interface `ShortcutSearchView.RecognitionCallbacks`:

```java
  public class CameraActivity extends Activity
        implements ShortcutSearchView.RecognitionCallbacks{

    ...

    public void onImageRecognized(KEvent event) {
        Search result = event.getSearch();
        Log.d("ScanActivity", "Image recognized. Result title = " + result.getTitle());
    }

    public void onImageNotRecognized(KEvent event) {
        Log.d("ScanActivity", "image not recognized");
    }

```

At this point you can launch the app and the camera view will open. If
you have used the demo API keys you can take a picture of 
[the standard test image 'Lenna'](http://en.wikipedia.org/wiki/Lenna)
which should result in a match. Check the app's log to see the response. 
 The `KEvent` object contains the metadata of the recognized item.

At this point you could persist the result to a data store and present
the response to the user. 


**Step 3:** If the image is recognized you can optionally pass the result to the ItemViewActivity provided by the SDK in order to display the result (see section ItemView for more details). Change the `onImageRecognized(KEvent)` handler method like following:  

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

Obviously `onImageNotRecognized(KEvent)` is called if the image could
not be recognized.


**Step 4:** In addition of the camera view for taking still pictures there 
is also a scanner view. Implementing an activity using the `ScannerView` class
is very similar to the CameraActivity. Create a new blank activity

`ScannerActivity` and make sure the necessary Lifecycle callbacks are called on `ScannerView`:

```java
  public class ScannerActivity extends Activity {

    private ScannerView mScannerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mScannerView = new ScannerView(this);
        mScannerView.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mScannerView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mScannerView.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mScannerView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mScannerView.onStop();
    }
}
```

**Step 5:** To receive the results of the recognition the Activity has to implement the interface `ShortcutSearchView.RecognitionCallbacks`:

```java
  public class ScannerActivity extends Activity
      implements ShortcutSearchView.RecognitionCallbacks {
    ...


    public void onImageRecognized(KEvent event) {
        Search result = event.getSearch();
        Log.d("ScanActivity", "Image recognized. Result title = " + result.getTitle());

        Intent i = new Intent(this, ItemViewActivity.class);
        i.setData(Uri.parse(result.getUrl()));
        startActivity(i);
    }

    public void onImageNotRecognized(KEvent event) {
        Log.d("ScanActivity", "image not recognized");
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
        mCameraView.onCreate(savedInstanceState);
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
        mScannerView.onCreate(savedInstanceState);
    }

    ...
```

# License
This project is released under the MIT license. See included LICENSE.txt file for details.

This project bundles parts of the zxing v2.0 library (https://github.com/zxing/zxing), which is available under an Apache-2.0 license. For details, see http://www.apache.org/licenses/LICENSE-2.0.
