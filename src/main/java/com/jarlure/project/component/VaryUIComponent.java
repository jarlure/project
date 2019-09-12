package com.jarlure.project.component;

import com.jarlure.ui.component.UIComponent;
import com.jarlure.ui.property.common.Property;

public class VaryUIComponent extends Property<UIComponent> implements UIComponent {

    /**
     * 组件变量。这是一个组件代理。你可以使用该类动态变更你的组件而与之关联的逻辑控制层不需要重新更新。例如你的类在初始
     * 化时如果需要获取组件引用，那么你可以先创建一个该类对象，等稍后布局初始化完毕再将真正的组件设置进这个对象中使用。
     * 另外该类允许你添加监听器监听组件的变更情况。
     */
    public VaryUIComponent() {
        super();
    }

    @Override
    public float getDepth() {
        if (value == null) return 0;
        return value.getDepth();
    }

    @Override
    public void setDepth(float depth) {
        if (value == null) return;
        value.setDepth(depth);
    }

    @Override
    public void scale(float x_percent, float y_percent) {
        if (value == null) return;
        value.scale(x_percent, y_percent);
    }

    @Override
    public void move(float dx, float dy) {
        if (value == null) return;
        value.move(dx, dy);
    }

    @Override
    public void rotate(float angle) {
        if (value == null) return;
        value.rotate(angle);
    }

    @Override
    public boolean isVisible() {
        if (value == null) return false;
        return value.isVisible();
    }

    @Override
    public void setVisible(boolean visible) {
        if (value == null) return;
        value.setVisible(visible);
    }

    @Override
    public boolean toggleVisible() {
        if (value == null) return false;
        return value.toggleVisible();
    }

    @Override
    public Object get(String param) {
        if (value == null) return null;
        return value.get(param);
    }

    @Override
    public <T> boolean exist(Class<T> type) {
        if (value == null) return false;
        return value.exist(type);
    }

    @Override
    public <T> T get(Class<T> type) {
        if (value == null) return null;
        return value.get(type);
    }

    @Override
    public <K, V extends K> void set(Class<K> type, V value) {
        if (this.value == null) return;
        this.value.set(type, value);
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) return true;
        if (this.value == null) return false;
        return this.value.equals(obj);
    }

}