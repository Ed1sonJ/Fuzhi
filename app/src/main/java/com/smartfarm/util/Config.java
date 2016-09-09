package com.smartfarm.util;

import android.content.Context;
import android.content.SharedPreferences;

//	保存配置信息的类
public class Config {
    private static final String file_name = "smartfarm";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor preferencesEditor;

    public Config(Context context) {
        sharedPreferences = context.getSharedPreferences(file_name,
                Context.MODE_PRIVATE);
        preferencesEditor = sharedPreferences.edit();
    }

    public void setToken(String token) {
        preferencesEditor.putString("token", token);
        preferencesEditor.commit();
    }

    public String getToken() {
        return sharedPreferences.getString("token", "");
    }

    public void setUsername(String username) {
        preferencesEditor.putString("username", username);
        preferencesEditor.commit();
    }

    public void setLastUsername(String username) {
        preferencesEditor.putString("last_username", username);
        preferencesEditor.commit();
    }

    public String getLastUsername() {
        return sharedPreferences.getString("last_username", "");
    }

    public String getUsername() {
        return sharedPreferences.getString("username", "");
    }

    public void setPassword(String password) {
        preferencesEditor.putString("password", password);
        preferencesEditor.commit();
    }

    public String getPassword() {
        return sharedPreferences.getString("password", "");
    }

    public void setPicture(String picture) {
        preferencesEditor.putString("picture", picture);
        preferencesEditor.commit();
    }

    public String getPicture() {
        return sharedPreferences.getString("picture", "");
    }

    public String getLastEquipmentCode() {
        return sharedPreferences.getString("lastEquipmentCode", "");
    }

    public void setLastEquipmentCode(String code) {
        preferencesEditor.putString("lastEquipmentCode", code);
        preferencesEditor.commit();
    }

    public void clear() {
        preferencesEditor.putString("username", "");
        preferencesEditor.putString("password", "");
        preferencesEditor.commit();
    }

    public boolean hasLogin() {
        return !getUsername().equals("");
    }

    public void setIsFirstOpen(String isFirstOpen, Boolean isFirst) {
        preferencesEditor.putBoolean(isFirstOpen, isFirst);
        preferencesEditor.commit();
    }

    public Boolean getIsFirstOpen(String isFirstOpen) {
        return sharedPreferences.getBoolean(isFirstOpen, true);
    }

//	public void setEquipmentName(String nameAndCode,String name){
//		preferencesEditor.putString(nameAndCode, name);
//		preferencesEditor.commit();
//	}
//	
//	public String getEquipmentName(String nameAndCode){
//		return sharedPreferences.getString(nameAndCode, "");	
//	}
//	
//	public void removeEquipmentName(String nameAndCode){
//		preferencesEditor.remove(nameAndCode);
//		preferencesEditor.commit();
//	}
}
