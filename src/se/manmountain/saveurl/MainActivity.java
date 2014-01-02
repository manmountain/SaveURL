package se.manmountain.saveurl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import se.mannberg.saveurl.R;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

public class MainActivity extends Activity {

	protected static final String TAG = "SaveURL";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Get intent, action and MIME type
	    Intent intent = getIntent();
	    String action = intent.getAction();
	    String type = intent.getType();
	    // Did we get a share intent?
	    if (Intent.ACTION_SEND.equals(action) && type != null) {
	    	if ("text/plain".equals(type)) {
	    		// Handle text being sent
	    		String urlText = intent.getStringExtra(Intent.EXTRA_TEXT);
	    		if (urlText != null) {
	    			// we got something shared with us, is it a valid url?
	    			if (UrlHelper.isValidURL(urlText)) {
	    				// is the download manager available?
	    				if (isDownloadManagerAvailable(this)) {
	    					String uriFile = UrlHelper.getFileName(urlText);
	    					if (uriFile != null) {
	    						DownloadManager.Request request = new DownloadManager.Request(Uri.parse(urlText));
	    						//request.setDescription("Some description");
	    						request.setTitle(uriFile);
	    						// in order for this if to run, you must use the android 3.2 to compile your app
	    						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
	    							request.allowScanningByMediaScanner();
	    							request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
	    						}
	    						request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, uriFile);
	    						// get download service and enqueue file
	    						DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
	    						manager.enqueue(request);
	    					} else {
	    						//ERROR
	    						Toast.makeText(getApplicationContext(), "Error downloading url", Toast.LENGTH_LONG).show();
	    					}
	    				} else {
	    					//ERROR
    						Toast.makeText(getApplicationContext(), "Download manager not avvailable", Toast.LENGTH_LONG).show();
	    				}
	    			} else {
	    				//ERROR
						Toast.makeText(getApplicationContext(), "Not a valid url", Toast.LENGTH_LONG).show();
	    			}
	    		}
	    	}
	    } else {
	        // Handle other intents, such as being started from the home screen
			Toast.makeText(getApplicationContext(), "This app is not useful when launched from the home screen. Use the share menu instead.", Toast.LENGTH_LONG).show();
	    }
	    finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	private boolean isDownloadManagerAvailable(Context context) {
	    try {
	        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
	            return false;
	        }
	        Intent intent = new Intent(Intent.ACTION_MAIN);
	        intent.addCategory(Intent.CATEGORY_LAUNCHER);
	        intent.setClassName("com.android.providers.downloads.ui", "com.android.providers.downloads.ui.DownloadList");
	        List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
	        return list.size() > 0;
	    } catch (Exception e) {
	        return false;
	    }
	}
	
	private static class UrlHelper {
		
		public static String getFileName(String url) {
			return getFileName(url, 0);
		}
		public static String getFileName(String url, int cnt) {
			if (cnt > 1) {
				return null;
			}
			URI uri;
			try {
				uri = new URI(url);
				// Get the parts from the url
				String uriHost = uri.getHost();
				String uriPath = uri.getPath();
				String uriFile = uriPath.substring(uriPath.lastIndexOf('/') + 1);
				String uriQuery = uri.getQuery();
				if (uriFile != null) {
					if (uriFile.contentEquals("")) {
						// Something went terrible wrong ..
						// .. but we can test to apply a index.html on the url try agian
						if (uriPath.endsWith("/")) {
							uriFile = getFileName(uriHost+uriPath+"index.html", cnt+1);
						} else {
							uriFile = getFileName(uriHost+uriPath+"/index.html", cnt+1);
						}
						return uriFile;
					}
					return uriFile;
				}
				return null;
			} catch (URISyntaxException e) {
				Log.d(TAG,"Malformed URI:\n"+url);
				return null;
			}
		}
		
		public static Boolean isValidURL(String url) {
			try {
				URI uri = new URI(url);
				return true;
			} catch (URISyntaxException e) {
				Log.d(TAG,"Malformed URI:\n"+url);
				return false;
			}
		}
		
	}

}
