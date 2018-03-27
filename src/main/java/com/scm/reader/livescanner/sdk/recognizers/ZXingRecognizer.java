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

package com.scm.reader.livescanner.sdk.recognizers;

import java.util.Date;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.scm.reader.livescanner.sdk.camera.CameraManager;
import com.scm.reader.livescanner.sdk.zxing.PlanarYUVLuminanceSource;
import com.scm.reader.livescanner.search.Search;
import com.scm.reader.livescanner.util.LogUtils;

public class ZXingRecognizer  {
  private CameraManager cameraManager;
  MultiFormatReader multiFormatReader;
  
  public ZXingRecognizer(){

  }
  
  public ZXingRecognizer(CameraManager camMag, MultiFormatReader mfr) {  
    cameraManager = camMag;
    multiFormatReader = mfr;
  }
  
  public Search recognize(byte[] data){
    	Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
    	bmp = bmp.copy(Bitmap.Config.ARGB_8888, true);
    	
    	int[] intArray = new int[bmp.getWidth() * bmp.getHeight()];
    	bmp.getPixels(intArray, 0, bmp.getWidth(), 0, 0, bmp.getWidth(),
                bmp.getHeight());
    	
    	LuminanceSource source = new RGBLuminanceSource(bmp.getWidth(), bmp.getHeight(), intArray);
    	BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        Reader reader = new MultiFormatReader();
        Result result = null;
        LogUtils.logDebug("QR Decoder", "waiting");
        try {
			result = reader.decode(bitmap);
			if(result!=null){
				LogUtils.logDebug("QR Decoder", "result is not null");
				LogUtils.logDebug("QR Decoder", result.getText()+"");
			}else{
				LogUtils.logDebug("QR Decoder", "result is null");
			}
	
		} catch (NotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ChecksumException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return parseSearch(result, bmp);
  }
    
  
  public Search recognize(byte[] data, int width, int height, Bitmap barcodeBitmap) {
    PlanarYUVLuminanceSource source = cameraManager.buildLuminanceSource(data, width, height);
    Result result = null;
    if (source != null) {
      BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
      try {
        result = multiFormatReader.decode(bitmap);
      } catch (ReaderException re) {
        // continue
      } finally {
        multiFormatReader.reset();
      }
    }
    return parseSearch(result, barcodeBitmap);
  }
  
  private Search parseSearch(Result result, Bitmap barcodeBitmap){
	  //TODO remove log.d tags
	  Search search = new Search();
	  if (result != null){
		  LogUtils.logDebug("ZXingRecognizer", "zxingResponse != null");
		  search.setImage(search.createByteArray(barcodeBitmap));
		  search.setUrl(result.getText());
		  search.setTitle(result.getText());
		  search.setSearchTime(new Date());
		  search.setRecognized(true);
		  search.setIsQrcode(true);
	  }else {
		  LogUtils.logDebug("ZXingRecognizer", "zxingResponse == null");
	  }
	  return search;
  }
}
