package com.videogo;

import android.text.TextUtils;

import com.hik.streamconvert.StreamConvert;
import com.hik.streamconvert.StreamConvertCB;
import com.videogo.util.LogUtil;

public class EZRecordDataImpl implements StreamConvertCB.OutputDataCB {
    private static final String TAG = "EZRecordDataImpl";
    private int mTransHandle = -1;
    private StreamConvert mStreamConvert = null;
    private String mRecordFilePath = "";
    private String mPassword = "";

    public EZRecordDataImpl(String recordPath, String password) {
        super();
        mStreamConvert = StreamConvert.getInstance();
        mRecordFilePath = recordPath;
        mPassword = password;
    }


//    @Override
//    public void onRecordData(byte[] pDataBuffer, int iDataSize) {
//        saveRecord(pDataBuffer, iDataSize);
//    }

    private void saveRecord(byte[] pDataBuffer, int iDataSize) {
        if (-1 == mTransHandle) {
            // 写文件
            if(!startSave(pDataBuffer, iDataSize)) {
//                sendMessage(RealPlayMsg.MSG_START_RECORD_FAIL, 0, 0);
            }
        } else {
            // 写文件
            if(!startSave(pDataBuffer, iDataSize)) {
//                sendMessage(RealPlayMsg.MSG_START_RECORD_FAIL, 0, 0);
            }
        }
    }
    private boolean startSave(byte[] hikHeader, int headerLen) {

        if (-1 == mTransHandle) {
            // RTP流要转封装
            mTransHandle = mStreamConvert.Create(hikHeader, headerLen, StreamConvert.TRANS_SYSTEM_MPEG4/*StreamConvert.TRANS_SYSTEM_MPEG2_PS*/);
            if (-1 == mTransHandle) {
                LogUtil.debugLog(TAG, "StreamConvert Create failed!");
                return false;
            }

            if (!TextUtils.isEmpty(mPassword)) {
                byte[] secretKey = mPassword.getBytes();
                if (!mStreamConvert.SetEncryptKey(mTransHandle, 1, secretKey,
                        secretKey.length * 8)) {
                    if (!mStreamConvert.Release(mTransHandle)) {
                        LogUtil.debugLog(TAG, "StreamConvert Release fail");
                    }
                    mTransHandle = -1;
                    return false;
                }
            }

            // 设置对讲音频解码参数
            /*HK_TRACK_AUDIO  stParam = new HK_TRACK_AUDIO();
            stParam.dwChannels      = 1;
            stParam.dwCodeType      = 0x7111;
            stParam.dwSamplesrate   = 8000;
            stParam.dwBitrate       = 64000;
            stParam.dwBitsPerSample = 16;
            stParam.dwDelayTime     = 10000; //毫秒 */            
            if (/*!mStreamConvert.SetAudioDecParam(mTransHandle, stParam) 
                 ||*/!mStreamConvert.Start(mTransHandle, null, mRecordFilePath)) {
                if (!mStreamConvert.Release(mTransHandle)) {
                    LogUtil.debugLog(TAG, "StreamConvert Release fail");
                }
                mTransHandle = -1;

                LogUtil.debugLog(TAG, "StreamConvert start failed");
                return false;
            }
            return true;
        } else {

            // if (mFileHasIData) {
            //
            // for (int i = 0; i < mDataBuffferList.size() && i < 1199; i++) {
            // InputData(mDataBuffferList.get(i), mDataSize[i]);
            //
            // LogUtil.infoLog(TAG, "Size" + mDataSize[i]);
            // mPreIDatePosition = -1;
            // }
            //
            // mDataBuffferList.clear();
            // mFileHasIData = false;
            //
            // InputData(hikHeader, headerLen);
            // } else {
            return inputData(hikHeader, headerLen);
            // }
        }
    }
    private boolean inputData(byte[] dataBuf, int dataLen) {
        if (mTransHandle >= 0) {

            if (!mStreamConvert.InputData(mTransHandle, StreamConvert.MULTI_DATA, dataBuf, dataLen)) {
                return false;
            }
        }
        return true;
    }

    private boolean stopSave() {
        if (mTransHandle >= 0) {
            if (!mStreamConvert.Stop(mTransHandle)) {
                LogUtil.debugLog(TAG, "StreamConvert Stop fail");
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }            
            if (!mStreamConvert.Release(mTransHandle)) {
                LogUtil.debugLog(TAG, "StreamConvert Release fail");
            }
            mTransHandle = -1;
        }
        return true;
    }

    @Override
    public void onOutputData(byte[] var1, int var2, int var3, byte[] var4) {

    }
}
