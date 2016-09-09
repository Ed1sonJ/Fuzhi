package com.smartfarm.model;

import android.app.Activity;

import com.google.gson.GsonBuilder;
import com.smartfarm.bean.EquipmentBean;
import com.smartfarm.bean.GroupBean;
import com.smartfarm.bean.LatestUseBean;
import com.smartfarm.util.Config;

import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by june_qijun on 16/1/5.
 */
public class Group {
    protected Activity activity;
    protected List<EquipmentBean> equipmentBeans;

    public Group(Activity act, List<EquipmentBean> beans) {
        activity = act;
        equipmentBeans = beans;
        loadGroupBean(act);
    }
    //读取分组信息
    public GroupBean loadGroupBean(Activity act) {
        Config config = new Config(act);
        GroupBean bean;
        try {
            //Returns the absolute path on the filesystem where a file created with openFileOutput(String, int) is stored.
            FileReader reader = new FileReader(act.getFileStreamPath(config.getUsername() + ":Group"));
            bean = new GsonBuilder().create().fromJson(reader, GroupBean.class);
        } catch (Exception e) {
            bean = new GroupBean();
        }
        return bean;
    }
    //保存分组信息
    public void saveGroupBean(Activity act, GroupBean bean) throws Exception {
        Config config = new Config(act);
        FileWriter writer = new FileWriter(act.getFileStreamPath(config.getUsername() + ":Group"));
        writer.write(new GsonBuilder().create().toJson(bean));
        writer.close();
    }
    //增加分组
    public void addGroup(String groupName) throws Exception {
        GroupBean bean = loadGroupBean(activity);
        int findIndex = bean.groups.indexOf(groupName);
        if (findIndex == -1) {
            bean.groups.add(groupName);
            bean.childToGroup.add(new ArrayList<String>());
            saveGroupBean(activity, bean);
        } else {
            throw new Exception();
        }
    }
    public void deleteGroup(String groupName) throws Exception {
        GroupBean bean = loadGroupBean(activity);
        int findIndex = bean.groups.indexOf(groupName);
        if (findIndex != -1) {
            bean.groups.remove(findIndex);
            bean.childToGroup.remove(findIndex);
            saveGroupBean(activity, bean);
        } else {
            throw new Exception();
        }
    }

    public void renameGroup(String src, String target) throws Exception {
        GroupBean bean = loadGroupBean(activity);
        int findIndex = bean.groups.indexOf(src);
        if (findIndex != -1) {
            bean.groups.set(findIndex, target);
            saveGroupBean(activity, bean);
        } else {
            throw new Exception();
        }
    }

    public List<String> getGroupNames() {
        List<String> names = new ArrayList<>();
        GroupBean beans = loadGroupBean(activity);
        names.add("我的设备");
        for (int i = 0; i < beans.groups.size(); ++i) {
            names.add(beans.groups.get(i));
        }
        return names;
    }

