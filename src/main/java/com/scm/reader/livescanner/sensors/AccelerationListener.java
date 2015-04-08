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

package com.scm.reader.livescanner.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class AccelerationListener implements SensorEventListener{
	
	private boolean initialized = false; 
	private boolean moving = false;
	
	private SensorManager sensorManager; 
	private Sensor accelerometer; 
	
	private final float LIMIT = (float) 1.0; 
	
	private float currentX;
	private float currentY;
	private float currentZ;
	
	private float lastX;
	private float lastY;
	private float lastZ;
	
	private float deltaX;
	private float deltaY;
	private float deltaZ;
	
	public AccelerationListener(Context context) {
		sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}
	
	public void register(){
		sensorManager.registerListener(this, accelerometer,
				SensorManager.SENSOR_DELAY_NORMAL);
	}
	
	public void unregister(){
		sensorManager.unregisterListener(this);
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		currentX = event.values[0];
		currentY = event.values[1];
		currentZ = event.values[2];
		
		if(!initialized){
			lastX = currentX;
			lastY = currentY;
			lastZ = currentZ;
			
			initialized = true;
		}else {
			deltaX = Math.abs(lastX - currentX);
			deltaY = Math.abs(lastY - currentY);
			deltaZ = Math.abs(lastZ - currentZ);
			
			if(deltaX > LIMIT){
				//Log.d("QueryHandler", "deltaX: " +deltaX);
				moving = true;
			}else if(deltaY > LIMIT){
				//Log.d("QueryHandler", "deltaY: " +deltaZ);
				moving = true;
			}else if(deltaZ > LIMIT){
				//Log.d("QueryHandler", "deltaZ: " +deltaZ);
				moving = true;
			}else{
				moving = false;
			}
		
			lastX = currentX;
			lastY = currentY;
			lastZ = currentZ;
		}
	}
	
	public boolean isPhoneMoving(){
		return moving;
	}

}
