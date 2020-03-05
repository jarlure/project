package com.jarlure.project.state;

import com.jarlure.project.bean.entitycomponent.Task;
import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.simsilica.es.Entity;
import com.simsilica.es.EntityData;
import com.simsilica.es.EntitySet;

public class TaskState extends BaseAppState {

    private EntityData ed;
    private EntitySet taskSet;

    @Override
    protected void initialize(Application app) {
        ed = getState(EntityDataState.class).getEntityData();
        taskSet = ed.getEntities(Task.class);
    }

    @Override
    protected void cleanup(Application app) {
        taskSet.release();
        ed=null;
    }

    @Override
    protected void onEnable() {

    }

    @Override
    protected void onDisable() {

    }

    @Override
    public void update(float tpf) {
        taskSet.applyChanges();
        if (taskSet.isEmpty())return;
        for (Entity entity:taskSet){
            new Thread(entity.get(Task.class).getTask()).start();
            ed.removeEntity(entity.getId());
        }
    }

}
