package com.videogo.ui.discovery;

import java.util.ArrayList;

import com.videogo.open.R;
import com.videogo.openapi.EZSquareChannel;
import com.videogo.widget.TitleBar;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;

public class SquareVideoListActivity2 extends FragmentActivity {
	private ViewPager viewPager;
	private PagerTabStrip tabs;
	private ArrayList<EZSquareChannel> mSquareChannelList;
	private int columnPosition = 0;
	private View emptyView;
	private TitleBar titleBar;
	
	public interface EXTRA_KEY {
		String COLUMN_INFOS = "columnInfos";
		String COLUMN_POSITION = "columnPosition";
	}
	
	@Override
	protected void onCreate(Bundle savedInstance) {
		super.onCreate(savedInstance);
		setContentView(R.layout.activity_square_video_list2);
		titleBar = (TitleBar) findViewById(R.id.title_bar);
		titleBar.setTitle(R.string.video_square);
		titleBar.addRightTextButton(getString(R.string.my_collect), new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(SquareVideoListActivity2.this, MyCollectActivity.class);
				startActivity(intent);
			}
		});
		viewPager = (ViewPager) findViewById(R.id.pager);
		tabs = (PagerTabStrip) findViewById(R.id.pager_tab_strip);
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			mSquareChannelList = bundle.getParcelableArrayList(EXTRA_KEY.COLUMN_INFOS);
			columnPosition = bundle.getInt(EXTRA_KEY.COLUMN_POSITION);
		}
		viewPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager()));
		viewPager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int arg0) {
				
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				
			}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {
				
			}
		});
        emptyView = findViewById(R.id.emptyView);
		if (mSquareChannelList == null || mSquareChannelList.size() == 0) {
			emptyView.setVisibility(View.VISIBLE);
			viewPager.setVisibility(View.GONE);
		} else {
            viewPager.setOffscreenPageLimit(Math.max(1, mSquareChannelList.size() / 2));
        }
		viewPager.setCurrentItem(columnPosition);
	}
	
	private class ViewPagerAdapter extends FragmentPagerAdapter {
		
		public ViewPagerAdapter(FragmentManager manager) {
			super(manager);
		}
		@Override
		public Fragment getItem(int position) {
			return SquareVideoListFragment.newInstance(mSquareChannelList.get(position), false);
		}

		@Override
		public int getCount() {
			return mSquareChannelList.size();
		}
		
		@Override
		public CharSequence getPageTitle(int position) {
			if (mSquareChannelList != null) {
				if (position >= 0 && position < mSquareChannelList.size()) {
					return mSquareChannelList.get(position).getChannelName();
				}
			}
			return "No_Title";
		}
		
	}
}
