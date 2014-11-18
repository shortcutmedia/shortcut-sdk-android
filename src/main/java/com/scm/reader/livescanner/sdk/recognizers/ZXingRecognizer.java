package com.scm.reader.livescanner.sdk.recognizers;

/*
Copyright (c) 2012, kooaba AG
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

  * Redistributions of source code must retain the above copyright notice, this
    list of conditions and the following disclaimer.
  * Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.
  * Neither the name of the kooaba AG nor the names of its contributors may be
    used to endorse or promote products derived from this software without
    specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

import java.util.Date;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
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
        Log.d("QR Decoder", "waiting");
        try {
			result = reader.decode(bitmap);
			if(result!=null){
				Log.d("QR Decoder", "result is not null");
				Log.d("QR Decoder", result.getText()+"");
			}else{
				Log.d("QR Decoder", "result is null");
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
		  Log.d("ZXingRecognizer", "zxingResponse != null");
		  search.setImage(search.createByteArray(barcodeBitmap));
		  search.setUrl(result.getText());
		  search.setTitle(result.getText());
		  search.setSearchTime(new Date());
		  search.setRecognized(true);
		  search.setIsQrcode(true);
	  }else {
		  Log.d("ZXingRecognizer", "zxingResponse == null");
	  }
	  return search;
  }
}
