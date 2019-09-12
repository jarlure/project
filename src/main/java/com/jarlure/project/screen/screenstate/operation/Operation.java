package com.jarlure.project.screen.screenstate.operation;

public interface Operation {

    /**
     * 初始化操作
     */
    default void initialize() {
    }

    /**
     * 启用操作
     */
    default void onEnable() {
    }

    /**
     * 更新操作
     * @param tpf 秒每帧
     */
    default void update(float tpf){
    }

    /**
     * 禁用操作
     */
    default void onDisable() {
    }

    /**
     * 清除操作
     */
    default void cleanup() {
    }

}