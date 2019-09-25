package com.jarlure.project.factory;

import com.jarlure.project.bean.LayerImageData;
import com.jarlure.ui.component.UIComponent;

public interface UIFactory {

    /**
     * 默认生产方式创建组件
     *
     * @return 组件
     */
    UIComponent create();

    /**
     * 无参数生产方式创建组件
     *
     * @param type 组件类型
     * @param name 组件名称
     * @return 组件
     */
    UIComponent create(String type, String name);

    /**
     * 图层作为参数的生产方式创建组件
     *
     * @param type 组件类型
     * @param name 组件名称
     * @param data 图层数据
     * @return 组件
     */
    UIComponent create(String type, String name, LayerImageData... data);

    /**
     * 组件作为参数的生产方式创建组件
     *
     * @param type     组件类型
     * @param name     组件名称
     * @param children 子组件
     * @return 组件
     */
    UIComponent create(String type, String name, UIComponent... children);

}