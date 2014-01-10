package org.devwork.vocabtrain;

import android.app.Activity;
import android.app.Dialog;
import android.view.Window;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class RubyDialog extends Dialog
{

	protected RubyDialog(Activity activity, String html)
	{
		super(activity, true, null);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.ruby_dialog);
		WebView webview = (WebView) this.findViewById(R.id.rubydialog_view);
		WebSettings settings = webview.getSettings();
		settings.setSupportZoom(true);
		settings.setBuiltInZoomControls(true);
		settings.setTextSize(WebSettings.TextSize.LARGEST);
		webview.loadDataWithBaseURL(null, html, "text/html", "utf-8", null);
	}

}
