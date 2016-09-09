package com.videogo.ui.util;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.text.TextUtils;

import com.videogo.exception.InnerException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;

//import com.videogo.exception.InnerException;
//import com.videogo.util.EZGenerateFilePath;
//import com.videogo.util.WaterMarkUtil;

public class EZUtils {
    public static String generateCaptureFilePath(String rootPath, String cameraId, String deviceSerial) {
        // 生成文件路径,文件格式为mnt/sdcard/VideoGo/20120901/20120901141138540_test
        if (TextUtils.isEmpty(rootPath)) {
            return null;
        }

        // 生成文件路径,文件格式为mnt/sdcard/VideoGo/20120901/20120901141138540_test
        String filePath = null;
        try {
            filePath = EZGenerateFilePath.generateFilePath(rootPath, cameraId,
                deviceSerial);
            return filePath;
        } catch (IOException e1) {
            e1.printStackTrace();
            return null;
        }
    }

    public static String generateThumbnailFilePath(String filePath) {
        // 生成缩略图文件路径,文件格式为mnt/sdcard/VideoGo/20120901/thumbnail/20120901141138540_test
        String thumbnailPath = EZGenerateFilePath.generateThumbnailPath(filePath);
        return thumbnailPath;
    }

    public static void saveCapturePictrue(String filePath, String thumbnailFilePath, Bitmap bitmap) throws InnerException {
        FileOutputStream out = null;
        FileOutputStream thumbnailOut = null;
        try {
            // 保存原图
            
            if (!TextUtils.isEmpty(filePath)) {
                File file = new File(filePath);
                out = new FileOutputStream(file);
                bitmap.compress(CompressFormat.JPEG, 100, out);
                //out.write(tempBuf, 0, size);
                out.flush();
                out.close();
                out = null;
            }

            // 保存缩略图
            if (!TextUtils.isEmpty(thumbnailFilePath)) {
                File file = new File(thumbnailFilePath);
                thumbnailOut = new FileOutputStream(file);
                boolean decodeRet = decodeThumbnail(bitmap, bitmap.getWidth(), bitmap.getHeight(), thumbnailOut);
                thumbnailOut.flush();
                thumbnailOut.close();
                thumbnailOut = null;
                if (!decodeRet) {
                    throw new InnerException("decode thumbnail picture fail");
                }
            }

        } catch (FileNotFoundException e) {
            throw new InnerException(e.getLocalizedMessage());
        } catch (IOException e) {
            throw new InnerException(e.getLocalizedMessage());
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if (thumbnailOut != null) {
                try {
                    thumbnailOut.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 为缩略图编码
     *
     *            - 原来JPEG图片缓冲
     * @param width
     *            - 原始图片宽
     * @param height
     *            - 原始图片高
     * @param fos
     *            - 文件流
     * @return true - 成功; false - 失败
     * @since V1.0
     */
    private static boolean decodeThumbnail(Bitmap bitmap, int width, int height, FileOutputStream fos) {
        if (bitmap == null || width < 0 || height < 0 || fos == null) {
            return false;
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        int scale = 0;
        int width_tmp = width;
        int height_tmp = height;
        int MIN_WIDTH = 120;
        int MIN_HEIGHT = 90;
        while (true) {
            if (width_tmp <= MIN_WIDTH && height_tmp <= MIN_HEIGHT) {
                break;
            }

            width_tmp = width_tmp / 2;
            height_tmp = height_tmp / 2;
            scale += 2;
        }

        options.inSampleSize = scale;
        
        Bitmap newBmp = Bitmap.createBitmap(width_tmp, height_tmp, Config.ARGB_8888);
        
        Canvas cv = new Canvas(newBmp);

        // 在 0, 0坐标开始画入src
//        cv.drawBitmap(bitmap, 0, 0, null);
        Rect src = new Rect(0,0,bitmap.getWidth(), bitmap.getHeight());
        Rect dst = new Rect(0,0, newBmp.getWidth(), newBmp.getHeight());
        cv.drawBitmap(bitmap, src, dst, null);

        // 保存
        cv.save(Canvas.ALL_SAVE_FLAG);
        cv.restore();

        newBmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);

        return true;
    }

    public static Object getPrivateMethodInvoke(Object instance, /*Class destClass,*/ String methodName,
                                                Class<?>[] parameterClass, Object... args) throws Exception {
        Class<?>[] parameterTypes = null;
        if(args != null) {
            parameterTypes = parameterClass;
        }
        Method method = instance.getClass().getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method.invoke(instance, args);
    }
}
