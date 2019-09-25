package com.jarlure.project.factory;

import com.jarlure.project.bean.LayerImageData;
import com.jarlure.ui.component.UIComponent;

public abstract class AbstractUIFactory implements UIFactory {

    @Override
    public UIComponent create() {
        throw new UnsupportedOperationException("该工厂不支持默认生产方式");
    }

    @Override
    public UIComponent create(String type, String name) {
        throw new UnsupportedOperationException("该工厂不支持无参数生产方式");
    }

    @Override
    public UIComponent create(String type, String name, LayerImageData... data) {
        throw new UnsupportedOperationException("该工厂不支持图层作为参数的生产方式");
    }

    @Override
    public UIComponent create(String type, String name, UIComponent... children) {
        throw new UnsupportedOperationException("该工厂不支持组件作为参数的生产方式");
    }

}
