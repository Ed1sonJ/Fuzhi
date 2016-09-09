package com.zxing.activity;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.ericssonlabs.R;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.zxing.camera.CameraManager;
import com.zxing.decoding.CaptureActivityHandler;
import com.zxing.decoding.InactivityTimer;
import com.zxing.decoding.RGBLuminanceSource;
import com.zxing.view.ViewfinderView;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;
/**
 * Initial the camera
 * @author Ryan.Tang
 */
public class CaptureActivity extends Activity implements Callback {

	private CaptureActivityHandler handler;
	private ViewfinderView viewfinderView;
	private boolean hasSurface;
	private Vector<BarcodeFormat> decodeFormats;
	private String characterSet;
	private InactivityTimer inactivityTimer;
	private MediaPlayer mediaPlayer;
	private boolean playBeep;
	private static final float BEEP_VOLUME = 0.10f;
	private boolean vibrate;
	private ImageView cancelScanButton;
	private ImageView function;
	private PopupWindow popupWindow;
	private String photo_path;
	private ProgressDialog mProgress;
	private Bitmap scanBitmap;
	private boolean isOpenLight;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.camera);
		//ViewUtil.addTopView(getApplicationContext(), this, R.string.scan_card);
		CameraManager.init(getApplication());
		viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
		cancelScanButton = (ImageView) this.findViewById(R.id.img_cancel_scan);
		function=(ImageView)findViewById(R.id.function_img);
		function.setOnClickListener(new ClickListener());
		hasSurface = false;
		inactivityTimer = new InactivityTimer(this);
	}
	@Override
	protected void onResume() {
		super.onResume();
		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
		SurfaceHolder surfaceHolder = surfaceView.getHolder();
		if (hasSurface) {
			initCamera(surfaceHolder);
		} else {
			surfaceHolder.addCallback(this);
			surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}
		decodeFormats = null;
		characterSet = null;

		playBeep = true;
		AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
		if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
			playBeep = false;
		}
		initBeepSound();
		vibrate = true;
		
		//quit the scan view
		cancelScanButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				CaptureActivity.this.finish();
			}
		});
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (handler != null) {
			handler.quitSynchronously();
			handler = null;
		}
		CameraManager.get().closeDriver();
	}

	@Override
	protected void onDestroy() {
		inactivityTimer.shutdown();
		super.onDestroy();
	}
	
	/**
	 * Handler scan result
	 * @param result
	 * @param barcode
	 */
	public void handleDecode(Result result, Bitmap barcode) {
		inactivityTimer.onActivity();
		playBeepSoundAndVibrate();
		String resultString = result.getText();
		//FIXME
		if (resultString.equals("")) {
			Toast.makeText(CaptureActivity.this, "Scan failed!", Toast.LENGTH_SHORT).show();
		}else {
//			System.out.println("Result:"+resultString);
			Intent resultIntent = new Intent();
			Bundle bundle = new Bundle();
			bundle.putString("result", resultString);
			resultIntent.putExtras(bundle);
			this.setResult(RESULT_OK, resultIntent);
		}
		CaptureActivity.this.finish();
	}
	
	private void initCamera(SurfaceHolder surfaceHolder) {
		try {
			CameraManager.get().openDriver(surfaceHolder);
		} catch (IOException ioe) {
			return;
		} catch (RuntimeException e) {
			return;
		}
		if (handler == null) {
			handler = new CaptureActivityHandler(this, decodeFormats,
					characterSet);
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (!hasSurface) {
			hasSurface = true;
			initCamera(holder);
		}

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		hasSurface = false;

	}

	public ViewfinderView getViewfinderView() {
		return viewfinderView;
	}

	public Handler getHandler() {
		return handler;
	}

	public void drawViewfinder() {
		viewfinderView.drawViewfinder();

	}

	private void initBeepSound() {
		if (playBeep && mediaPlayer == null) {
			// The volume on STREAM_SYSTEM is not adjustable, and users found it
			// too loud,
			// so we now play on the music stream.
			setVolumeControlStream(AudioManager.STREAM_MUSIC);
			mediaPlayer = new MediaPlayer();
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mediaPlayer.setOnCompletionListener(beepListener);

			AssetFileDescriptor file = getResources().openRawResourceFd(
					R.raw.beep);
			try {
				mediaPlayer.setDataSource(file.getFileDescriptor(),
						file.getStartOffset(), file.getLength());
				file.close();
				mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
				mediaPlayer.prepare();
			} catch (IOException e) {
				mediaPlayer = null;
			}
		}
	}

	private static final long VIBRATE_DURATION = 200L;

	private void playBeepSoundAndVibrate() {
		if (playBeep && mediaPlayer != null) {
			mediaPlayer.start();
		}
		if (vibrate) {
			Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
			vibrator.vibrate(VIBRATE_DURATION);
		}
	}

	/**
	 * When the beep has finished playing, rewind to queue up another one.
	 */
	private final OnCompletionListener beepListener = new OnCompletionListener() {
		public void onCompletion(MediaPlayer mediaPlayer) {
			mediaPlayer.seekTo(0);
		}
	};

	private class ClickListener implements OnClickListener{
		@Override
		public void onClick(View v) {
			functionPopwin();
		}
	}
	private void functionPopwin()
	{
		LayoutInflater inflater = LayoutInflater.from(CaptureActivity.this);
		View view = inflater.inflate(R.layout.popwin_layout, null);
		popupWindow = new PopupWindow();
		popupWindow.setContentView(view);
		popupWindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
		popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
		// ��Ҫ����һ�´˲����������߿���ʧ
		popupWindow.setBackgroundDrawable(new ColorDrawable(
				android.graphics.Color.BLACK));
		// ���õ��������ߴ�����ʧ
		popupWindow.setOutsideTouchable(true);
		// ���ô˲�����ý��㣬�����޷����
		popupWindow.setFocusable(true);
		// popupWindow.showAtLocation(findViewById(R.id.manufacturer_register_typeBut),
		// Gravity.LEFT|Gravity.BOTTOM, 0, 0);
		popupWindow.showAsDropDown(function,0,10);
		RelativeLayout qr=(RelativeLayout)view.findViewById(R.id.qr_code);
		RelativeLayout light=(RelativeLayout)view.findViewById(R.id.light);
		qr.setOnClickListener(new QrClickListener());
		light.setOnClickListener(new LightClickListener());
	}
	//�������
	private class LightClickListener implements OnClickListener{
		@Override
		public void onClick(View v) {
			openLight();
		}
	}
	private void openLight(){
		if(!isOpenLight)
		{
			CameraManager.get().openLight();
			isOpenLight=true;
		}
		else
		{
			CameraManager.get().closeLight();
			isOpenLight=false;
		}
	}
	//������еĶ�ά��
	private class QrClickListener implements OnClickListener{
		@Override
		public void onClick(View v) {
			// ���ֻ��е����
			Intent innerIntent = new Intent(Intent.ACTION_GET_CONTENT); // "android.intent.action.GET_CONTENT"
			innerIntent.setType("image/*");
			Intent wrapperIntent = Intent.createChooser(innerIntent, "ѡ���ά��ͼƬ");
			startActivityForResult(wrapperIntent, 1);
		}
	}
	final Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub

			switch (msg.what) {
				case 1:
					mProgress.dismiss();
					String resultString = msg.obj.toString();
					if (resultString.equals("")) {
						Toast.makeText(CaptureActivity.this, "ɨ��ʧ��!",
								Toast.LENGTH_SHORT).show();
					} else {
						// System.out.println("Result:"+resultString);
						Intent resultIntent = new Intent();
						Bundle bundle = new Bundle();
						bundle.putString("result", resultString);
						bundle.putParcelable("bitmap", scanBitmap);
						resultIntent.putExtras(bundle);
						CaptureActivity.this.setResult(RESULT_OK, resultIntent);
					}
					CaptureActivity.this.finish();
					break;

				case 2:
					mProgress.dismiss();
					Toast.makeText(CaptureActivity.this, "��������", Toast.LENGTH_LONG)
							.show();

					break;
				default:
					break;
			}

			super.handleMessage(msg);
		}

	};
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub

		if (resultCode == RESULT_OK) {
			switch (requestCode) {
				case 1:
					try {
						Uri uri = data.getData();
						if (!TextUtils.isEmpty(uri.getAuthority())) {
							Cursor cursor = getContentResolver().query(uri,
									new String[] { MediaStore.Images.Media.DATA },
									null, null, null);
							if (null == cursor) {
								Toast.makeText(this, "ͼƬû�ҵ�", Toast.LENGTH_SHORT)
										.show();
								return;
							}
							cursor.moveToFirst();
							photo_path = cursor.getString(cursor
									.getColumnIndex(MediaStore.Images.Media.DATA));
							cursor.close();
						} else {
							photo_path = data.getData().getPath();
						}
						mProgress = new ProgressDialog(CaptureActivity.this);
						mProgress.setMessage("����ɨ��...");
						mProgress.setCancelable(false);
						mProgress.show();

						new Thread(new Runnable() {
							@Override
							public void run() {
								Result result = scanningImage(photo_path);
								if (result != null) {
									Message m = mHandler.obtainMessage();
									m.what = 1;
									m.obj = result.getText();

									mHandler.sendMessage(m);
								} else {
									Message m = mHandler.obtainMessage();
									m.what = 2;
									m.obj = "Scan failed!";
									mHandler.sendMessage(m);
								}

							}
						}).start();
					} catch (Exception e) {
						Toast.makeText(CaptureActivity.this, "��������",
								Toast.LENGTH_LONG).show();
					}

					break;

				default:
					break;
			}
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * ɨ���ά��ͼƬ�ķ���
	 *
	 * Ŀǰʶ��Ȳ��ߣ��д��Ľ�
	 *
	 * @param path
	 * @return
	 */
	public Result scanningImage(String path) {
		if (TextUtils.isEmpty(path)) {
			return null;
		}
		Hashtable<DecodeHintType, String> hints = new Hashtable<DecodeHintType, String>();
		hints.put(DecodeHintType.CHARACTER_SET, "UTF8"); // ���ö�ά�����ݵı���

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true; // �Ȼ�ȡԭ��С
		scanBitmap = BitmapFactory.decodeFile(path, options);
		options.inJustDecodeBounds = false; // ��ȡ�µĴ�С
		int sampleSize = (int) (options.outHeight / (float) 100);
		if (sampleSize <= 0)
			sampleSize = 1;
		options.inSampleSize = sampleSize;
		scanBitmap = BitmapFactory.decodeFile(path, options);
		RGBLuminanceSource source = new RGBLuminanceSource(scanBitmap);
		BinaryBitmap bitmap1 = new BinaryBitmap(new HybridBinarizer(source));
		QRCodeReader reader = new QRCodeReader();
		try {
			return reader.decode(bitmap1, hints);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}