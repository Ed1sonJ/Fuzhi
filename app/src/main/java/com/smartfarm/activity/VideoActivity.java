package com.smartfarm.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.smartfarm.fragment.BaseFragment;
import com.smartfarm.util.ToastUtil;

import io.vov.vitamio.LibsChecker;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.VideoView;

public class VideoActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction().add(R.id.video_container, new PlaceholderFragment()).commit();
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    //占位fragment
    public static class PlaceholderFragment extends BaseFragment {
        protected final String path = "rtmp://v.gzfuzhi.com/mytv/test";
        protected RelativeLayout progressLayout;
        protected VideoView mVideoView;
        protected Activity activity;

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            this.activity = activity;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_video, container, false);
            if (!LibsChecker.checkVitamioLibs(getActivity()))
                return null;
            progressLayout = (RelativeLayout) rootView.findViewById(R.id.video_progress_layout);

            mVideoView = (VideoView) rootView.findViewById(R.id.vitamio_videoView);
            mVideoView.setBufferSize(128);
            mVideoView.setVideoPath(path);
            mVideoView.setMediaController(new MediaController(getActivity()));
            mVideoView.requestFocus();
            mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    mediaPlayer.setPlaybackSpeed(1.0f);
                }
            });
            mVideoView.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                @Override
                public boolean onInfo(MediaPlayer mp, int what, int extra) {
                    switch (what) {
                        case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                            mp.pause();
                            ToastUtil.showShort(activity, "加载数据");
                            break;
                        case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                            progressLayout.setVisibility(View.GONE);
                            mp.start();
                            break;
                    }
                    return true;
                }
            });
            mVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    ToastUtil.showLong(activity, "加载数据出错，请稍后再试");
                    progressLayout.setVisibility(View.GONE);
                    activity.finish();
                    return false;
                }
            });
            progressLayout.setVisibility(View.VISIBLE);
            return rootView;
        }
    }
}
