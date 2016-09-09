/* 
 * @ProjectName VideoGoJar
 * @Copyright HangZhou Hikvision System Technology Co.,Ltd. All Right Reserved
 * 
 * @FileName CameraListAdapter.java
 * @Description 这里对文件进行描述
 * 
 * @author chenxingyf1
 * @data 2014-7-14
 * 
 * @note 这里写本文件的详细功能描述和注释
 * @note 历史记录
 * 
 * @warning 这里写本文件的相关警告
 */
package com.videogo.ui.cameralist;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.videogo.open.R;
import com.videogo.openapi.bean.EZCameraInfo;
import com.videogo.universalimageloader.core.DisplayImageOptions;
import com.videogo.universalimageloader.core.ImageLoader;
import com.videogo.universalimageloader.core.assist.FailReason;
import com.videogo.universalimageloader.core.listener.ImageLoadingListener;
import com.videogo.util.LogUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * 摄像头列表适配器
 * @author chenxingyf1
 * @data 2014-7-14
 */
public class EZCameraListAdapter extends BaseAdapter {
    private static final String TAG = "CameraListAdapter";

    private Context mContext = null;
    private List<EZCameraInfo> mCameraInfoList = null;
    /** 监听对象 */
    private OnClickListener mListener;
    private ImageLoader mImageLoader;
    private ExecutorService mExecutorService = null;// 线程池
    public Map<String, EZCameraInfo> mExecuteItemMap = null;
    
    /**
     * 自定义控件集合
     * 
     * @author dengsh
     * @data 2012-6-25
     */
    public static class ViewHolder {
        public ImageView iconIv;

        public ImageView playBtn;

        public ImageView offlineBtn;

        public TextView cameraNameTv;
        
        public ImageButton cameraDelBtn;

        public ImageButton alarmListBtn;
        
        public ImageButton remoteplaybackBtn;

        public ImageButton setDeviceBtn;

        public View itemIconArea;

        public ImageView offlineBgBtn;
        
        public ImageButton deleteBtn;
        
        public ImageButton devicePicBtn;
        
        public ImageButton deviceVideoBtn;
        
        public View deviceDefenceRl;
        public ImageButton deviceDefenceBtn;
    }
    
    public EZCameraListAdapter(Context context) {
        mContext = context;
        mCameraInfoList = new ArrayList<EZCameraInfo>();
        mImageLoader = ImageLoader.getInstance();
        mExecuteItemMap = new HashMap<String, EZCameraInfo>();
    }
    
    public void clearImageCache() {
        mImageLoader.clearMemoryCache();
    }
    
    public void setOnClickListener(OnClickListener l) {
        mListener = l;
    }
    
    public void addItem(EZCameraInfo item) {
        mCameraInfoList.add(item);
    }

    public void removeItem(EZCameraInfo item) {
        for(int i = 0; i < mCameraInfoList.size(); i++) {
            if(item == mCameraInfoList.get(i)) {
                mCameraInfoList.remove(i);
            }
        }
    }
    
    public void clearItem() {
        //mExecuteItemMap.clear();
        mCameraInfoList.clear();
    }
    
    /* (non-Javadoc)
     * @see android.widget.Adapter#getCount()
     */
    @Override
    public int getCount() {
        return mCameraInfoList.size();
    }

    /* (non-Javadoc)
     * @see android.widget.Adapter#getItem(int)
     */
    @Override
    public EZCameraInfo getItem(int position) {
    	EZCameraInfo item = null;
        if (position >= 0 && getCount() > position) {
            item = mCameraInfoList.get(position);
        }
        return item;
    }

