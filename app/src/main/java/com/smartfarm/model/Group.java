package com.smartfarm.model;

import android.app.Activity;

import com.google.gson.GsonBuilder;
import com.smartfarm.bean.EquipmentBean;
import com.smartfarm.bean.GroupBean;
import com.smartfarm.bean.LatestUseBean;
import com.smartfarm.bean.TopBean;
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
        TopBean topBean=loadTop(activity);
        List<String> latest=latestUseBean.latestUse;
        List<List<String>> childBeans = new ArrayList<>();
        List<String> topList=topBean.top;
        // 分组：我的设备,在我的分组中，我的分组包括所有设备
        List<String> list = new ArrayList<>();
        for (int i = 0; i < equipmentBeans.size(); ++i) {
            list.add(equipmentBeans.get(i).code);
        }
        //有置顶的设备
        for (int i=0;i<topList.size();i++)
        {
            list.remove(topList.get(i));
            latest.remove(topList.get(i));
        }
        //除去最近用过的和置顶的设备
        for (int i=0;i<latest.size();i++) {
            list.remove(latest.get(i));
            //设备的排序，置顶+最近用过的+剩下的
            topList.add(latest.get(i));
        }
        //剩下的设备
        for (int i=0;i<list.size();i++){
            topList.add(list.get(i));
        }
        childBeans.add(topList);
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
        //删除相应在个分组中最新用过的的设备
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
        //删除相应最近用过的设备
        LatestUseBean latestUseBean=loadLatestUse(activity);
        if (latestUseBean.latestUse.contains(childCode)){
            latestUseBean.latestUse.remove(childCode);
            try {
                saveLatestUse(activity,latestUseBean);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //删除相应置顶的设备
        TopBean topBean=loadTop(activity);
        if (topBean.top.contains(childCode)){
            topBean.top.remove(childCode);
            try {
                saveTop(activity,topBean);
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

    //读取设备的置顶的信息
    private TopBean loadTop(Activity act) {
        Config config = new Config(act);
        TopBean bean;
        try {
            //Returns the absolute path on the filesystem where a file created with openFileOutput(String, int) is stored.
            FileReader reader = new FileReader(act.getFileStreamPath(config.getUsername() + ":Top"));
            bean = new GsonBuilder().create().fromJson(reader, TopBean.class);
        } catch (Exception e) {
            bean = new TopBean();
        }
        return bean;
    }
    //保存设备的置顶的信息
    private void saveTop(Activity act, TopBean bean) throws Exception {
        Config config = new Config(act);
        FileWriter writer = new FileWriter(act.getFileStreamPath(config.getUsername() + ":Top"));
        writer.write(new GsonBuilder().create().toJson(bean));
        writer.close();
    }
    //添加顶置
    public void addTop(String equipmentCode){
        TopBean topBean=loadTop(activity);
        if(topBean.top.contains(equipmentCode))
        {
            topBean.top.remove(equipmentCode);
        }
        topBean.top.add(0,equipmentCode);
        try {
            saveTop(activity,topBean);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //取消顶置
    public void cancelTop(String equipmentCode){
        TopBean topBean=loadTop(activity);
        topBean.top.remove(equipmentCode);
        try {
            saveTop(activity,topBean);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public TopBean getTopBean(){
        return loadTop(activity);
    }
}
