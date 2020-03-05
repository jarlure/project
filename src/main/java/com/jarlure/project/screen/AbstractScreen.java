package com.jarlure.project.screen;

import com.jarlure.project.bean.Bundle;
import com.jarlure.project.bean.LayoutData;
import com.jarlure.project.layout.AbstractLayout;
import com.jarlure.project.layout.Layout;
import com.jarlure.project.screen.screenstate.ScreenState;
import com.jarlure.ui.component.UIComponent;
import com.jarlure.ui.property.ChildrenProperty;
import com.jarlure.ui.system.AssetManager;
import com.jarlure.ui.system.UIRenderState;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AbstractScreen implements Screen {

    protected Application app;
    protected Layout layout;
    private boolean initialized;
    private boolean enabled;
    private boolean visible;
    protected final List<ScreenState> screenStates = new ArrayList<>();

    /**
     * 获取子类关联的布局类。该方法由子类实现
     *
     * @return 子类关联的布局类
     */
    protected abstract Class<? extends Layout> getLayoutClass();

    /**
     * 获取数据包
     * @return  数据包
     */
    protected abstract Bundle getBundle();

    /**
     * 由子类实现的初始化。此时布局已经加载完毕，UI场景仍处于未初始化和禁用状态
     */
    protected abstract void initialize();

    @Override
    public final boolean isInitialized() {
        return initialized;
    }

    @Override
    public final void initialize(AppStateManager stateManager, Application app) {
        this.app = app;
        layout = loadLayout(getLayoutClass());
        initialize();
        initialized = true;
        if (enabled) onEnable();
    }

    /**
     * 加载布局。
     *
     * @param layoutClass 布局类
     * @return 布局类对象
     */
    protected Layout loadLayout(Class layoutClass) {
        Layout layout = null;
        try {
            layout = (Layout) layoutClass.newInstance();
        } catch (IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        LayoutData data = (LayoutData) AssetManager.loadAsset(getLayoutDataURL(layoutClass));
        ((AbstractLayout) layout).loadLayout(data);

        Map<String, Class<? extends Layout>> linkedComponentLayoutMap = ((AbstractLayout) layout).getLinkedComponentLayoutMap();
        if (linkedComponentLayoutMap != null) {
            for (Map.Entry<String, Class<? extends Layout>> entry : linkedComponentLayoutMap.entrySet()) {
                UIComponent linkedComponent = layout.getComponent(entry.getKey());
                Layout linkedLayout = loadLayout(entry.getValue());
                List<UIComponent> childrenList = linkedLayout.getLayoutNode().get(ChildrenProperty.class).value;
                linkedComponent.get(ChildrenProperty.class).attachChild(childrenList);
            }
        }

        return layout;
    }

    /**
     * 获取布局数据文件的路径。默认为"Interface/布局名称/布局名称.j3o"。也可以通过重写覆盖。注意：布局数据文件实际由
     * JME3的AssetManager加载。如果发生路径不正确的情况，请参看AssetManager加载j3o文件的相关教程。
     * @param layoutClass   布局类
     * @return  布局数据文件的路径
     */
    protected String getLayoutDataURL(Class layoutClass) {
        String layoutName = layoutClass.getSimpleName();
        return "Interface/" + layoutName + "/" + layoutName + ".j3o";
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public final void setEnabled(boolean enabled) {
        if (this.enabled == enabled) return;
        this.enabled = enabled;
        if (!isInitialized()) return;
        if (enabled) {
            onEnable();
        } else {
            onDisable();
        }
    }

    /**
     * 启动UI场景
     */
    protected void onEnable() {
        for (ScreenState screenState : screenStates) {
            screenState.initialize(app, this);
        }
        for (ScreenState screenState : screenStates) {
            screenState.setLayout(layout);
        }
    }

    @Override
    public void loadData() {
        Bundle bundle = getBundle();
        for (ScreenState screenState : screenStates) {
            screenState.loadData(bundle);
        }
    }

    @Override
    public final boolean isVisible() {
        return visible;
    }

    @Override
    public void setVisible(boolean visible) {
        if (layout == null) return;
        if (this.visible == visible) return;
        this.visible = visible;
        if (visible) {
            UIRenderState renderState = getState(UIRenderState.class);
            if (renderState != null) {
                ChildrenProperty childrenProperty = renderState.getNode().get(ChildrenProperty.class);
                childrenProperty.attachChild(layout.getLayoutNode());
            } else {
                Node node = (Node) layout.getLayoutNode().get(UIComponent.VIEW);
                ((SimpleApplication) app).getGuiNode().attachChild(node);
            }
        } else {
            UIRenderState renderState = getState(UIRenderState.class);
            if (renderState != null) {
                ChildrenProperty childrenProperty = renderState.getNode().get(ChildrenProperty.class);
                childrenProperty.detachChild(layout.getLayoutNode());
            } else {
                Node node = (Node) layout.getLayoutNode().get(UIComponent.VIEW);
                node.removeFromParent();
            }
        }
    }

    @Override
    public void update(float tpf) {
        for (ScreenState screenState : screenStates) {
            if (screenState.isEnabled()) screenState.update(tpf);
        }
    }

    @Override
    public <T extends Screen> void skipTo(Class<T> screenClass) {
        Screen screen = getState(screenClass);
        if (screen == null || this == screen) return;
        this.saveData();
        this.setEnabled(false);
        screen.setEnabled(true);
        screen.loadData();
        this.setVisible(false);
        screen.setVisible(true);
    }

    @Override
    public void saveData() {
        Bundle bundle = getBundle();
        for (ScreenState screenState : screenStates) {
            screenState.saveData(bundle);
        }
    }

    /**
     * 禁用UI场景
     */
    protected void onDisable() {
        for (ScreenState screenState : screenStates) {
            screenState.cleanup();
        }
    }

    @Override
    public void cleanup() {
        setVisible(false);
        setEnabled(false);
        layout = null;
        screenStates.clear();
        initialized = false;
        app = null;
    }

    @Override
    public final <T extends Object> T getState(Class<T> type) {
        if (ScreenState.class.isAssignableFrom(type)) {
            for (ScreenState screenState : screenStates) {
                if (type.isAssignableFrom(screenState.getClass())) {
                    return (T) screenState;
                }
            }
        }
        if (AppState.class.isAssignableFrom(type)) {
            Class<AppState> stateClass = (Class<AppState>) type;
            return (T) app.getStateManager().getState(stateClass);
        }
        return null;
    }

    @Override
    public void stateAttached(AppStateManager stateManager) {
    }

    @Override
    public void stateDetached(AppStateManager stateManager) {
    }

    @Override
    public void render(RenderManager rm) {
    }

    @Override
    public void postRender() {
    }

}
