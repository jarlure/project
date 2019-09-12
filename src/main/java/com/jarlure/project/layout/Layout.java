package com.jarlure.project.layout;

import com.jarlure.ui.component.UIComponent;
import com.jarlure.ui.component.UINode;

public interface Layout {

    /**
     * 获取布局宽度
     *
     * @return 布局宽度
     */
    float getLayoutWidth();

    /**
     * 获取布局高度
     *
     * @return 布局高度
     */
    float getLayoutHeight();

    /**
     * 获取布局根结点
     *
     * @return 布局根结点
     */
    UINode getLayoutNode();

    /**
     * 获取组件
     *
     * @param name 组件名称
     * @return 组件
     */
    UIComponent getComponent(String name);

}
