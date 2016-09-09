package com.smartfarm.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class StaticViewPager extends ViewPager {

	public StaticViewPager(Context context) {
		super(context);
		// TODO 自动生成的构造函数存根
	}

	public StaticViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO 自动生成的构造函数存根
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
