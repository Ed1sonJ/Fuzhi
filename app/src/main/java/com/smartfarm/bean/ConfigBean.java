package com.smartfarm.bean;

/**
 * Created by shawn on 2016/1/12.
 */
public class ConfigBean {
    public String hostCode;
    public String indicatorNum;
    public String productCode;
    public String protocolKey;

    public String getHostCode(){
        return hostCode;
    }
    public void setHostCode(String hostCode){
        this.hostCode = hostCode;
    }

    public String getIndicatorNum(){
        return  indicatorNum;
    }
    public void setIndicatorNum(String indicatorNum){
        this.indicatorNum = indicatorNum;
    }

    public String getProductCode(){
        return productCode;
    }
    public void setProductCode(String productCode){
        this.productCode = productCode;
    }

    public String getProtocolKey(){
        return protocolKey;
    }
    public void setProtocolKey(String protocolKey){
        this.protocolKey = protocolKey;
    }

    public ConfigBean(String hostCode,String indicatorNum,String productCode,String protocolKey){
        this.hostCode = hostCode;
        this.indicatorNum = indicatorNum;
        this.productCode = productCode;
        this.protocolKey = protocolKey;
    }

    public ConfigBean(){

    }

}
