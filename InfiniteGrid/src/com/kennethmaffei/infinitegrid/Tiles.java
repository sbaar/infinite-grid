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

import java.io.IOException;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;

/**
 * A 2D array of Tile objects
 * A stock thumbnail is displayed if no images have been loaded
 * In the current implementation, images are not assigned to any specific tile location
 * They are loaded as they arrive
 * 
 * @author Kenneth Maffei
 *
 */
public class Tiles {
	
	static final int numXTiles = Constants.NUM_X_TILES; 	//Number of tiles along the horizontal direction
	private int numYTiles; 									//Number of tiles along the vertical direction (this is calculated from the device aspect ratio)
	public static Point tileSize = new Point(); 			//Width and height of a tile
	
	private Paint paint = new Paint();
	int prevXStart;
	int prevYStart;
	private Point origin = new Point();
	
	Bitmap stockThumbnail;
	
	public static int borderThickness;
	public static final Rect borderVertical = new Rect();
	public static final Rect borderHorizontal = new Rect();
	
	private LinkedList<LinkedList<Tile>> tiles = new LinkedList<LinkedList<Tile>>();	//Array of queues
	private ArrayList<Tile> allTiles = new ArrayList<Tile>();	//Helper array that makes indexing and index calculations easier
	private ArrayList<Integer> tileXStartIndex = new ArrayList<Integer>();	//Array of all tile objects
	
	void calculateTiles(int deviceWidth, int deviceHeight) {
		
		tileSize.x = (int)((float)deviceWidth/(float)(numXTiles - 2)); 				//Need two extra at the end since we have to render while scrolling (extra one before and after)
		tileSize.y = (int)((float)tileSize.x/(Constants.TILE_ASPECT_RATIO));
		numYTiles = (int)Math.ceil(((float)deviceHeight/(float)tileSize.y)) + 2; 	//Use the ceiling so we make sure we span the full height
		
		//If borderThickness is an odd number, then push it up a pixel so we don't have to 
		//deal with rounding issues when dividing by 2 during tile rendering
		borderThickness = (int)(Constants.BORDER_THICKNESS*deviceWidth);
		if(borderThickness % 2 != 0)
			borderThickness++;
		
		stockThumbnail = LoadFromFile("aplogo.jpg");
		
		//Create an array of tiles for each y position
		//Add that array to our tiles array
		Tile t = new Tile();
		ImageDescriptor id = new ImageDescriptor();
		id.image = stockThumbnail;
		id.url = "stock";
		t.setImageDescriptor(id);
		allTiles.add(t);
		for(int i=0; i<numYTiles; i++) {
			LinkedList<Tile> xTiles = new LinkedList<Tile>();
			for(int j=0; j<numXTiles; j++) 
				xTiles.add(t);

			tiles.add(xTiles);
			tileXStartIndex.add(0);
		}
		
		//Create the border rects
		borderVertical.top = 0;
		borderVertical.bottom = tileSize.y;
		borderVertical.left = 0;
		borderVertical.right = borderThickness/2; //Scale this with the device size
		
		borderHorizontal.top = 0;
		borderHorizontal.bottom = borderThickness/2;
		borderHorizontal.left = 0;
		borderHorizontal.right = tileSize.x; //Scale this with the device size
	}
	
	//Load a Bitmap from disk
	/**
	 * Loads an image from disk and scales to our tile size
	 * 
	 * @param fileName - the name for our stock thumbnail
	 */
	public Bitmap LoadFromFile(String fileName) {
		InputStream is;
		try{
			is = MainActivity.context.getAssets().open(fileName); 
			int size = is.available(); 
			byte[] buffer = new byte[size]; 
			is.read(buffer, 0, size);
			is.close(); 
			
			//Check if png or jpg
			BitmapFactory.Options opt = new BitmapFactory.Options();
			
			Bitmap bitmap;

			//We'll get the size of our image, then scale to our tile size
			//This saves a little extra memory as we don't scale during drawing
			opt.inJustDecodeBounds = true;
			BitmapFactory.decodeByteArray(buffer, 0, size, opt);
			opt.inJustDecodeBounds = false;
			Bitmap tmp = BitmapFactory.decodeByteArray(buffer, 0, size, opt);
			
			try {
				bitmap = Bitmap.createScaledBitmap(tmp, tileSize.x - borderThickness, tileSize.y - borderThickness, true);
			}
			catch(OutOfMemoryError e){
				tmp.recycle();
				buffer = null;
				return null;
			}

			buffer = null;
			return bitmap;
		}
		catch(IOException IOerror)
		{
			return null;
		}
	}
	
