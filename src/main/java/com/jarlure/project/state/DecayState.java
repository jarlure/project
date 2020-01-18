package com.jarlure.project.state;

import com.jarlure.project.bean.entitycomponent.Decay;
import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.simsilica.es.Entity;
import com.simsilica.es.EntityData;
import com.simsilica.es.EntitySet;

public final class DecayState extends BaseAppState {

    private EntityData ed;
    private EntitySet decaySet;
    private float threshold;
    private float t;

    /**
     * 该AppState用于回收死亡的实体。如果有实体添加了Decay组件，意味着该实体已死亡。此时该AppState会在更新时刷新Decay中的死
     * 亡倒计时。当倒计时<0时，该AppState将移除该实体。
     */
    public DecayState() {

    }

    @Override
    protected void initialize(Application app) {
        ed = getState(EntityDataState.class).getEntityData();
        decaySet = ed.getEntities(Decay.class);
    }

    @Override
    protected void cleanup(Application app) {
        decaySet.release();
        decaySet = null;
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
        if (decaySet.applyChanges()) updateThreshold();
        if (decaySet.isEmpty()) return;
        t += tpf;
        if (t < threshold) return;
        for (Entity entity:decaySet){
            Decay decay = entity.get(Decay.class);
            if (decay.getTime() < 0) ed.removeEntity(entity.getId());
            else decay.setTime(decay.getTime() - t);
        }
        t = 0;
    }

    /**
     * 为了应对有可能出现大量实体同时死亡导致出现更新死亡倒计时任务繁重的情况，设计了阶梯式的更新死亡倒计时办法：当死亡
     * 数量在1K以下时不进行管制；当死亡数量在1K-10K时每0.1秒更新一次死亡倒计时；当死亡数量在10K以上时每0.5秒更新一次死亡
     * 倒计时。
     */
    private void updateThreshold() {
        int size = decaySet.size();
        if (size < 1000) threshold = 0;
        else if (size < 10000) threshold = 0.1f;
        else threshold = 0.5f;
    }

}

