package com.smartfarm.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class StaticViewPager extends ViewPager {

	public StaticViewPager(Context context) {
		super(context);
		// TODO �Զ����ɵĹ��캯�����
	}

	public StaticViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO �Զ����ɵĹ��캯�����
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		return false;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {

		return false;
	}

}
