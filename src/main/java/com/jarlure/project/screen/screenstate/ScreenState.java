package com.jarlure.project.screen.screenstate;

import com.jarlure.project.bean.Bundle;
import com.jarlure.project.layout.Layout;
import com.jarlure.project.screen.Screen;
import com.jarlure.project.screen.screenstate.operation.Operation;
import com.jme3.app.Application;

public interface ScreenState {

    /**
     * 判断是否已初始化ScreenState
     *
     * @return true如果已初始化；false如果未初始化
     */
    boolean isInitialized();

    /**
     * 初始化ScreenState
     *
     * @param app    系统
     * @param screen 界面状态
     */
    void initialize(Application app, Screen screen);

    /**
     * 判断当前ScreenState是否已启用
     *
     * @return true如果已启用；false如果未启用
     */
    boolean isEnabled();

    /**
     * 设置当前ScreenState的启用状态
     *
     * @param enabled true如果启用；false如果禁用
     */
    void setEnabled(boolean enabled);

    /**
     * 设置布局。通常在这个方法里设置组件成员变量的引用
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
     * 更新ScreenState
     *
     * @param tpf 秒每帧
     */
    void update(float tpf) ;

    /**
     * 保存数据
     *
     * @param bundle 数据包
     */
    void saveData(Bundle bundle) ;

    /**
     * 清除ScreenState
     */
    void cleanup();

    /**
     * 获取关联的UI场景
     *
     * @return UI场景
     */
    Screen getScreen();

    /**
     * 获取关联的操作
     *
     * @param operationClass 操作类
     * @param <T>
     * @return 关联的操作
     */
    <T extends Operation> T getOperation(Class<T> operationClass);

}
