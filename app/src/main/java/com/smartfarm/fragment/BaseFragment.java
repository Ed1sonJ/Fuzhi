package com.smartfarm.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class BaseFragment extends Fragment {
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		Log.d("gzfuzhi", this.toString() + " onAttach");
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("gzfuzhi", this.toString() + " onCreate");
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Log.d("gzfuzhi", this.toString() + " onActivityCreated");
	}

	@Override
	public void onStart() {
		super.onStart();
		Log.d("gzfuzhi", this.toString() + " onStart");
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.d("gzfuzhi", this.toString() + " onResume");
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.d("gzfuzhi", this.toString() + " onPause");
	}

	@Override
	public void onStop() {
		super.onStop();
		Log.d("gzfuzhi", this.toString() + " onStop");
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		Log.d("gzfuzhi", this.toString() + " onDestroyView");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d("gzfuzhi", this.toString() + " onDestroy");
	};

	@Override
	public void onDetach() {
		super.onDetach();
		Log.d("gzfuzhi", this.toString() + " onDetach");
	}
}
