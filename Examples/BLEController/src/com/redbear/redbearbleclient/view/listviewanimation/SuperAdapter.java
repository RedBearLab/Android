package com.redbear.redbearbleclient.view.listviewanimation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.animation.ValueAnimator.AnimatorUpdateListener;

/**
 * 
 * 
 * 带动画的ListView适配器
 * 
 * 可选择是否删除某项
 * 
 * @author James
 *
 */
public class SuperAdapter extends SingleAnimationAdapter {

	protected static final long DEFAULTANIMATIONDELAYMILLIS = 100;
	protected static final long DEFAULTANIMATIONDURATIONMILLIS = 300; 

	private final long mAnimationDelayMillis;
	private final long mAnimationDurationMillis;

	private OnDismissCallback mCallback;
	
	boolean isSlideToRemove = false;
	
	public SuperAdapter(BaseAdapter baseAdapter, OnDismissCallback callback) {
		this(baseAdapter,DEFAULTANIMATIONDELAYMILLIS,
				DEFAULTANIMATIONDURATIONMILLIS);
		mCallback = callback;
	}
	
	public SuperAdapter(BaseAdapter baseAdapter) {
		this(baseAdapter, DEFAULTANIMATIONDELAYMILLIS,
				DEFAULTANIMATIONDURATIONMILLIS);
	}

	public SuperAdapter(BaseAdapter baseAdapter, long animationDelayMillis) {
		this(baseAdapter, animationDelayMillis, DEFAULTANIMATIONDURATIONMILLIS);
	}

	public SuperAdapter(BaseAdapter baseAdapter, long animationDelayMillis,
			long animationDurationMillis) {
		super(baseAdapter);
		mAnimationDelayMillis = animationDelayMillis;
		mAnimationDurationMillis = animationDurationMillis;
	}

	protected long getAnimationDelayMillis() {
		return mAnimationDelayMillis;
	}

	protected long getAnimationDurationMillis() {
		return mAnimationDurationMillis;
	}

	@Override
	protected Animator getAnimator(ViewGroup parent, View view) {
		return ObjectAnimator.ofFloat(view, "translationX", parent.getWidth(), 0);
	} 
	
	public void animateDismiss(int index) {
		animateDismiss(Arrays.asList(index));
	}

	public void animateDismiss(Collection<Integer> positions) {
		final List<Integer> positionsCopy = new ArrayList<Integer>(positions);
		if(getAbsListView() == null) {
			throw new IllegalStateException("Call setListView() on this AnimateDismissAdapter before calling setAdapter()!");
		}

		List<View> views = getVisibleViewsForPositions(positionsCopy);
		Log.e("TAG", "positionsCopy: " + positionsCopy.toString());
		if (!views.isEmpty()) {
			List<Animator> animators = new ArrayList<Animator>();
			for (final View view : views) {
				animators.add(createAnimatorForView(view));
			}

			AnimatorSet animatorSet = new AnimatorSet();

			Animator[] animatorsArray = new Animator[animators.size()];
			for (int i = 0; i < animatorsArray.length; i++) {
				animatorsArray[i] = animators.get(i);
			}

			animatorSet.playTogether(animatorsArray);
			animatorSet.addListener(new AnimatorListener() {
 
				public void onAnimationStart(Animator arg0) {
				}
 
				public void onAnimationRepeat(Animator arg0) {
				}
 
				public void onAnimationEnd(Animator arg0) {
					invokeCallback(positionsCopy);
				}
 
				public void onAnimationCancel(Animator arg0) {
				}
			});
			animatorSet.start();
		} else {
			invokeCallback(positionsCopy);
		}
	}

	private void invokeCallback(Collection<Integer> positions) {
		ArrayList<Integer> positionsList = new ArrayList<Integer>(positions);
		Collections.sort(positionsList);
		int[] dismissPositions = new int[positionsList.size()];
		for (int i = 0; i < positionsList.size(); i++) {
			dismissPositions[i] = positionsList.get(positionsList.size() - 1 - i);
		} 
		mCallback.onDismiss(getAbsListView(), dismissPositions);
	}

	private List<View> getVisibleViewsForPositions(Collection<Integer> positions) {
		List<View> views = new ArrayList<View>();
		for (int i = 0; i < getAbsListView().getChildCount(); i++) {
			View child = getAbsListView().getChildAt(i);
			if (positions.contains(getAbsListView().getPositionForView(child))) {
				views.add(child);
			}
		}
		return views;
	}

	private Animator createAnimatorForView(final View view) {
		final ViewGroup.LayoutParams lp = view.getLayoutParams();
		final int originalHeight = view.getHeight();

		ValueAnimator animator = ValueAnimator.ofInt(originalHeight, 0);
		animator.addListener(new AnimatorListener() {

			public void onAnimationStart(Animator arg0) {
			}
 
			public void onAnimationRepeat(Animator arg0) {
			}
 
			public void onAnimationEnd(Animator arg0) {
				lp.height = 0;
				view.setLayoutParams(lp);
			}
 
			public void onAnimationCancel(Animator arg0) {
			}
		});

		animator.addUpdateListener(new AnimatorUpdateListener() {
 
			public void onAnimationUpdate(ValueAnimator valueAnimator) {
				lp.height = (Integer) valueAnimator.getAnimatedValue();
				view.setLayoutParams(lp);
			}
		});

		return animator;
	}
	
	@Override
	public void setAbsListView(AbsListView listView) {
		super.setAbsListView(listView);
		if(isSlideToRemove)
		{
			listView.setOnTouchListener(new SwipeDismissListViewTouchListener(listView, mCallback));
		}
	}

	public boolean isSlideToRemove() {
		return isSlideToRemove;
	}

	public void setSlideToRemove(boolean isSlideToRemove) {
		this.isSlideToRemove = isSlideToRemove;
	}
	
	
}
