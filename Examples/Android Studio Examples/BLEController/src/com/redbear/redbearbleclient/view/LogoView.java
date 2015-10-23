package com.redbear.redbearbleclient.view;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.redbear.redbearbleclient.R;

/**
 * 
 * 
 *  Logo控件
 * 
 * @author James
 *
 */
public class LogoView extends View {

	int view_width; 
	int view_height;
	BitmapDrawable image_logo; 
	int color_logobg = 0xffffffff;
	Rect rect_logo; 
 
	
	public LogoView(Context context) {
		super(context);
		initRes();
	}
	
	void initRes()
	{
		image_logo =  new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.splash));
	 }

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
	 	view_width = w;
		view_height = h;
		Log.e("scrrenDesity", view_width+" "+view_height);
		genRects();
		
	}
	
	/**
	 * 
	 * 初始化各元素位置，大小
	 * 
	 */
	protected void genRects()
	{
		Log.e("tttt", "view_height : " + view_height); 
		
		
		
		/**
		 * 
		 *  顶部logo大小，宽度是屏幕宽度三分之一
		 * 
		 */
		int logo_width = image_logo.getIntrinsicWidth();
		int logo_height = image_logo.getIntrinsicHeight();
		int logo_target_width = view_width / 2;
		int logo_target_height = logo_height * logo_target_width / logo_width;
		 	
	 
		
		/**
		 * 
		 *  顶部logo位置，x轴是二分之一，y轴是三分之一
		 * 
		 */
		rect_logo = new Rect();
		rect_logo.left = view_width / 2 - logo_target_width / 2;
		rect_logo.right = view_width / 2 + logo_target_width / 2;
		rect_logo.top = view_height / 2 - logo_target_height / 2;
		rect_logo.bottom = view_height / 2 + logo_target_height / 2;
		
		image_logo.setBounds(rect_logo);
		
		 
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
		
		canvas.drawColor(color_logobg);  
		if(image_logo != null)
			image_logo.draw(canvas);
	 
	}

	/**
	 * 
	 * 用于过滤所有触摸事件
	 * 
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return true;
	} 
}
