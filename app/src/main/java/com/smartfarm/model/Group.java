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
    //��ȡ������Ϣ
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
    //���������Ϣ
    public void saveGroupBean(Activity act, GroupBean bean) throws Exception {
        Config config = new Config(act);
        FileWriter writer = new FileWriter(act.getFileStreamPath(config.getUsername() + ":Group"));
        writer.write(new GsonBuilder().create().toJson(bean));
        writer.close();
    }
    //���ӷ���
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
        names.add("�ҵ��豸");
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
    //���з��鼰�������child
    //�ҵķ�����������豸
    //expendaList��ʼ��ʹ��
    public List<List<String>> getChildBeans() {
        //��ȡ����
        GroupBean beans = loadGroupBean(activity);
        LatestUseBean latestUseBean=loadLatestUse(activity);
        TopBean topBean=loadTop(activity);
        List<String> latest=latestUseBean.latestUse;
        List<List<String>> childBeans = new ArrayList<>();
        List<String> topList=topBean.top;
        // ���飺�ҵ��豸,���ҵķ����У��ҵķ�����������豸
        List<String> list = new ArrayList<>();
        for (int i = 0; i < equipmentBeans.size(); ++i) {
            list.add(equipmentBeans.get(i).code);
        }
        //���ö����豸
        for (int i=0;i<topList.size();i++)
        {
            list.remove(topList.get(i));
            latest.remove(topList.get(i));
        }
        //��ȥ����ù��ĺ��ö����豸
        for (int i=0;i<latest.size();i++) {
            list.remove(latest.get(i));
            //�豸�������ö�+����ù���+ʣ�µ�
            topList.add(latest.get(i));
        }
        //ʣ�µ��豸
        for (int i=0;i<list.size();i++){
            topList.add(list.get(i));
        }
        childBeans.add(topList);
        //��������
        for (int i = 0; i < beans.childToGroup.size(); ++i) {
            childBeans.add(beans.childToGroup.get(i));
        }
        return childBeans;
    }
    //����ù��ķ��ڷ����������
    public void addLatestUse(String childCode, String groupName){
        //�ڸ������������ù����豸
        GroupBean bean=loadGroupBean(activity);
        List<List<String>> child=bean.childToGroup;
        List<String> group=bean.groups;
        int key=group.indexOf(groupName);
        //����û�б����ҵ��豸��һ������
        //������������ù�����Ϣ����
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
        //����ù����豸��Ϣ
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
    //ɾ���豸��ʱ��ҲҪɾ����Ӧ��LatestUse
    public void deleteLatestUse(String childCode){
        //ɾ����Ӧ�ڸ������������ù��ĵ��豸
        GroupBean bean=loadGroupBean(activity);
        List<List<String>> child=bean.childToGroup;
        List<String> group=bean.groups;
        //����û�б����ҵ��豸��һ������
        //������������ù�����Ϣ����
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
        //ɾ����Ӧ����ù����豸
        LatestUseBean latestUseBean=loadLatestUse(activity);
        if (latestUseBean.latestUse.contains(childCode)){
            latestUseBean.latestUse.remove(childCode);
            try {
                saveLatestUse(activity,latestUseBean);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //ɾ����Ӧ�ö����豸
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

    //���ʹ�ù��豸����Ϣ
    //��ȡ���ʹ�ù��豸��Ϣ
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
    //�������ʹ�ù��豸��Ϣ
    private void saveLatestUse(Activity act, LatestUseBean bean) throws Exception {
        Config config = new Config(act);
        FileWriter writer = new FileWriter(act.getFileStreamPath(config.getUsername() + ":LatestUse"));
        writer.write(new GsonBuilder().create().toJson(bean));
        writer.close();
    }

    //��ȡ�豸���ö�����Ϣ
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
    //�����豸���ö�����Ϣ
    private void saveTop(Activity act, TopBean bean) throws Exception {
        Config config = new Config(act);
        FileWriter writer = new FileWriter(act.getFileStreamPath(config.getUsername() + ":Top"));
        writer.write(new GsonBuilder().create().toJson(bean));
        writer.close();
    }
    //��Ӷ���
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
    //ȡ������
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
