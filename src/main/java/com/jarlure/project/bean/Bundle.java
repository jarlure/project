package com.jarlure.project.bean;

import com.jarlure.project.util.SavableHelper;
import com.jme3.export.*;

import java.util.HashMap;

/**
 * 数据包。用于保存当前系统数据
 */
public final class Bundle implements Savable {

    private HashMap<String, Object> data;
    private boolean dataChanged;

    public boolean exist(String name) {
        if (data == null || data.isEmpty()) return false;
        return data.containsKey(name);
    }

    /**
     * 判断数据是否发生了更新。
     * @return  如果在此方法被调用之前调用过put()方法，则返回true；否则返回false
     */
    public boolean dataChanged(){
        if (dataChanged){
            dataChanged=false;
            return true;
        }
        return false;
    }

    public void put(String name, Object data) {
        if (this.data == null) this.data = new HashMap<>();
        this.data.put(name, data);
        dataChanged=true;
    }

    public <T> T get(String name) {
        if (this.data == null) return null;
        return (T) this.data.get(name);
    }

    @Override
    public void read(JmeImporter im) {
        InputCapsule capsule = im.getCapsule(this);
        SavableHelper.read(this, capsule, null);
    }

    @Override
    public void write(JmeExporter ex) {
        if (this.data == null || this.data.isEmpty()) return;
        OutputCapsule capsule = ex.getCapsule(this);
        SavableHelper.write(this, capsule, null);
    }

}
