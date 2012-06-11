package mobisocial.omnistanford;

import mobisocial.omnistanford.util.JavaScriptInterface;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

public class VisualizationActivity extends OmniStanfordBaseActivity {
	public static final String TAG = "VisualizationActivity";
	
	private WebView mWebView;
	private ProgressDialog mSpinner;

	
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.visualization);
	    
	    mSpinner = new ProgressDialog(this);
        mSpinner.setMessage("Loading...");

	    mWebView = (WebView) findViewById(R.id.webview);
	    mWebView.getSettings().setJavaScriptEnabled(true);
	    mWebView.addJavascriptInterface(new JavaScriptInterface(this), "Android");
	    mWebView.setWebChromeClient(new OmniStanfordWebChromeClient());

	    Long time = getIntent().getExtras().getLong("time");
	    String url = "http://omnistanford.herokuapp.com";
	    if(time != null) url += "/?time=" + time.toString();
	    mWebView.loadUrl(url);
	}
	
	private class OmniStanfordWebChromeClient extends WebChromeClient {
		@Override
		public void onProgressChanged(WebView view, int newProgress) {
			if(newProgress == 0) {
				mSpinner.show();
			} else if(newProgress == 100) {
				mSpinner.hide();
				mWebView.setVisibility(View.VISIBLE);
			}
			super.onProgressChanged(view, newProgress);
		}
	}
}
