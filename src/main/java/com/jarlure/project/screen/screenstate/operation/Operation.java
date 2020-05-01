package com.jarlure.project.screen.screenstate.operation;

import com.jarlure.project.bean.Bundle;
import com.jarlure.project.layout.Layout;

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
     * 设置布局
     *
     * @param layout 界面布局
     */
    void setLayout(Layout layout);

    /**
     * 加载数据
     *
     * @param bundle 数据包
     */
    void loadData(Bundle bundle) ;

    /**
     * 更新操作
     *
     * @param tpf 秒每帧
     */
    void update(float tpf);

    /**
     * 保存数据
     *
     * @param bundle 数据包
     */
    void saveData(Bundle bundle) ;

    /**
     * 禁用操作
     */
    void onDisable();

    /**
     * 清除操作
     */
    void cleanup();

}