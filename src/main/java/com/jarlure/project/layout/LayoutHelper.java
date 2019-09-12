package com.jarlure.project.layout;

import com.jarlure.project.bean.Entity;
import com.jarlure.project.bean.LayerImageData;
import com.jarlure.project.factory.DefaultUIFactory;
import com.jarlure.project.factory.DynamicUIFactory;
import com.jarlure.project.factory.UIFactory;
import com.jarlure.ui.component.UIComponent;
import com.jarlure.ui.component.UINode;
import com.jarlure.ui.property.ChildrenProperty;

import java.util.EnumMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LayoutHelper {

    private static final Logger LOG = Logger.getLogger(LayoutHelper.class.getName());

    public enum Type {
        FACTORY, LAYER_IMG_DATA, TYPE, NAME, NUMBER, CHILDREN
    }

    protected Entity configure;

    /**
     * 用于帮助布局组件创建的类。由布局提供数据，该类执行创建组件的代码
     * @param layerImageData    图层数据
     */
    public LayoutHelper(List<LayerImageData> layerImageData) {
        configure = new Entity(new EnumMap<>(Type.class));
        configure.setDefaultValue(Type.FACTORY, new DefaultUIFactory());
        configure.setDefaultValue(Type.LAYER_IMG_DATA, layerImageData);
    }

    /**
     * 添加无参数组件。
     *
     * @param type 组件类型
     * @param name 组件名称
     */
    public void add(String type, String name) {
        add(type, name, new int[0]);
    }

    /**
     * 添加组件。
     *
     * @param type   组件类型
     * @param name   组件名称
     * @param number 图层数据索引
     */
    public void add(String type, String name, int... number) {
        configure.addItem(new EnumMap<>(Type.class),
                Type.TYPE, type,
                Type.NAME, name,
                Type.NUMBER, number);
    }

    /**
     * 添加复合组件。
     *
     * @param type     组件类型
     * @param name     组件名称
     * @param children 组件的子组件名称
     */
    public void add(String type, String name, String... children) {
        configure.addItem(new EnumMap<>(Type.class),
                Type.TYPE, type,
                Type.NAME, name,
                Type.CHILDREN, children);
    }

    /**
     * 设置创建组件的默认工厂。当某个组件没有设置创建该组件的工厂，则使用默认工厂创建该组件。
     *
     * @param factory 创建组件的默认工厂
     */
    public void setDefault(UIFactory factory) {
        configure.setDefaultValue(Type.FACTORY, factory);
    }

    /**
     * 设置组件参数。
     *
     * @param name    组件名称
     * @param factory 创建该组件的工厂
     */
    public void set(String name, UIFactory factory) {
        int index = configure.findItem(Type.NAME, name);
        if (index == -1) throw new IllegalArgumentException("未找到名称为" + name + "的数据项");
        configure.setValue(index, Type.FACTORY, factory);
    }

    /**
     * 设置组件参数。
     *
     * @param type    组件类型
     * @param name    组件名称
     * @param factory 创建该组件的工厂
     */
    public void set(String type, String name, UIFactory factory) {
        int index = configure.findItem(Type.NAME, name);
        if (index == -1) throw new IllegalArgumentException("未找到名称为" + name + "的数据项");
        configure.setValue(index, Type.TYPE, type);
        configure.setValue(index, Type.FACTORY, factory);
    }

    /**
     * 创建布局组件与布局根结点。
     *
     * @param layoutName 布局根结点的名称。一般跟布局名同名
     * @return 带有布局组件的布局根结点
     */
    public UINode create(String layoutName) {
        UINode node = new UINode(layoutName);
        ChildrenProperty childrenProperty = node.get(ChildrenProperty.class);
        List<LayerImageData> layerImgData = configure.getDefaultValue(Type.LAYER_IMG_DATA);
        for (int index : configure.getItems()) {
            UIFactory factory = configure.getValue(index, Type.FACTORY);
            String type = configure.getValue(index, Type.TYPE);
            String name = configure.getValue(index, Type.NAME);
            String[] children = configure.getValue(index, Type.CHILDREN);
            UIComponent component;
            LOG.log(Level.INFO, "创建组件...name={0}", name);
            if (children == null) {
                int[] number = configure.getValue(index, Type.NUMBER);
                LayerImageData[] data = new LayerImageData[number.length];
                for (int n = 0; n < number.length; n++) {
                    data[n] = layerImgData.get(number[n]);
                }
                component = create(factory, type, name, data);
            } else {
                UIComponent[] childrenComponent = new UIComponent[children.length];
                for (int j = 0; j < childrenComponent.length; j++) {
                    childrenComponent[j] = childrenProperty.getChildByName(children[j]);
                }
                component = create(factory, type, name, childrenComponent);
            }
            childrenProperty.attachChild(component);
        }
        return node;
    }

    /**
     * 创建组件。
     *
     * @param factory 组件工厂。如果组件工厂是动态组件工厂，会给工厂设置好组件原件的类型、名称、数据
     * @param type    组件类型
     * @param name    组件名称
     * @param data    组件数据。图层数据或子组件
     * @return 组件
     */
    protected UIComponent create(UIFactory factory, String type, String name, Object data) {
        UIComponent component;
        if (data instanceof LayerImageData[]) component = factory.create(type, name, (LayerImageData[]) data);
        else if (data instanceof UIComponent[]) component = factory.create(type, name, (UIComponent[]) data);
        else component = factory.create(type, name);
        if (factory instanceof DynamicUIFactory) {
            DynamicUIFactory theFactory = (DynamicUIFactory) factory;
            theFactory.setType(type);
            theFactory.setName(name);
            if (data instanceof LayerImageData[]) theFactory.setData((LayerImageData[]) data);
            else if (data instanceof UIComponent[]) theFactory.setChildren((UIComponent[]) data);
            component.set(UIFactory.class, theFactory);
            //动态组件的原件通常不用于显示，因此默认不显示
            component.setVisible(false);
        }
        return component;
    }

}