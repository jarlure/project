package com.jarlure.project.state;

import com.jarlure.project.bean.entitycomponent.Delay;
import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.simsilica.es.Entity;
import com.simsilica.es.EntityData;
import com.simsilica.es.EntitySet;

public class DelayState extends BaseAppState {

    private EntityData ed;
    private EntitySet delaySet;

    /**
     * 该AppState用于延迟调用回调函数。有些方法在执行前需要延迟一段时间等待其他AppState同步或是等待用户响应。例如该方法
     * 在执行中会从EntityData中获取数据A，而数据A是由另一个AppState维护的。那么该方法就需要在另一个AppState更新后执行；
     * 另一个常见的例子是关闭程序时会弹出是否保存数据的提示并倒计时。若用户在10秒内不响应则默认直接关闭不保存数据。
     */
    public DelayState() {

    }

    @Override
    protected void initialize(Application app) {
        ed = getState(EntityDataState.class).getEntityData();
        delaySet = ed.getEntities(Delay.class);
    }

    @Override
    protected void cleanup(Application app) {
        delaySet.release();
        delaySet = null;
        ed = null;
    }

    @Override
    protected void onEnable() {
    }

    @Override
    protected void onDisable() {
    }

    @Override
    public void update(float tpf) {
        delaySet.applyChanges();
        if (delaySet.isEmpty()) return;
        for (Entity entity:delaySet){
            Delay delay = entity.get(Delay.class);
            if (delay.getTime() < 0) {
                delay.getCallback().apply();
                ed.removeEntity(entity.getId());
            } else delay.setTime(delay.getTime() - tpf);
        }
    }

}
