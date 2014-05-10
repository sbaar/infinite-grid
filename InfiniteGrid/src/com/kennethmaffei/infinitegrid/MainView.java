/*******************************************************************************
 * Copyright 2014 Kenneth Maffei
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package com.kennethmaffei.infinitegrid;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

/**
 * A dynamically constructed view that contains infinite tiling of images
 * An instance of this will be our main view
 * 
 * @author Kenneth Maffei
 *
 */
public class MainView extends RelativeLayout {

    private int swipeMinDistance = 60; 					//Default value. Is set in setFlingVariables()
    private int swipeThresholdVelocity = 200;			//Default value. Is set in setFlingVariables()
    private float xAcceleration;
    private float yAcceleration;
    private float xVelocity;
    private float yVelocity;
    private long flingStartTime;
    private boolean flinging;
    private Context context;
    
    private Point currentPos = new Point(); //Where we currently have scrolled to
    private Point prevFlingPos = new Point(); //Where we last were
    
	public MainView(Context context) {
		super(context);
		
		this.context = context;
	}
	
	final GestureDetector gdt = new GestureDetector(context, new GestureListener());

	/**
	 * A Gesture Listener for all motions and swipes
	 * 
	 * @author Kenneth Maffei
	 *
	 */
    private class GestureListener extends SimpleOnGestureListener {
    	
    	@Override
    	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

    		currentPos.x+= distanceX;
    		currentPos.y+= distanceY;
    	    invalidate();
    	    return true;
    	}

    	
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        	
        	//The velocities will be too high, so we throttle them down
        	//velocityX/= 5.0f;
        	//velocityY/= 5.0f;
        	
        	//Horizontal
        	xVelocity = 0.0f;
        	yVelocity = 0.0f;
        	xAcceleration = 0.0f;
        	yAcceleration = 0.0f;
            if(Math.abs(e2.getX() - e1.getX()) > swipeMinDistance && Math.abs(velocityX) > swipeThresholdVelocity) {
            	xVelocity = velocityX;
            	xAcceleration = -(xVelocity/Constants.FLING_STOP_TIME);
            	flinging = true;
            }

            //Vertical
            if(Math.abs(e2.getY() - e1.getY()) > swipeMinDistance && Math.abs(velocityY) > swipeThresholdVelocity) {
            	yVelocity = velocityY;
            	yAcceleration = -(yVelocity/Constants.FLING_STOP_TIME);
                flinging = true;
            }
            
            //If we're flinging, then start the deceleration
            if(flinging) {
            	flingStartTime = System.currentTimeMillis();
            	prevFlingPos.x = prevFlingPos.y = 0;
            	invalidate();
            }
            return false;
        }
        
        @Override
        public boolean onSingleTapUp(MotionEvent event) {
        	Point press = new Point();
        	press.x = (int)event.getX();
        	press.y = (int)event.getY();
            MainActivity.tiles.selectTile(press);    //Get which tile was pressed so we can do whatever with it
            return true;
        }
        
        @Override
        public boolean onDown(MotionEvent event) {
        	flinging = false; //Stop any current flinging motion
        	return true;
        }
    }

    /**
     * Sets the absolute values for fling parameters
     * Scaled to device width so that the experience is the same across all devices
     * 
     * @param deviceWidth - the width of the device in pixels
     */
    public void setFlingVariables(int deviceWidth) {
    	swipeMinDistance = (int)(Constants.SWIPE_MIN_DISTANCE*deviceWidth);
    	swipeThresholdVelocity = (int)(Constants.SWIPE_THRESHOLD_VELOCITY*deviceWidth);
    }

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		//Only respond to single touch events
		if(event.getPointerCount() == 1)
			gdt.onTouchEvent(event);

		invalidate();  //Only redraw when necessary
		return true;
	}
	
	/**
	 * Calculates the current position based on simple acceleration physics
	 */
	private boolean calculateFlingPositions() {
		//distance = v*t + 0.5*a*t**2
		
		//First get the time increment
		float deltaTime = (float)(System.currentTimeMillis() - flingStartTime)/1000.0f;
		
		//If we've gone past the time, then stop the fling system
		if(deltaTime > Constants.FLING_STOP_TIME) {
			flinging = false;
			return false;
		}
		
		float x = -(xVelocity*deltaTime + 0.5f*xAcceleration*deltaTime*deltaTime);
		float y = -(yVelocity*deltaTime + 0.5f*yAcceleration*deltaTime*deltaTime);
		currentPos.x+= (int) (x - prevFlingPos.x);
		currentPos.y+= (int) (y - prevFlingPos.y);
		
		prevFlingPos.x = (int) x;
		prevFlingPos.y = (int) y;
		
		return true;
		
	}
	
	//Our rendering code
	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		Matrix matrix = getMatrix();
		MainActivity.tiles.draw(currentPos, canvas, matrix);
		
		if(flinging) {
			if(calculateFlingPositions())
				invalidate(); //Keep the rendering going!
		}
	}
}
