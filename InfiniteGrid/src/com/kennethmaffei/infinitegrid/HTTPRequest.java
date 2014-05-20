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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * 
 * @author Kenneth Maffei
 * General Purpose class for several types of HTTP requests
 *
 */

public class HTTPRequest {
	
	public boolean isNetworkAvailable() {
	    ConnectivityManager cm  = (ConnectivityManager)MainActivity.context.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo[] info = cm.getAllNetworkInfo();
	    
	    //If no network is available networkInfo will be null
	    //Otherwise check if we are connected
	    if (info != null) {
	    	for (int i = 0; i<info.length; i++){
	    		if (info[i].getState() == NetworkInfo.State.CONNECTED)
	    			return true;
	    	}
	    }
	    return false;
	}

	/**
	 * Makes a REST GET call
	 * 
	 * @param properties - Associated array of header properties
	 * @return JSON String
	 */
	public String RESTGet(HashMap<String, String> properties) {
		/**
		* -H "X-Parse-Application-Id: kU0ija2xJOPy61EHr8uCCV5gyXkr7tpK4b8VCX5j" \
		* -H "X-Parse-REST-API-Key: LvvOSjEX4r0fvHWIJRzR1CcueHZLRWZOqUcRPxSW" \
		* -H "Content-Type: application/json" \		 
		**/
		
		String result = "";
			
		if(!isNetworkAvailable()) {
			return null;
		}
		
		InputStream is = null;
		HttpsURLConnection urc = null;
		try {
 			URL url = new URL(Constants.RECORD_URL);
 			urc = (HttpsURLConnection)url.openConnection();
 			for(Map.Entry<String, String> entry:properties.entrySet())
 				urc.setRequestProperty((String)entry.getKey(), (String)entry.getValue());
 			
 			urc.setConnectTimeout(Constants.CONNECTION_TIMEOUT);
 			urc.setReadTimeout(Constants.REQUEST_TIMEOUT);
 			urc.setRequestMethod("GET");
 	        urc.setDoInput(true);
 	        urc.connect();
 	        is = urc.getInputStream();

 	        BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8") );
 	        String data = null;
 	        while ((data = reader.readLine()) != null)
 	        	result+= data;

 			is.close();
 		}
 		catch (Exception e) {
 			try {
				if(is != null)
					is.close();
			}
			catch(IOException io) {
				
			}
 		} 
		finally {
			if(urc != null)
				urc.disconnect();
		}
		return result;
	}
	
	/**
	 * Gets the image from a url
	 * 
	 * @param urlString - the url where the image resides
	 * @return a Bitmap object for the image
	 */
	public Bitmap getImage(String urlString) {
		Bitmap image = null;
	    try {
	        image = BitmapFactory.decodeStream(new URL(urlString).openConnection().getInputStream());
	    } 
	    catch (Exception e) {
	        
	    }
	    return image;
	}
	
	/**
	 * Gets arbitrary data from a url
	 * 
	 * @param urlString - the url we are retrieving data from
	 */
	public byte[] getData(String urlString) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		InputStream is = null;
		URLConnection urc = null;
		try {
			URL url = new URL(urlString);
			urc = url.openConnection();
			urc.setConnectTimeout(Constants.CONNECTION_TIMEOUT);
			urc.setReadTimeout(Constants.REQUEST_TIMEOUT);
			is = urc.getInputStream();
			
			int size = 1024;           
			byte[] buf = new byte[size];
			
			int len = 0;
			while(((len = is.read(buf, 0, size)) > 0))                 
				baos.write(buf, 0, len);
			
			is.close();
		}
		catch (Exception e) {
 			try {
				if(is != null)
					is.close();
			}
			catch(IOException io) {
				
			}
 		} 

		//Return buffer
		return baos.toByteArray();
	}
}