	public void clearGrid() {
		
	}
	
	/**
	 * Creates a new tile for the new image
	 * Re-tiles our array based on all previously loaded images
	 * This is called on the user thread from our AsynTask, so is safe
	 * 
	 * @param image - the new image
	 */
	public void fillGrid(ImageDescriptor id) {
		
		if(id.image == null)
			return;
		
		//If we have this one already, don't add it again
		for(Tile t: allTiles) {
			if(t.getUrl().equals(id.url))
				return;
		}
		
		Bitmap bitmap = null;
		
		try {
			bitmap = Bitmap.createScaledBitmap(id.image, tileSize.x - borderThickness, tileSize.y - borderThickness, true);
			id.image = bitmap;
		}
		catch(OutOfMemoryError e){
			id.image.recycle();
			return;
		}
		
		//Check if default tiles are loaded
		//If so, then clear out our helper arrays
		if(allTiles.get(0).getThumbnail() == stockThumbnail) {
			allTiles.clear();
			tileXStartIndex.clear();
		}
		
		//Create a new tile
		Tile tile = new Tile();
		tile.setImageDescriptor(id);
		allTiles.add(tile);
		
		int size = allTiles.size();
		int count = 0;
		int idx;
		tileXStartIndex.clear();
		for(int i=0; i<numYTiles; i++) {
			LinkedList<Tile> xTiles = tiles.get(i);
			for(int j=0; j<numXTiles; j++) {
				xTiles.removeFirst();
				idx = count % size;
				xTiles.add(allTiles.get(idx));
				
				//Set the tileXStartIndex
				if(j == 0)
					tileXStartIndex.add(idx);
				count++;
			}
		}
	}
	
	/**
	 * Returns the tile where the user pressed
	 * 
	 * @param press - the point where the user pressed on the screen
	 */
	public void selectTile(Point press) {
		//Subtract out the origin
		press.x-= origin.x;
		press.y-= origin.y;
		
		//Make adjustment due to how we render the tiles
		if(press.x < 0)
			press.x-= tileSize.x;
		if(press.y < 0)
			press.y-= tileSize.y;
		
		//We display the tiles biased, so we add 1
		press.x = press.x/tileSize.x + 1; 
		press.y = press.y/tileSize.y + 1;
		Tile t = tiles.get(press.y).get(press.x);
		
		//If no connection, then only stock images will show
		//If we have stock images, then pressing on one will attempt to access the db again
		//Note: we have safeties in place in the comm manager to prevent spamming the server.
		if(t.getThumbnail() == stockThumbnail)
			HTTPCommManager.INSTANCE.getDBData();
	}
	