    /* (non-Javadoc)
     * @see android.widget.Adapter#getItemId(int)
     */
    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // 自定义视图
        ViewHolder viewHolder = null;
        if (convertView == null) {
            viewHolder = new ViewHolder();

            // 获取list_item布局文件的视图
            convertView = LayoutInflater.from(mContext).inflate(R.layout.cameralist_small_item, null);

            // 获取控件对象
            viewHolder.iconIv = (ImageView) convertView.findViewById(R.id.item_icon);
            viewHolder.iconIv.setDrawingCacheEnabled(false);
            viewHolder.iconIv.setWillNotCacheDrawing(true);
            viewHolder.playBtn = (ImageView) convertView.findViewById(R.id.item_play_btn);

            viewHolder.offlineBtn = (ImageView) convertView.findViewById(R.id.item_offline);
            viewHolder.cameraNameTv = (TextView) convertView.findViewById(R.id.camera_name_tv);
            viewHolder.cameraDelBtn = (ImageButton) convertView.findViewById(R.id.camera_del_btn);
            viewHolder.alarmListBtn = (ImageButton) convertView.findViewById(R.id.tab_alarmlist_btn);            
            viewHolder.remoteplaybackBtn = (ImageButton) convertView.findViewById(R.id.tab_remoteplayback_btn);
            viewHolder.setDeviceBtn = (ImageButton) convertView.findViewById(R.id.tab_setdevice_btn);
            viewHolder.offlineBgBtn = (ImageView) convertView.findViewById(R.id.offline_bg);
            viewHolder.itemIconArea = convertView.findViewById(R.id.item_icon_area);
            viewHolder.deleteBtn = (ImageButton) convertView.findViewById(R.id.camera_del_btn);
            viewHolder.devicePicBtn = (ImageButton) convertView.findViewById(R.id.tab_devicepicture_btn);
            viewHolder.deviceVideoBtn = (ImageButton) convertView.findViewById(R.id.tab_devicevideo_btn);
            viewHolder.deviceDefenceRl = convertView.findViewById(R.id.tab_devicedefence_rl);
            viewHolder.deviceDefenceBtn = (ImageButton) convertView.findViewById(R.id.tab_devicedefence_btn);
            
            // 设置点击图标的监听响应函数
            viewHolder.playBtn.setOnClickListener(mOnClickListener);

            // 设置删除的监听响应函数
            viewHolder.cameraDelBtn.setOnClickListener(mOnClickListener);

            // 设置报警列表的监听响应函数
            viewHolder.alarmListBtn.setOnClickListener(mOnClickListener);
            
            // 设置历史回放的监听响应函数
            viewHolder.remoteplaybackBtn.setOnClickListener(mOnClickListener);

            // 设置设备设置的监听响应函数
            viewHolder.setDeviceBtn.setOnClickListener(mOnClickListener);
            
            viewHolder.deleteBtn.setOnClickListener(mOnClickListener);
            
            viewHolder.devicePicBtn.setOnClickListener(mOnClickListener);
            
            viewHolder.deviceVideoBtn.setOnClickListener(mOnClickListener);
            
            viewHolder.deviceDefenceBtn.setOnClickListener(mOnClickListener);
            
            // 设置控件集到convertView
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        
        // 设置position
        viewHolder.playBtn.setTag(position);
        viewHolder.remoteplaybackBtn.setTag(position);
        viewHolder.alarmListBtn.setTag(position);
        viewHolder.setDeviceBtn.setTag(position);
        viewHolder.deleteBtn.setTag(position);
        viewHolder.devicePicBtn.setTag(position);
        viewHolder.deviceVideoBtn.setTag(position);
        viewHolder.deviceDefenceBtn.setTag(position);


        final EZCameraInfo cameraInfo = getItem(position);
        if(cameraInfo != null) {
            if (cameraInfo.getOnlineStatus() == 0) {
                viewHolder.offlineBtn.setVisibility(View.VISIBLE);
                viewHolder.offlineBgBtn.setVisibility(View.VISIBLE);
                viewHolder.playBtn.setVisibility(View.GONE);
                viewHolder.deviceDefenceRl.setVisibility(View.INVISIBLE);
            } else {
                viewHolder.offlineBtn.setVisibility(View.GONE);
                viewHolder.offlineBgBtn.setVisibility(View.GONE);
                viewHolder.playBtn.setVisibility(View.VISIBLE);
                viewHolder.deviceDefenceRl.setVisibility(View.VISIBLE);
            }

            // 如果是分享设备，隐藏消息列表按钮和设置按钮
            if(cameraInfo.getShareStatus() != 0 && cameraInfo.getShareStatus() != 1) {
                viewHolder.alarmListBtn.setVisibility(View.GONE);
                viewHolder.setDeviceBtn.setVisibility(View.GONE);
            } else {
                viewHolder.alarmListBtn.setVisibility(View.VISIBLE);
                viewHolder.setDeviceBtn.setVisibility(View.VISIBLE);
            }
            
            viewHolder.cameraNameTv.setText(cameraInfo.getCameraName());   
            viewHolder.iconIv.setVisibility(View.INVISIBLE);

            String imageUrl = cameraInfo.getPicUrl();
            if(!TextUtils.isEmpty(imageUrl)) {
                DisplayImageOptions options = new DisplayImageOptions.Builder()
                        .cacheInMemory(true)//设置下载的图片是否缓存在内存中
                        .cacheOnDisk(true)//设置下载的图片是否缓存在SD卡中
                        .considerExifParams(true)  //是否考虑JPEG图像EXIF参数（旋转，翻转）
                        .build();//构建完成
                // 依次从内存和sd中获取，如果没有则网络下载
                mImageLoader.displayImage(imageUrl, viewHolder.iconIv, options, mImgLoadingListener);
            }
        }
        
        return convertView;
    }

