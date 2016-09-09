package com.smartfarm.model;

import android.app.Activity;
import android.net.Uri;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.smartfarm.activity.R;
import com.smartfarm.util.Config;
import com.smartfarm.util.FilePathManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;

/**
 * Created by june_qijun on 16/1/4.
 */
public class Equipment {
    public static String getEquipmentName(Activity activity, String equipmentCode) {
        try {
            Config config = new Config(activity);
            FileInputStream fis = activity.openFileInput(config.getUsername()
                    + equipmentCode);
            byte[] buff = new byte[128];
            int hasRead;
            StringBuilder sb = new StringBuilder("");
            //�����ж�ȡ����buff.length ���ֽڣ�������洢�� b �У������� 0 ��ʼ���� ������� EOF���򷵻� -1��
            while ((hasRead = fis.read(buff)) > 0) {
                sb.append(new String(buff, 0, hasRead));
            }
            fis.close();
            String equipmentNameFromFile = sb.toString();
            //��Ϊд��ʱ���� ps.println(equipmentName);
            equipmentNameFromFile = equipmentNameFromFile.replaceAll("\n", "");
            return equipmentNameFromFile;
        } catch (Exception e) {
            e.printStackTrace();
            return equipmentCode;
        }
    }

    public static boolean setEquipmentName(Activity activity, String equipmentCode, String equipmentName) {
        try {
            Config config = new Config(activity);
            FileOutputStream fos = activity.openFileOutput(config.getUsername() +
                    equipmentCode, activity.MODE_PRIVATE);
            PrintStream ps = new PrintStream(fos);
            ps.println(equipmentName);
            ps.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void loadEquipmentImage(Activity activity,String code,SimpleDraweeView userPicture,int targetWidth) {
        try {
            //��ȡ������Դ��ͼƬ��Fresco ��֧�� ���·����URI. ���е�URI�������Ǿ���·�������Ҵ��ϸ�URI��scheme��
            File equimentImageFile = getEquipmentImageFile(activity, code);
            Uri uri = Uri.parse("file://com.smartfarm.activity/" + equimentImageFile);
            if (!equimentImageFile.exists()) {
                uri = Uri.parse("res://com.smartfarm.activity/" + R.drawable.overview_image);
            }
            int width = targetWidth, height = targetWidth;
            //�𽥼��ص�ͼƬ��������ģ����������ImageRequest��PipelineDraweeController����ҪͼƬ����Ҳ֧�����ַ�ʽ
            ImageRequest request = ImageRequestBuilder.newBuilderWithSource(uri)
                    .setAutoRotateEnabled(true)
                    .setResizeOptions(new ResizeOptions(width, height))
                    .build();
            PipelineDraweeController controller = (PipelineDraweeController)Fresco.newDraweeControllerBuilder()
                    .setOldController(userPicture.getController())
                    .setImageRequest(request)
                    .build();
            userPicture.setController(controller);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected static String getEquipmentImageName(Activity activity,String code) {
        Config config = new Config(activity);
        return config.getUsername()
                + code + ".jpeg";
    }

    protected static File getEquipmentImageFile(Activity activity,String code) throws Exception {
        FilePathManager fpm = FilePathManager.getInstance(activity);
        return fpm.getFile(getEquipmentImageName(activity,code));
    }
}
