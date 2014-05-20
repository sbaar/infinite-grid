package webview;

import com.appliedideas.infinitegrid.R;
import com.kennethmaffei.infinitegrid.Constants;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;

/**
 * After clicking a tile, we open up a webview
 * 
 * @author Kenneth Maffei
 *
 */
public class WebViewActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.webview_layout);
		
		final View mainView = findViewById(android.R.id.content);
		mainView.setBackgroundColor(Color.TRANSPARENT);
		
		//Get the url we are going to through the bundle
		Bundle bundle = getIntent().getExtras();
		String url = bundle.getString(Constants.WEBVIEW_INTENT_URL);
		WebView wv = (WebView) findViewById(R.id.webView1);	
		wv.loadUrl(url);
	}
}
