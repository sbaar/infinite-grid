package com.kennethmaffei.infinitegrid;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Descriptor class for a database image
 * Note that we do not include the image as a parcelable item. It's not necessary
 * 
 * @author Kenneth Maffei
 *
 */
public class RecordDescriptor implements Parcelable{
	Bitmap image;	//The image
	String url;		//The image source
	String description; //The description from the db
	String link; //The webview url that this record links to
	
	private void readFromParcel(Parcel in) {        
        url = in.readString();   
        description = in.readString();   
        link = in.readString();   
    }   
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel out, int flags) {   
        out.writeString(url);   
        out.writeString(description); 
        out.writeString(link);
	}
	
	public static final Parcelable.Creator<RecordDescriptor> CREATOR = new Parcelable.Creator<RecordDescriptor>() {   
	     
        public RecordDescriptor createFromParcel(Parcel in) {   
            RecordDescriptor record = new RecordDescriptor();  
            record.readFromParcel(in);
            return record;
        }   
    
        public RecordDescriptor[] newArray(int size) {   
            return new RecordDescriptor[size];   
        }   
           
    }; 
}
