package com.jarlure.project.screen.screenstate.operation;

public interface Operation {

    /**
     * 初始化操作
     */
    void initialize();

    /**
     * 启用操作
     */
    void onEnable();

    /**
     * 更新操作
     *
     * @param tpf 秒每帧
     */
    void update(float tpf);

    /**
     * 禁用操作
     */
    void onDisable();

    /**
     * 清除操作
     */
    void cleanup();

}