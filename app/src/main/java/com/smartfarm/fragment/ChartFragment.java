package com.smartfarm.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.github.mikephil.charting.data.Entry;
import com.smartfarm.activity.R;
import com.smartfarm.bean.ChartBean;
import com.smartfarm.event.GlobalEvent;
import com.smartfarm.event.OverviewSensorCodeEvent;
import com.smartfarm.model.Equipment;
import com.smartfarm.observable.HistoryDataObservable;
import com.smartfarm.util.BaseProgressDialog;
import com.smartfarm.util.Config;
import com.smartfarm.util.DateUtil;
import com.smartfarm.util.ToastUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Subscriber;
import rx.schedulers.Schedulers;

public class ChartFragment extends BaseFragment {
	private View rootView;
	private ImageView chartListImageView;
	private ViewPager chartViewPager;
	private int currentSensorIndex;
	private ViewPagerAdapter adapter;
	private LruCache<String, Map<String,List<String>>> timeCache = 
			new LruCache<String, Map<String,List<String>>>(8);
	private LruCache<String, List<Entry>> dataCache =
			new LruCache<String, List<Entry>>(8);
	private List<View> views;
	private View downLoadFial;
	private String equipmentCode;
	private List<String> sensorName;
	/**传感器的种类例如
	 * [
	 "000031007-hs-1-001",
	 "000031007-ls-1-001",
	 "000031007-ts-1-001",
	 "000031007-shs-1-001",
	 "000031007-sts-1-001",
	 "000031007-co2s-1-001"
	 ]*/
	private List<String> sensorCode;
	private List<String> historyDate;
	private BaseProgressDialog dialog;
	private Activity activity = null;
	private TextView chartEquipmentNames;
	private TextView chartEquipmentCodes;
	//Event
	EventHandler eventHandler;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.activity = activity;
		registerEvent();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Config config = new Config(activity);
		config.setIsFirstOpen("isFirstOpen", false);
		config = null;

