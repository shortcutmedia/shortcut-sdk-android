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

package com.scm.reader.livescanner.sdk.animation;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

import com.scm.reader.livescanner.util.LogUtils;
import com.scm.reader.livescanner.util.Utils;
import com.scm.shortcutreadersdk.R;

public class ScannerAnimation {
	
	private Activity activity;
	private View animationView;
	
	private int screenWidth;
	private int screenHeight;
	private int bottomBarHeight;
	
	private TranslateAnimation ta;

	public ScannerAnimation(Activity act, View animationView) {
		this.activity = act;
		this.animationView = animationView;
		
		WindowManager manager = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
		/**
		 * ScanActivity is landscape oriented,
		 * so screenHeight equals layoutWidth
		 */
	    screenWidth = Utils.getScreenResolution(manager).x;
	    screenHeight = Utils.getScreenResolution(manager).y;

		bottomBarHeight = activity.getResources().getDimensionPixelSize(R.dimen.shortcut_sdk_bottom_toolbar);
		LogUtils.logDebug("Animation", bottomBarHeight +"");
		ta = new TranslateAnimation(-screenWidth,  0, -screenHeight, -bottomBarHeight);
		ta.initialize(screenWidth*2, screenHeight*2, screenWidth*2, screenHeight*2);
	}
	
	public void start(){
		animationView.setVisibility(View.VISIBLE);

		ta.setDuration(1500);
		ta.setRepeatCount(Animation.INFINITE);
		ta.setRepeatMode(Animation.REVERSE);
		
		animationView.startAnimation(ta);
	}
	
	public void stop(){
		animationView.setVisibility(View.GONE);
		animationView.clearAnimation();
	}
	
	public boolean isAnimationVisible(){
		return (View.VISIBLE == animationView.getVisibility()) ? true : false;
	}
}