	/**
	 * Iterates through the 2D array and draws the tiles
	 * Shifts/rotates indices as necessary 
	 * 
	 * @param p - the scrolled point for the container view
	 * @param canvas - the container view's canvas
	 * @param matrix - the container view's matrix
	 */
	public void draw(Point p, Canvas canvas, Matrix matrix) {
		//We need to determine which tiles we are starting with
		int xTileStart = -p.x/tileSize.x; //As x shifts negative, we advance in our tiles array
		int yTileStart = -p.y/tileSize.y; //As y shifts negative, we advance in our tiles array
		
		//Check the conditions for shifting tiles around
		if(xTileStart > prevXStart) {
			for(int j=0; j<(Math.abs(xTileStart - prevXStart)); j++) {
				for(int i=0; i<tiles.size(); i++) {
					LinkedList<Tile> llt = tiles.get(i);
					llt.removeLast();
					
					//Set the new first index
					int currentStartIdx = tileXStartIndex.get(i);
					int newStartIdx = (currentStartIdx - 1);
					if(newStartIdx < 0)
						newStartIdx = allTiles.size() - 1;
					tileXStartIndex.set(i, newStartIdx);
					
					//Add the appropriate new tile
					llt.add(0, allTiles.get(newStartIdx));
				}
			}
		}
		else if(xTileStart < prevXStart) {
			for(int j=0; j<(Math.abs(xTileStart - prevXStart)); j++) {
				for(int i=0; i<tiles.size(); i++) {
					LinkedList<Tile> llt = tiles.get(i);
					llt.removeFirst();
					
					//Set the new first index
					int currentStartIdx = tileXStartIndex.get(i);
					int newStartIdx = (currentStartIdx + 1) % allTiles.size();
					tileXStartIndex.set(i, newStartIdx);
					
					//Add the appropriate new tile
					int lastIdx = (newStartIdx + numXTiles - 1) % allTiles.size();
					llt.add(allTiles.get(lastIdx));
				}
			}
		}
		
		if(yTileStart > prevYStart) { //Scrolling down
			for(int j=0; j<(Math.abs(yTileStart - prevYStart)); j++) {
				LinkedList<Tile> yTilesFirstRow = tiles.removeLast();
				
				//Get the last idx for this row, then back up by numXTiles
				int startIdx = (tileXStartIndex.get(0));
				for(int i=0; i<numXTiles; i++) {
					startIdx--;
					if(startIdx < 0)
						startIdx = allTiles.size() - 1;
				}
	
				for(int i=0; i<numXTiles; i++) {
					int idx = (startIdx + i) % allTiles.size();
					yTilesFirstRow.set(i, allTiles.get(idx));
				}
				
				//Rotate the list
				tileXStartIndex.remove(tileXStartIndex.size() - 1);
				tileXStartIndex.add(0, startIdx);
				
				tiles.add(0, yTilesFirstRow);
			}
		}
		else if(yTileStart < prevYStart) { //Scrolling up
			for(int j=0; j<(Math.abs(yTileStart - prevYStart)); j++) {
				LinkedList<Tile> yTilesLastRow = tiles.removeFirst();
				
				//Get the last idx for this row
				int lastIdxForLastRow = (tileXStartIndex.get(numYTiles - 1) + numXTiles - 1) % allTiles.size();
				int startIdx = (lastIdxForLastRow + 1) % allTiles.size();
				for(int i=0; i<numXTiles; i++) {
					int idx = (startIdx + i) % allTiles.size();
					yTilesLastRow.set(i, allTiles.get(idx));
				}
				
				//Rotate the list
				tileXStartIndex.remove(0);
				tileXStartIndex.add(startIdx);
				
				tiles.add(yTilesLastRow);
			}
		}
		
		prevXStart = xTileStart;
		prevYStart = yTileStart;
		
		//The origin runs from 0 to the tileSize
		origin.x = -p.x % (tileSize.x);
		origin.y = -p.y % (tileSize.y);
		
		int i = 0;
		int j = 0;
		Iterator<LinkedList<Tile>> iY = tiles.iterator();
		while (iY.hasNext()) {
			LinkedList<Tile> ad = iY.next();
			Iterator<Tile> iX = ad.iterator();
			i = 0;
			while (iX.hasNext()) {
				Tile t = iX.next();
				
				Point drawPoint = new Point();
				drawPoint.x = origin.x + (i - 1)*tileSize.x; //Add a bias to the draw position so we have one to the left
				drawPoint.y = origin.y + (j - 1)*tileSize.y; //Add a bias to the draw position so we have one to the top
				t.draw(drawPoint, canvas, paint, matrix);
				i++;
			}
			j++;
		}
	}
}
