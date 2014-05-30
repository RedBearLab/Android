package com.redbear.redbearbleclient;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;

import com.redbear.redbearbleclient.view.LogoView;

/**
 * 
 * 
 *  SplashScreen
 * 
 * @author James
 *
 */
public class SplashScreen extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		LogoView mLogoView = new LogoView(this);
		 
		setContentView(mLogoView);
		
		mLogoView.postDelayed(new Runnable() {
			
			@Override
			public void run() {
			
				Intent intent = new Intent(SplashScreen.this, MainPage.class);
				startActivity(intent);
				overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
				finish();
			}
			
		}, 500);
	}

	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return true;
	}

}
