package DiskLruCache;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedOutputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import com.kennethmaffei.infinitegrid.Constants;
import com.kennethmaffei.infinitegrid.MainActivity;

/**
 * Manages HTTP communications, marshaling data to/from fragments and the UI
 * We only want one instance of the comm manager and Singletons are controversial
 * So we'll use the enum approach
 * 
 * @author Kenneth Maffei
 *
 */
public class DiskLruOperations {
	
	public static DiskLruCache mDiskLruCache = null;
	public static final Object mDiskCacheLock = new Object();
	public static File cacheDir = null;
	
	public static void setCacheDir() {
		synchronized (mDiskCacheLock) {
			if(cacheDir == null) {
				cacheDir = getDiskCacheDir(MainActivity.context, Constants.DISK_CACHE_SUBDIR);
				if(mDiskLruCache == null) {
					try {
						mDiskLruCache = DiskLruCache.open(cacheDir, 100, 1, Constants.DISK_CACHE_SIZE);
						mDiskCacheLock.notifyAll(); // Wake any waiting threads
					}
					catch(Exception e) {
						
					}
				}
			}
		}
	}
	
	/**
	 * Add an image to our DiskLruCache
	 * 
	 * @param key - a string value used by the DiskLruCache HashMap
	 * @param bitmap - the image to add tot the DiskLruCache
	 */
	public static void addBitmapToCache(String key, Bitmap bitmap) {
		//Add to disk cache
		synchronized(mDiskCacheLock) {
			try {
				if(mDiskLruCache != null && mDiskLruCache.get(key) == null) {
					DiskLruCache.Editor editor = null;
					try {
						editor = mDiskLruCache.edit(key);
						if(editor == null )
							return;
						
						OutputStream out = null;
						try {
							out = new BufferedOutputStream(editor.newOutputStream(0), Constants.IO_BUFFER_SIZE);
							if(bitmap.compress(Constants.COMPRESS_FORMAT, Constants.COMPRESS_QUALITY, out))
								editor.commit();
							else
								editor.abort();
						} 
						catch(Exception e) {
				        	
						}
						finally {
							if (out != null) {
								out.close();
							}
						}
					} 
					catch(IOException ioe) {
						try {
							if(editor != null)
								editor.abort();
						} 
						catch (IOException ignored) {
		                	
						}           
					}
				}
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Retrieve an image from our DiskLruCache
	 * 
	 * @param key - a string value used by the DiskLruCache HashMap
	 * @return
	 */
	public static Bitmap getBitmapFromDiskCache(String key) {
		synchronized(mDiskCacheLock) {
			Bitmap bitmap = null;
			DiskLruCache.Snapshot snapshot = null;
			try {
				snapshot = mDiskLruCache.get(key);
				if(snapshot == null)
					return null;

				final InputStream in = snapshot.getInputStream(0);
				if(in != null) {
					final BufferedInputStream buffIn = new BufferedInputStream(in);
					bitmap = BitmapFactory.decodeStream( buffIn );              
				}   
			} 
			catch(IOException e) {
				e.printStackTrace();
			} 
			finally {
				if(snapshot != null )
					snapshot.close();
			}
			return bitmap;
		}
	}


	//
	/**
	 * Creates a unique subdirectory of the designated app cache directory. Tries to use external
	 * but if not mounted, falls back on internal storage.
	 * 
	 * @param context - the application context
	 * @param uniqueName - a name for the cache directory
	 * @return
	 */
	public static File getDiskCacheDir(Context context, String uniqueName) {
		//Check if media is mounted or storage is built-in, if so, try and use external cache dir
		//otherwise use internal cache dir
		final String cachePath = Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
								!Environment.isExternalStorageRemovable() ? context.getExternalCacheDir().getPath() : context.getCacheDir().getPath();

		return new File(cachePath + File.separator + uniqueName);
	}
}
