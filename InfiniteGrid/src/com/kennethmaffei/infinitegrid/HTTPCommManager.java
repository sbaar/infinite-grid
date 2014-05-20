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

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.kennethmaffei.infinitegrid.Constants.CALLTYPE;

import android.app.Activity;
import android.app.FragmentManager;
import android.os.Bundle;

/**
 * Manages HTTP communications, marshaling data to/from fragments and the UI
 * We only want one instance of the comm manager and Singletons are controversial
 * So we use the more readily acceptable enum approach
 * 
 * @author Kenneth Maffei
 *
 */
public enum HTTPCommManager {
	
	INSTANCE;
	
	//This is the app's main activity.
	//It is set on activity restarts and in the Fragments attach callback
	//So it is always current.
	Activity activity = null;
	
	//If a database retrieval is in process, we need to prevent another retrieval from occurring
	private Object synchronizeObject = new Object();
	private boolean retrievalDone = true; //No getter or setters for this! It's set internally only
	
	int numImageThreads = 0;
	int imageCount = 0;
	
	public void setActivity(Activity activity) {
    	this.activity = activity;
    }
	
	/**
	 * Sets up a headless fragment that calls an AsyncTask to retrieve records in the db
	 * 
	 * @param activity - the activity, needed to access the fragment manager
	 */
	public void getDBData() {
		if(activity == null)
			return;
		
		synchronized(synchronizeObject) {
			if(!retrievalDone)
				return;
		
			retrievalDone = false;
			imageCount = 0;
			FragmentManager fm = activity.getFragmentManager();
			TaskFragment taskFragment = (TaskFragment) fm.findFragmentByTag(Constants.TAG_TASK_GETALLRECORDS);
	
			//If the Fragment is non-null, then it is currently being retained across a configuration change.			
			if (taskFragment == null) {
				taskFragment = new TaskFragment();
				Bundle args = new Bundle();
				args.putInt("type", CALLTYPE.ALL_RECORDS.ordinal());
				taskFragment.setArguments(args);
				fm.beginTransaction().add(taskFragment, Constants.TAG_TASK_GETALLRECORDS).commit();
			}
		}
	}
	
	/**
	 * Sets up a headless fragment that calls an asynctask to retrieve an image from a url
	 * 
	 * @param url - the url which we will get data back from
	 */
	public void getURL(RecordDescriptor record) {
		if(activity == null)
			return;
		
		FragmentManager fm = activity.getFragmentManager();
		//Use the url as the fragment tag
		TaskFragment taskFragment = (TaskFragment) fm.findFragmentByTag(record.url);

		//If the Fragment is non-null, then it is currently being retained across a configuration change.
		if (taskFragment == null) {
			taskFragment = new TaskFragment();
			Bundle args = new Bundle();
			args.putInt("type", CALLTYPE.IMAGE.ordinal());
			args.putParcelable("record", record);
			taskFragment.setArguments(args);
			fm.beginTransaction().add(taskFragment, record.url).commit();
		}
	}

	/**
	 * The fragment calls this when done with the ALL_RECORDS request to parse the JSON string
	 * 
	 * @param jsonData - the JSON string returned from the cloud
	 */
	public void parseAllRecords(String jsonData) {
		if(jsonData.length() == 0) {
			retrievalDone = true;
			return;
		}
		
		ArrayList<RecordDescriptor> records = new ArrayList<RecordDescriptor>();
		
		//Parse the JSON, which is simple in our case
		try {
			JSONObject jsonObj = new JSONObject(jsonData);
			JSONArray entries = jsonObj.getJSONArray(Constants.TAG_RESULTS);
			for (int i=0; i<entries.length(); i++) {
				RecordDescriptor record = new RecordDescriptor();
				JSONObject entry = entries.getJSONObject(i);
				JSONObject imageAttributes = entry.getJSONObject(Constants.TAG_IMAGEATTRIBUTES);
				String url = imageAttributes.getString(Constants.TAG_URL);
				String description = entry.getString(Constants.TAG_DESCRIPTION);
				String link = entry.getString(Constants.TAG_LINK);
				
				record.url = url;
				record.description = description;
				record.link = link;
				records.add(record);
			}

		}
		catch(JSONException e) {
			retrievalDone = true;
			return; //Any exception means we should bail
		}
		
		//We've got our data, so now spawn all the threads
		numImageThreads = records.size();
		for(RecordDescriptor record : records) {
			getURL(record);
		}
	}
	
	//The fragment calls this when done with the RECORD request
	/**
	 * Sends an image to the tiled view
	 * 
	 * @param image - the bitmap to send
	 */
	public void fillGrid(RecordDescriptor record) {
		if(record.image != null)
			((MainActivity)activity).fillGrid(record);
		imageCount++;
		if(imageCount == numImageThreads)
			retrievalDone = true;
	}
}
