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
            //从流中读取至多buff.length 个字节，并将其存储到 b 中（从索引 0 开始）。 如果到达 EOF，则返回 -1。
            while ((hasRead = fis.read(buff)) > 0) {
                sb.append(new String(buff, 0, hasRead));
            }
            fis.close();
            String equipmentNameFromFile = sb.toString();
            //因为写的时候用 ps.println(equipmentName);
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
            //获取本地资源的图片，Fresco 不支持 相对路径的URI. 所有的URI都必须是绝对路径，并且带上该URI的scheme。
            File equimentImageFile = getEquipmentImageFile(activity, code);
            Uri uri = Uri.parse("file://com.smartfarm.activity/" + equimentImageFile);
            if (!equimentImageFile.exists()) {
                uri = Uri.parse("res://com.smartfarm.activity/" + R.drawable.overview_image);
            }
            int width = targetWidth, height = targetWidth;
            //逐渐加载的图片，即，从模糊逐渐清晰，ImageRequest，PipelineDraweeController。需要图片本身也支持这种方式
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
