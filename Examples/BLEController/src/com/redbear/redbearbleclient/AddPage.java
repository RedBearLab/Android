package com.redbear.redbearbleclient;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.TextView;


/**
 * 
 * 
 *  Add Page
 * 
 * @author James
 *
 */
public class AddPage extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		TextView textView = new TextView(this);

		textView.setTextColor(Color.BLACK);
		textView.setGravity(Gravity.CENTER);
		textView.setTextSize(25);
		textView.setText("That is a AddPage page");

		setContentView(textView);

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
