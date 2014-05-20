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

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;

/**
 * An tile element that is added to our view
 * It can be rendered in any arbitrary fashion and includes borders
 * 
 * @author Kenneth Maffei
 *
 */
public class Tile {
	
	private RecordDescriptor recordDescriptor;

	Bitmap getThumbnail() {
		return recordDescriptor.image;
	}
	
	void setRecordDescriptor(RecordDescriptor record) {
		this.recordDescriptor = record;
	}
	
	String getUrl() {
		return recordDescriptor.url;
	}
	
	RecordDescriptor getRecordDescriptor() {
		return recordDescriptor;
	}
	
	/**
	 * Draw code for the tile
	 * 
	 * @param origin - the x, y coordinates for drawing
	 * @param canvas - the container view's canvas
	 * @param paint - the container view's paint
	 * @param matrix - the container view's matrix
	 */
	void draw(Point origin, Canvas canvas, Paint paint, Matrix matrix) {
		
		//Draw the image
		if(recordDescriptor.image != null)
			canvas.drawBitmap(recordDescriptor.image, origin.x + Tiles.borderThickness/2, origin.y + Tiles.borderThickness/2, paint);
		
		//Draw borders
		Matrix defaultMatrix = matrix;
		Matrix workingMatrix = new Matrix(matrix);
		
		workingMatrix.postTranslate(origin.x, origin.y);
		canvas.setMatrix(workingMatrix);
		canvas.drawRect(Tiles.borderVertical, paint);
		canvas.drawRect(Tiles.borderHorizontal, paint);
		
		workingMatrix.set(defaultMatrix);
		workingMatrix.postTranslate(origin.x + Tiles.tileSize.x - Tiles.borderThickness/2, origin.y);
		canvas.setMatrix(workingMatrix);
		canvas.drawRect(Tiles.borderVertical, paint);
		
		workingMatrix.set(defaultMatrix);
		workingMatrix.postTranslate(origin.x, origin.y + Tiles.tileSize.y - Tiles.borderThickness/2);
		canvas.setMatrix(workingMatrix);
		canvas.drawRect(Tiles.borderHorizontal, paint);
		
		canvas.setMatrix(defaultMatrix);
	}
}