		System.out.println("chartfragment onDestroy");
	}

	@Override
	public void onDetach() {
		unregisterEvent();
		super.onDetach();

		System.out.println("chartfragment onDetach()");
	}

	private class EventHandler{
		public void onEvent(OverviewSensorCodeEvent overviewSensorCodeEvent){
			sensorName = overviewSensorCodeEvent.sensorName;
			equipmentCode = overviewSensorCodeEvent.equipmentCode;
			sensorCode = overviewSensorCodeEvent.sensorCode;
			historyDate = overviewSensorCodeEvent.newTime;
		}
	}

	private void registerEvent(){
		eventHandler = new EventHandler();
		GlobalEvent.bus.register(eventHandler);
	}

	private void unregisterEvent(){
		GlobalEvent.bus.unregister(eventHandler);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		views = new ArrayList<>();
	}

	private boolean isFirstOpenApp(){
		Config config = new Config(activity);
		boolean result = config.getIsFirstOpen("isFirstOpen");
		config.setIsFirstOpen("isFirstOpen", false);
		return result;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_chart, container, false);
		chartListImageView = (ImageView)rootView.findViewById(R.id.chart_list_imageview);
		chartEquipmentNames = (TextView) rootView.findViewById(R.id.chart_equiment_name);
		chartEquipmentCodes = (TextView)rootView.findViewById(R.id.chart_equipment_code);
		chartViewPager = (ViewPager) rootView
				.findViewById(R.id.fragment_chartview);
		downLoadFial = inflater.inflate(R.layout.fragment_chart_download_fail,
				container, false);
		initViewPager();
		return rootView;
	}

	//显示传感器信息的弹出式菜单
	private void showPopupMenu(View view) {
		PopupMenu popupMenu = new PopupMenu(activity, view);
		Menu menu = popupMenu.getMenu();
		List<String> menuName = sensorName;
		for (int i = 0; i < menuName.size(); i++) {
			menu.add(Menu.NONE, Menu.FIRST + i, i, menuName.get(i));
		}
		activity.getMenuInflater().inflate(R.menu.popup, popupMenu.getMenu());
		popupMenu.setOnMenuItemClickListener(new popupMenuListener());
		popupMenu.show();
	}

	private class popupMenuListener implements PopupMenu.OnMenuItemClickListener {
		@Override
		public boolean onMenuItemClick(MenuItem item) {
			int order = item.getOrder();
			chartViewPager.setCurrentItem(order);
			loadData(order);
			return true;
		}
	}

	//现阶段因为不知道具体的传感器，所以要在代码中转化，如果能在下载数据中直接获取则无需此操作
	private List<String> getNameFromSensorCode(List<String> in){
		ArrayList<String> result = new ArrayList<>();
		String tmp;
		for (int i=0;i<in.size();i++){
			tmp = in.get(i).substring(in.get(i).indexOf("-") + 1);
			tmp = tmp.substring(0,tmp.indexOf("-"));
			if (tmp.equals("hs")){
				result.add("湿度传感器");
			}else if(tmp.equals("ls")){
				result.add("光强传感器");
			}else if (tmp.equals("ts")){
				result.add("温度传感器");
			}else if(tmp.equals("sphs")){
				result.add("土壤酸碱度传感器");
			}else if (tmp.equals("shs")){
				result.add("土壤湿度传感器");
			}else if(tmp.equals("sts")){
				result.add("土壤温度传感器");
			}else if (tmp.equals("ats")){
				result.add("空气温度传感器");
			}else if (tmp.equals("co2s")){
				result.add("二氧化碳传感器");
			}else if(tmp.equals("ahs")){
				result.add("空气湿度传感器");
			}else{
				result.add("未知传感器");
			}
		}
		return  result;
	}

	private void initViewPager() {
		dialog = new BaseProgressDialog(activity);

		adapter = new ViewPagerAdapter(views);

		chartViewPager.setAdapter(adapter);

		chartViewPager.addOnPageChangeListener(new OnPageChangeListener() {
			//此方法是页面跳转完后得到调用，arg0是你当前选中的页面的Position（位置编号）。
			@Override
			public void onPageSelected(int which) {
				loadData(which);
			}
			//当页面在滑动的时候会调用此方法，在滑动被停止之前，此方法回一直得到调用。
			//arg0 :当前页面，及你点击滑动的页面
			//arg1:当前页面偏移的百分比
			//arg2:当前页面偏移的像素位置
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}
			//此方法是在状态改变的时候调用，其中arg0这个参数有三种状态（0，1，2）。
			// arg0 ==1的时表示正在滑动，
			// arg0==2的时表示滑动完毕了，
			// arg0==0的时表示什么都没做。
			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});
		//功能弹窗选择要显示的viewpager
		chartListImageView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showPopupMenu(v);
			}
		});
	}

	// 获取传感器编号的接口
	public void initHistoryFragment(int index) {
		currentSensorIndex = index;
		RefreshViewPager();
	}

	private void RefreshViewPager() {
		chartEquipmentNames.setText(Equipment.getEquipmentName(activity, equipmentCode));
		//chartEquipmentCodes.setText(equipmentCode);
		if (sensorCode == null || sensorCode.size() < 0) {
			showEmptyPager();
		} else {
			//页数设置为传感器的种类个数
			int numPage = sensorCode.size();
			try {
				views.clear();
			} catch (Exception e) {
				e.printStackTrace();
			}
			for (int i = 0; i < numPage; ++i) {
				views.add(new ChartFragmentItem(activity));
			}
			adapter.setViewList(views);
			adapter.notifyDataSetChanged();
			chartViewPager.setCurrentItem(currentSensorIndex);
			loadData(currentSensorIndex);
			if (isFirstOpenApp()){
				ToastUtil.showLong(activity,"左右滑动改变查看不同历史数据");
			}
		}
	}
	
	private void getHistoryDataObservable(final int currentIndex,String code,String startDate,String endDate){
		HistoryDataObservable.createObservable(code, startDate, endDate)
			.subscribeOn(Schedulers.newThread()).subscribe(new Subscriber<ChartBean>() {
			@Override
			public void onStart() {
				super.onStart();
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						dialog.setMessage("正在下载数据");
						dialog.show();
					}
				});
			}

			@Override
			public void onCompleted() {
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						dialog.dismiss();
					}
				});
			}

			@Override
			public void onError(Throwable e) {
				e.printStackTrace();
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						dialog.dismiss();
						((ChartFragmentItem) views.get(currentIndex))
								.showEmptyText();
					}
				});
			}

			@Override
			public void onNext(final ChartBean response) {
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (response != null) {
							timeCache.put(sensorCode.get(currentIndex), historyData2Cache_1(response));
							dataCache.put(sensorCode.get(currentIndex), historyData2Cache_2(response));
							dialog.dismiss();
							((ChartFragmentItem) views.get(currentIndex)).showLineChart(
									timeCache.get(sensorCode.get(currentIndex)),
									dataCache.get(sensorCode.get(currentIndex)),
									historyDate.get(currentIndex));
						} else {
							dialog.dismiss();
							((ChartFragmentItem) views.get(currentIndex))
									.showEmptyText();
						}
					}
				});
			}
		});
	}
	//一次只能有一个线程进入该方法,其他线程要想在此时调用该方法,
	// 只能排队等候,当前线程(就是在synchronized方法内部的线程)执行完该方法后,别的线程才能进入.
	private synchronized void loadData(final int currentIndex) {
		Log.d("gzfuzhi", "Lru cache size: " + timeCache.size() + dataCache.size());
		Map<String,List<String>> cache1 = timeCache.get(sensorCode.get(currentIndex));
		List<Entry> cache2 = dataCache.get(sensorCode.get(currentIndex));
		if (cache1 != null && cache2 !=null) {
			((ChartFragmentItem) views.get(currentIndex)).showLineChart(cache1,cache2,
					historyDate.get(currentIndex));
			return;
		}
		getHistoryDataObservable(currentIndex, sensorCode.get(currentIndex),
				historyDate.get(currentIndex), historyDate.get(currentIndex));
	}
	//获取监控时间,监控时间与监控数据一一对应，数目一样
	private Map<String,List<String>> historyData2Cache_1(ChartBean historyData){
		int numData = historyData.getTime().size();
		int startPoint = 0;
		//只显示最近的100个点
		if (numData > 100)
			startPoint = numData - 100;
		String name = historyData.getName();
		String unit = historyData.getUnit();
		List<String> name2Save = new ArrayList<String>();
		List<String> unit2Save = new ArrayList<String>();
		name2Save.add(name);
		unit2Save.add(unit);
		//注意使用subList方法时，修改subList会影响原来的list，左闭右开
		List<String> times = historyData.getTime().subList(startPoint, numData);
		List<String> time2Save = new ArrayList<String>();
		String time = "";
		String timeFormat;
		SimpleDateFormat simpleFormat = new SimpleDateFormat("HH:mm");	//MM-dd/HH:mm
		for (int i = 0; i < times.size(); i++) {
			time = times.get(i);
			try {
				Date d = DateUtil.dateFormat.parse(time);
				timeFormat = simpleFormat.format(d);
			} catch (Exception e) {
				timeFormat = "";
			}
			time2Save.add(timeFormat);
		}
		//上下限
//		List<String> upSchema = new ArrayList<String>();
//		upSchema.add(historyData.getUpSchema().get(0));
//		List<String> lowSchema = new ArrayList<String>();
//		lowSchema.add(historyData.getLowSchema().get(0));
		
		Map<String,List<String>> historyData2Save = new HashMap<String, List<String>>();;
		historyData2Save.put("name", name2Save);
		historyData2Save.put("unit", unit2Save);
		historyData2Save.put("time", time2Save);
//		historyData2Save.put("upSchema", upSchema);
//		historyData2Save.put("lowSchema", lowSchema);
		
		return historyData2Save;
	}
	//获取监控数据，监控时间与监控数据一一对应，数目一样
	private List<Entry> historyData2Cache_2(ChartBean historyData){
		int numData = historyData.getData().size();
		int startPoint = 0;
		if (numData > 100)
			startPoint = numData - 100;
		List<String> datas = historyData.getData().subList(startPoint, numData);
		List<Entry> data2Save = new ArrayList<Entry>();
		String data = "";
		for (int i = 0; i < datas.size(); i ++) {
			data = datas.get(i);
			data2Save.add(new Entry(Float.parseFloat(data), i));
		}
		return data2Save;
	}
	
	public void showEmptyPager() {
		final List<View> views = new ArrayList<>();
		views.add(downLoadFial);
		adapter.setViewList(views);
		chartViewPager.setAdapter(adapter);
	}

	private class ViewPagerAdapter extends PagerAdapter {
		private List<View> listViews;

		public ViewPagerAdapter(List<View> list) {
			listViews = list;
		}

		public void setViewList(List<View> views) {
			listViews = views;
		}

		@Override
		public int getCount() {
			return listViews.size();
		}

		@Override
		public int getItemPosition(Object object) {
			return POSITION_NONE;
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			Log.d("gzfuzhi", "InstantiateItem " + position);
			View v = listViews.get(position);
			container.addView(v);
			return v;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			//return super.getPageTitle(position);
			return sensorCode.get(position);
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			Log.d("gzfuzhi", "Destroy  " + position);
			container.removeView((View) object);
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}
	}
}
