package com.jarlure.project.state;

import com.jme3.app.state.AppState;
import com.simsilica.es.EntityData;

public interface EntityDataState extends AppState {

    /**
     * 获取实体数据库
     *
     * @return 实体数据库
     */
    EntityData getEntityData();

}