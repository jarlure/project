package com.jarlure.project.layout;

import com.jarlure.project.bean.LayerImageData;
import com.jarlure.project.bean.LayoutData;
import com.jarlure.ui.component.UIComponent;
import com.jarlure.ui.component.UINode;
import com.jarlure.ui.property.ChildrenProperty;

import java.util.List;
import java.util.Map;

public abstract class AbstractLayout implements Layout {

    private UINode layoutNode;
    private int layoutWidth;
    private int layoutHeight;

    /**
     * 获取引用外部布局的组件的名称-外部布局类映射。
     *
     * @return 引用外部布局的组件的名称-外部布局类映射
     */
    public abstract Map<String, Class<Layout>> getLinkedComponentLayoutMap();

    /**
     * 加载布局数据，初始化布局
     *
     * @param data 布局数据
     */
    public final void loadLayout(LayoutData data) {
        layoutWidth = data.getLayoutWidth();
        layoutHeight = data.getLayoutHeight();
        layoutNode = createComponent(data.getImgList());
        configureUIComponent();
    }

    /**
     * 创建布局上的组件
     *
     * @param layerImageData 图层数据
     * @return 布局根结点
     */
    protected abstract UINode createComponent(List<LayerImageData> layerImageData);

    /**
     * 配置布局上的组件。例如组件被创建后，需要配置监听器、层次结构、是否可见、组件关联等额外信息。
     */
    protected abstract void configureUIComponent();

    @Override
    public float getLayoutWidth() {
        return layoutWidth;
    }

    @Override
    public float getLayoutHeight() {
        return layoutHeight;
    }

    @Override
    public UINode getLayoutNode() {
        return layoutNode;
    }

    @Override
    public UIComponent getComponent(String name) {
        return layoutNode.get(ChildrenProperty.class).getChildByName(name);
    }

}
