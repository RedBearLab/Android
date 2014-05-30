package com.redbear.redbearbleclient;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;


/**
 * 
 * 
 *  Setting Page
 * 
 * @author James
 *
 */
public class SettingPage extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		RelativeLayout mLayout = new RelativeLayout(this); 
		mLayout.setBackgroundColor(Color.WHITE);
		
		LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT); 
		params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
		
		ImageView imageView = new ImageView(this); 
		imageView.setImageResource(R.drawable.splash);
		imageView.setId(1);
		mLayout.addView(imageView, params);
		
		
		params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT); 
		params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
		params.addRule(RelativeLayout.BELOW, 1);
		
		TextView textView = new TextView(this);

		textView.setTextColor(Color.BLACK);
		textView.setGravity(Gravity.CENTER);
		textView.setTextSize(25);
		textView.setText("BLE Controller v1.0");
		mLayout.addView(textView, params);
		
		setContentView(mLayout);

		final ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

	}

	@Override
	public void onBackPressed() {
		
		close();
		
	}

	protected void close()
	{
		finish();
		overridePendingTransition(R.anim.slide_in_from_left,
				R.anim.slide_out_to_right);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			close(); 
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}
