package com.scm.reader.livescanner.sdk.animation;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

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

		bottomBarHeight = activity.getResources().getDimensionPixelSize(R.dimen.bottom_toolbar);
		Log.d("Animation", bottomBarHeight +"");
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