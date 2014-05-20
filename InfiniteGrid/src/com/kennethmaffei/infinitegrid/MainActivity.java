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

import webview.WebViewActivity;

import com.appliedideas.infinitegrid.R;

import disklrucache.DiskLruOperations;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

/**
 * Our main activity. Sets the UI and initiates the HTTP requests
 * 
 * @author Kenneth Maffei
 *
 */
public class MainActivity extends Activity {
	
	//Tiles just holds data, so no need to re-populate it on an activity restart
	//Make it static so we can instantiate just once
	static Tiles tiles;
	MainView mainView;
	public static Context context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		context = this;
		
		requestWindowFeature(Window.FEATURE_NO_TITLE); 
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setRequestedOrientation(6);
        
		WindowManager wm = getWindowManager();
		DisplayMetrics dm = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(dm);
		
		DiskLruOperations.setCacheDir();
	    
		mainView = new MainView(this);
		mainView.setFlingVariables(dm.widthPixels);
		mainView.setWillNotDraw(false);
	    RelativeLayout.LayoutParams relativeParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
	    setContentView(mainView, relativeParams);
		
	    if(tiles == null) {
			tiles = new Tiles();
	        tiles.calculateTiles(dm.widthPixels, dm.heightPixels);
		}
		
		//We need to retrieve the thumbnails from the server
		HTTPCommManager.INSTANCE.setActivity(this);
		HTTPCommManager.INSTANCE.getDBData();
		
		mainView.invalidate();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	/**
	 * @param image - a retrieved image that is forwarded to the Tiles object
	 */
	public void fillGrid(RecordDescriptor id)
	{
		tiles.fillGrid(id);
		mainView.invalidate();
	}
	
	public void transitionToWebview(String url) {
		Intent intent = new Intent(MainActivity.context, WebViewActivity.class);
		intent.putExtra(Constants.WEBVIEW_INTENT_URL, url);
		startActivity(intent);
	}
}
