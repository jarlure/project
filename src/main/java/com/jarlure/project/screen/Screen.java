package com.jarlure.project.screen;

import com.jme3.app.state.AppState;

public interface Screen extends AppState {

    /**
     * 判断该UI场景是否可见
     * @return true如果UI场景可见；false如果UI场景不可见
     */
    boolean isVisible();

    /**
     * 设置UI场景是否可见
     * @param visible   UI场景是否可见
     */
    void setVisible(boolean visible);

    /**
     * 加载业务逻辑相关数据。
     */
    void loadData();

    /**
     * 存储业务逻辑相关数据。
     */
    void saveData();

    /**
     * 切换至另一UI场景
     * @param screenClass   另一UI场景的类
     */
    void skipTo(Class<Screen> screenClass);

    /**
     * 通过类获取对应类实例。例如获取RecordState实例
     * @param type  类
     * @param <T>
     * @return  类实例
     */
    <T extends Object> T getState(Class<T> type);

}


