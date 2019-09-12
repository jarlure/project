package com.jarlure.project.factory;

import com.jarlure.project.bean.LayerImageData;
import com.jarlure.ui.component.UIComponent;

public interface UIFactory {

    /**
     * 默认生产方式创建组件
     * @return  组件
     */
    default UIComponent create(){
        throw new UnsupportedOperationException("该工厂不支持默认生产方式");
    }

    /**
     * 无参数生产方式创建组件
     * @param type  组件类型
     * @param name  组件名称
     * @return  组件
     */
    default UIComponent create(String type, String name){
        throw new UnsupportedOperationException("该工厂不支持无参数生产方式");
    }

    /**
     * 图层作为参数的生产方式创建组件
     * @param type  组件类型
     * @param name  组件名称
     * @param data  图层数据
     * @return  组件
     */
    default UIComponent create(String type, String name, LayerImageData... data){
        throw new UnsupportedOperationException("该工厂不支持图层作为参数的生产方式");
    }

    /**
     * 组件作为参数的生产方式创建组件
     * @param type  组件类型
     * @param name  组件名称
     * @param children  子组件
     * @return  组件
     */
    default UIComponent create(String type,String name,UIComponent... children){
        throw new UnsupportedOperationException("该工厂不支持组件作为参数的生产方式");
    }

}