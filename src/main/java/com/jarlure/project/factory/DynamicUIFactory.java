package com.jarlure.project.factory;

import com.jarlure.project.bean.LayerImageData;
import com.jarlure.ui.component.UIComponent;
import com.jarlure.ui.component.Vision;
import com.jme3.scene.Spatial;

public class DynamicUIFactory extends AbstractUIFactory {

    private UIFactory factory;
    private String type;
    private String name;
    private Object data;

    /**
     * 动态组件工厂。用于延迟创建或多次创建组件。例如创建面板上的列表项组件。
     *
     * @param factory 实际用于生产的工厂
     */
    public DynamicUIFactory(UIFactory factory) {
        this.factory = factory;
    }

    public DynamicUIFactory(UIFactory factory, String type, String name, LayerImageData... data) {
        this.factory = factory;
        this.type = type;
        this.name = name;
        this.data = data;
    }

    public DynamicUIFactory(UIFactory factory, String type, String name, UIComponent... children) {
        this.factory = factory;
        this.type = type;
        this.name = name;
        this.data = children;
    }

    /**
     * 获取实际用于生产的工厂
     *
     * @return 实际用于生产的工厂
     */
    public UIFactory getFactory() {
        return factory;
    }

    /**
     * 设置实际用于生产的工厂
     *
     * @param factory 实际用于生产的工厂
     */
    public void setFactory(UIFactory factory) {
        this.factory = factory;
    }

    /**
     * 获取组件类型
     *
     * @return 组件类型
     */
    public String getType() {
        return type;
    }

    /**
     * 设置组件类型
     *
     * @param type 组件类型
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * 获取组件名称
     *
     * @return 组件名称
     */
    public String getName() {
        return name;
    }

    /**
     * 设置组件名称
     *
     * @param name 组件名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 设置图层数据。图层数据与子组件原件不能共存。设置图层数据会覆盖旧的图层数据或子组件原件、
     *
     * @param data 图层数据
     */
    public void setData(LayerImageData... data) {
        this.data = data;
    }

    /**
     * 设置子组件原件。图层数据与子组件原件不能共存。设置子组件原件会覆盖图层数据或旧的子组件原件、
     *
     * @param children 子组件原件
     */
    public void setChildren(UIComponent... children) {
        this.data = children;
    }

    /**
     * 动态组件工厂允许直接使用默认生产方式创建组件。如果组件原件是复合组件且无法获取某个子组件原件的组件工厂，则会创建
     * 该子组件的镜像作为其替代品。
     *
     * @return 组件
     */
    @Override
    public UIComponent create() {
        if (data instanceof LayerImageData[]) {
            return factory.create(type, name, (LayerImageData[]) data);
        }
        if (data instanceof UIComponent[]) {
            UIComponent[] childrenSrc = (UIComponent[]) data;
            UIComponent[] children = new UIComponent[childrenSrc.length];
            for (int i = 0; i < children.length; i++) {
                UIFactory factory = childrenSrc[i].get(UIFactory.class);
                if (factory != null) children[i] = factory.create();
                else children[i] = new Vision((Spatial) childrenSrc[i].get(UIComponent.VIEW));
            }
            return factory.create(type, name, children);
        }
        return factory.create(type, name);
    }

    @Override
    public UIComponent create(String type, String name) {
        return factory.create(type, name);
    }

    @Override
    public UIComponent create(String type, String name, LayerImageData... data) {
        return factory.create(type, name, data);
    }

    @Override
    public UIComponent create(String type, String name, UIComponent... children) {
        return factory.create(type, name, children);
    }

}
