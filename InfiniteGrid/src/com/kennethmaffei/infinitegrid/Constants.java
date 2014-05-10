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

public class Constants {
	public static enum CALLTYPE {ALL_RECORDS, IMAGE}; //NOTE: We are using ordinal() to access values, so add elements accordingly!
	
	public static final String DISK_CACHE_SUBDIR = "artisanspalatecachedir";
	
	//HTTP Request strings
	public static final String RECORD_URL  = "https://api.parse.com/1/classes/DataAttributes";
	public static final String PARSE_ID_KEY = "X-Parse-Application-Id";
	public static final String PARSE_ID_VALUE = "HiqtQGlZ9ay6qkEb8Lwwb6VMujWBeJ9SlriVvpzi";
	public static final String PARSE_REST_KEY = "X-Parse-REST-API-Key";
	public static final String PARSE_REST_VALUE = "nobmnmJrnqd0ex3Hw8oFFsxqpSzQtJklzxhe0wx7";
	public static final String PARSE_REST_CONTENT_KEY = "Content-Type";
	public static final String PARSE_REST_CONTENT_VALUE = "application/json";
	
	//Network constants
	public static final int CONNECTION_TIMEOUT = 5000;
	public static final int REQUEST_TIMEOUT = 5000;
	
	//Fling constants
	public static final float SWIPE_MIN_DISTANCE = .025f;			//Normalized per pixel
    public static final float SWIPE_THRESHOLD_VELOCITY = .05f;		//Normalized per pixel
    public static final float FLING_STOP_TIME = 1.25f;
	
	//Tag for AsyncTasks
	public static final String TAG_TASK_GETALLRECORDS = "getallrecords";
	
	//JSON Node names
    public static final String TAG_RESULTS = "results";
    public static final String TAG_IMAGEATTRIBUTES = "image";
    public static final String TAG_IMAGENAME = "name";
    public static final String TAG_URL = "url";
    
    //Rendering
    public static final float BORDER_THICKNESS = .01f;
    public static final float TILE_ASPECT_RATIO = 1.333f;
    public static final int NUM_X_TILES = 6;
}