    public void addChildToGroup(String childCode, String groupName) throws Exception {
        if (isValidEquipmentCode(childCode)) {
            if (isValidGroupName(groupName)) {
                if (!isChildInGroup(childCode, groupName)) {
                    GroupBean beans = loadGroupBean(activity);
                    int findIndex = beans.groups.indexOf(groupName);
                    beans.childToGroup.get(findIndex).add(childCode);
                    saveGroupBean(activity, beans);

                }
            }
        }
    }
    //所有分组及其包含的child
    //我的分组包括所有设备
    //expendaList初始化使用
    public List<List<String>> getChildBeans() {
        //读取分组
        GroupBean beans = loadGroupBean(activity);
        LatestUseBean latestUseBean=loadLatestUse(activity);
        List<String> latest=latestUseBean.latestUse;
        List<List<String>> childBeans = new ArrayList<>();
        // 分组：我的设备,在我的分组中，我的分组包括所有设备
        List<String> list = new ArrayList<>();
        for (int i = 0; i < equipmentBeans.size(); ++i) {
            list.add(equipmentBeans.get(i).code);
        }
        //最近使用的为0或最近全部都是用过
        if(latest.size()==list.size())
        {
            childBeans.add(latest);
        }
        else
        {
            //除去最近用过的设备
            for (int i=0;i<latest.size();i++) {
                list.remove(latest.get(i));
            }
            //最近用过的设备和没有用过的设备
            for (int i=0;i<list.size();i++){
                latest.add(list.get(i));
            }
            childBeans.add(latest);
        }
        //其他分组
        for (int i = 0; i < beans.childToGroup.size(); ++i) {
            childBeans.add(beans.childToGroup.get(i));
        }
        return childBeans;
    }
    //最近用过的放在分组的最上面
    public void addLatestUse(String childCode, String groupName){
        //在个分组中最新用过的设备
        GroupBean bean=loadGroupBean(activity);
        List<List<String>> child=bean.childToGroup;
        List<String> group=bean.groups;
        int key=group.indexOf(groupName);
        //本地没有保存我的设备这一个分组
        //其他分组最近用过的消息设置
        if(key>=0)
        {
            child.get(key).remove(childCode);
            child.get(key).add(0,childCode);
            bean.childToGroup=child;
            try {
                saveGroupBean(activity,bean);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //最近用过的设备信息
        LatestUseBean latestUseBean=loadLatestUse(activity);
        if (latestUseBean.latestUse.contains(childCode)){
            latestUseBean.latestUse.remove(childCode);
        }
        latestUseBean.latestUse.add(0,childCode);
        try {
            saveLatestUse(activity,latestUseBean);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //删除设备的时候也要删除相应的LatestUse
    public void deleteLatestUse(String childCode){
        //在个分组中最新用过的设备，删除刚刚删除的设备
        GroupBean bean=loadGroupBean(activity);
        List<List<String>> child=bean.childToGroup;
        List<String> group=bean.groups;
        //本地没有保存我的设备这一个分组
        //其他分组最近用过的消息设置
        for (int i=0;i<group.size();i++)
        {
            child.get(i).remove(childCode);
        }
        bean.childToGroup=child;
        try {
            saveGroupBean(activity,bean);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //最近用过的设备信息,删除刚刚删除的设备
        LatestUseBean latestUseBean=loadLatestUse(activity);
        if (latestUseBean.latestUse.contains(childCode)){
            latestUseBean.latestUse.remove(childCode);
            try {
                saveLatestUse(activity,latestUseBean);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
    protected boolean isValidEquipmentCode(String code) {
        int findIndex = -1;
        for (int i = 0; i < equipmentBeans.size(); ++i) {
            if (equipmentBeans.get(i).code.equals(code)) {
                findIndex = i;
                break;
            }
        }
        return (findIndex != -1);
    }

    protected boolean isValidGroupName(String name) {
        return (getGroupNames().indexOf(name) != -1);
    }

    protected boolean isChildInGroup(String child, String group) {
        int groupIndex = getGroupNames().indexOf(group);
        if (groupIndex == -1) {
            return false;
        }
        List<String> childList = getChildBeans().get(groupIndex);
        return (childList.indexOf(child) != -1);
    }

    //最近使用过设备的信息
    //读取最近使用过设备信息
    private LatestUseBean loadLatestUse(Activity act) {
        Config config = new Config(act);
        LatestUseBean bean;
        try {
            //Returns the absolute path on the filesystem where a file created with openFileOutput(String, int) is stored.
            FileReader reader = new FileReader(act.getFileStreamPath(config.getUsername() + ":LatestUse"));
            bean = new GsonBuilder().create().fromJson(reader, LatestUseBean.class);
        } catch (Exception e) {
            bean = new LatestUseBean();
        }
        return bean;
    }
    //保存最近使用过设备信息
    private void saveLatestUse(Activity act, LatestUseBean bean) throws Exception {
        Config config = new Config(act);
        FileWriter writer = new FileWriter(act.getFileStreamPath(config.getUsername() + ":LatestUse"));
        writer.write(new GsonBuilder().create().toJson(bean));
        writer.close();
    }
}
