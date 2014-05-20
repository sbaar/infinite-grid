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

import java.util.HashMap;
import java.util.Locale;

import com.kennethmaffei.infinitegrid.Constants;
import com.kennethmaffei.infinitegrid.Constants.CALLTYPE;

import disklrucache.DiskLruOperations;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;

/**
 * Headless fragment class for creating AsynTasks that live across configuration changes
 * The onAttach() override ensures we always retain the current activity
 * 
 * @author Ken Maffei
 *
 */
public class TaskFragment extends Fragment {
	Activity activity; //The current activity, which could change on an activity restart
	RESTCallTask RESTCallTask; //The task for REST calls
	ImageCallTask ImageCallTask;
	
	/**
	 * 
	 * An implementation of AsyncTask for making a REST call
	 * 
	 * @author Kenneth Maffei
	 *
	 */
	private class RESTCallTask extends AsyncTask<CALLTYPE, Void, String> {
		
		private CALLTYPE callType;
		@Override
		protected String doInBackground(CALLTYPE... params) {
			callType = params[0];
			HTTPRequest httpRequest = new HTTPRequest();
			switch (callType) {
			case ALL_RECORDS: {
				HashMap<String, String> restGET = new HashMap<String, String>();
				restGET.put(Constants.PARSE_ID_KEY, Constants.PARSE_ID_VALUE);
				restGET.put(Constants.PARSE_REST_KEY, Constants.PARSE_REST_VALUE);
				restGET.put(Constants.PARSE_REST_CONTENT_KEY, Constants.PARSE_REST_CONTENT_VALUE);
				return httpRequest.RESTGet(restGET);
			}
			default:
				return null;
			}
		}
		
		@Override
	    protected void onCancelled() {

	    }
		
		@Override
	    protected void onPostExecute(String result) {
			switch (callType) {
				case ALL_RECORDS: {
					//The result is null if there is no internet connection
					if(result == null) {
						AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.context).create();
						alertDialog.setTitle("Error");
						alertDialog.setMessage("No Network Connection");
						alertDialog.setCancelable(false);
						alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
							public void onCancel(DialogInterface dialog) {
		
								return;		
							}
						});
						alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
			            	public void onClick(DialogInterface dialog, int which) {
					
			            		return;
			            	}
			            });
						alertDialog.show();
						return;
					}
					//Here's where we parse the json
					HTTPCommManager.INSTANCE.parseAllRecords(result);
				}
			}
		}
	}
	
	/**
	 * An implementation of AsyncTask that gets an image from a url
	 * 
	 * @author Kenneth Maffei
	 *
	 */
	private class ImageCallTask extends AsyncTask<RecordDescriptor, Void, RecordDescriptor> {

		@Override
		protected RecordDescriptor doInBackground(RecordDescriptor... params) {
			RecordDescriptor id = (RecordDescriptor)params[0];
			String key = id.url;
			key = key.toLowerCase(Locale.US);
			key = key.replaceAll("[^a-z0-9]","");
			Bitmap bitmap = DiskLruOperations.getBitmapFromDiskCache(key);
			if(bitmap != null) {
			    id.image = bitmap;
			    return id;
			}
			HTTPRequest httpRequest = new HTTPRequest();
			bitmap = httpRequest.getImage(id.url);
			id.image = bitmap;
			return id;
		}
		
		@Override
	    protected void onCancelled() {

	    }
		
		@Override
	    protected void onPostExecute(RecordDescriptor id) {
			//Write the bitmap to the cache
			String key = id.url;
			key = key.toLowerCase(Locale.US);
			key = key.replaceAll("[^a-z0-9]","");
			DiskLruOperations.addBitmapToCache(key, id.image);
			//Fill in our grid
			HTTPCommManager.INSTANCE.fillGrid(id);
		}
	 }
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//Retain this fragment across configuration changes.
		setRetainInstance(true);

		//Create and execute the background task.
	    
		int value = getArguments().getInt("type");
	    
		//We'll use the stock executor. It's good enough for this purpose
		//Call the correct AsyncTask depending on the "type"
		if(value == CALLTYPE.ALL_RECORDS.ordinal()) {
			RESTCallTask restCallTask = new RESTCallTask();
			restCallTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,  CALLTYPE.values()[value]);
		}
		else if(value == CALLTYPE.IMAGE.ordinal()) {
			ImageCallTask imageCallTask = new ImageCallTask();
			RecordDescriptor record = getArguments().getParcelable("record");
			imageCallTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,  record);
		}
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		HTTPCommManager.INSTANCE.setActivity(activity);
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
	}
}