    private final ImageLoadingListener mImgLoadingListener = new ImageLoadingListener() {

        @Override
        public void onLoadingStarted(String imageUri, View view) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
            LogUtil.errorLog(TAG, "onLoadingFailed: " + failReason.toString());
        }

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            if (view != null && view instanceof ImageView && loadedImage != null) {
                ImageView imgView = (ImageView) view;
                imgView.setImageBitmap(loadedImage);
                imgView.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onLoadingCancelled(String imageUri, View view) {
            // TODO Auto-generated method stub

        }
    };
        
    public void shutDownExecutorService() {
        if (mExecutorService != null) {
            if (!mExecutorService.isShutdown()) {
                mExecutorService.shutdown();
            }
            mExecutorService = null;
        }
    }
    
    private View.OnClickListener mOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (mListener != null) {
                int position = (Integer) v.getTag();
                int i = v.getId();
                if (i == R.id.item_play_btn) {
                    mListener.onPlayClick(EZCameraListAdapter.this, v, position);

                } else if (i == R.id.tab_remoteplayback_btn) {
                    mListener.onRemotePlayBackClick(EZCameraListAdapter.this, v, position);

                } else if (i == R.id.tab_alarmlist_btn) {
                    mListener.onAlarmListClick(EZCameraListAdapter.this, v, position);

                } else if (i == R.id.tab_setdevice_btn) {
                    mListener.onSetDeviceClick(EZCameraListAdapter.this, v, position);

                } else if (i == R.id.camera_del_btn) {
                    mListener.onDeleteClick(EZCameraListAdapter.this, v, position);

                } else if (i == R.id.tab_devicepicture_btn) {
                    mListener.onDevicePictureClick(EZCameraListAdapter.this, v, position);

                } else if (i == R.id.tab_devicevideo_btn) {
                    mListener.onDeviceVideoClick(EZCameraListAdapter.this, v, position);

                } else if (i == R.id.tab_devicedefence_btn) {
                    mListener.onDeviceDefenceClick(EZCameraListAdapter.this, v, position);

                }
            }
        }
    };
    
    public interface OnClickListener {

        public void onPlayClick(BaseAdapter adapter, View view, int position);

        public void onDeleteClick(BaseAdapter adapter, View view, int position);
        
        public void onAlarmListClick(BaseAdapter adapter, View view, int position);
        
        public void onRemotePlayBackClick(BaseAdapter adapter, View view, int position);
        
        public void onSetDeviceClick(BaseAdapter adapter, View view, int position);
        
        public void onDevicePictureClick(BaseAdapter adapter, View view, int position);
        
        public void onDeviceVideoClick(BaseAdapter adapter, View view, int position);
        
        public void onDeviceDefenceClick(BaseAdapter adapter, View view, int position);
    }
}
