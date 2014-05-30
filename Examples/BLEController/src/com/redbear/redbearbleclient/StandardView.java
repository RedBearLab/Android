package com.redbear.redbearbleclient;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import com.redbear.RedBearService.RedBearService;
import com.redbear.redbearbleclient.MainPage.Device;

/**
 * 
 * 
 * StandardView Page
 * 
 * @author James
 * 
 */
public class StandardView extends Activity implements
		ActionBar.OnNavigationListener {

	Device mDevice = null;
	StandardViewFragmentForPinsEx mDummySectionFragment;
	RedBearService mRedBearService;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getDataFromIntent();

		mRedBearService = MainPage.instance.getReadBearService();

		setContentView(R.layout.activity_main_page);

		final ActionBar actionBar = getActionBar();
		// actionBar.setDisplayHomeAsUpEnabled(true);

		actionBar.setDisplayShowTitleEnabled(true);
		// actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

		// Set up the dropdown list navigation in the action bar.
		// actionBar.setListNavigationCallbacks(
		// // Specify a SpinnerAdapter to populate the dropdown list.
		// new android.widget.ArrayAdapter<String>(actionBar.getThemedContext(),
		// android.R.layout.simple_list_item_1,
		// android.R.id.text1, new String[] {
		// getString(R.string.title_section1) }), this);

		mDummySectionFragment = new StandardViewFragmentForPinsEx(mDevice,
				mRedBearService);

		getFragmentManager()
				.beginTransaction()
				.setCustomAnimations(android.R.animator.fade_in,
						android.R.animator.fade_out)
				.replace(R.id.container, mDummySectionFragment).commit();
	}

	protected void getDataFromIntent() {
		Intent mIntent = getIntent();

		if (mIntent != null) {
			mDevice = (Device) mIntent.getSerializableExtra("Device");
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_standardview, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		// case R.id.menu_add:
		// Intent mAdd = new Intent(this, AddPage.class);
		// startActivity(mAdd);
		// overridePendingTransition(R.anim.slide_in_from_right,
		// R.anim.slide_out_to_left);
		// break;
		case R.id.menu_disconnect:
			setResult(StandardViewFragmentForPinsEx.RST_CODE);

			super.finish();
			overridePendingTransition(R.anim.slide_in_from_left,
					R.anim.slide_out_to_right);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			setResult(StandardViewFragmentForPinsEx.RST_CODE);
			super.finish();
			overridePendingTransition(R.anim.slide_in_from_left,
					R.anim.slide_out_to_right);
		}

		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onNavigationItemSelected(int arg0, long arg1) {
		// TODO Auto-generated method stub
		return false;
	}
}
