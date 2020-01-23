package com.jarlure.project.state;

import com.jarlure.project.bean.Bundle;
import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.simsilica.es.EntityData;
import com.simsilica.es.base.DefaultEntityData;
import com.simsilica.es.base.EntityIdGenerator;

import java.util.concurrent.atomic.AtomicLong;

public class DefaultEntityDataState extends BaseAppState implements EntityDataState {

    private DefaultEntityIdGenerator idGenerator;
    private EntityData ed;

    /**
     * 该AppState用于创建实体数据库
     */
    public DefaultEntityDataState() {
        idGenerator = new DefaultEntityIdGenerator();
        ed = new DefaultEntityData(idGenerator);
    }

    @Override
    public EntityData getEntityData() {
        return ed;
    }

    /**
     * 加载当前id值
     *
     * @param bundle 数据包
     */
    public void loadData(Bundle bundle) {
        Long id = bundle.get(idGenerator.getClass().getName());
        if (id != null) idGenerator.setCurrentValue(id);
    }

    /**
     * 保存当前id值
     *
     * @param bundle 数据包
     */
    public void saveData(Bundle bundle) {
        bundle.put(idGenerator.getClass().getName(), idGenerator.getCurrentValue());
    }

    @Override
    protected void initialize(Application app) {
    }

    @Override
    protected void cleanup(Application app) {
    }

    @Override
    protected void onEnable() {
    }

    @Override
    protected void onDisable() {
    }

    private static class DefaultEntityIdGenerator implements EntityIdGenerator {

        private AtomicLong entityId;

        private DefaultEntityIdGenerator() {
            this(0);
        }

        private DefaultEntityIdGenerator(long initialValue) {
            this.entityId = new AtomicLong(initialValue);
        }

        @Override
        public long nextEntityId() {
            return entityId.getAndIncrement();
        }

        private long getCurrentValue() {
            return entityId.get();
        }

        private void setCurrentValue(long initValue) {
            entityId.set(initValue);
        }

    }

}
