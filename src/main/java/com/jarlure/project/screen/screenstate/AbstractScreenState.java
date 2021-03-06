package com.jarlure.project.screen.screenstate;

import com.jarlure.project.bean.Bundle;
import com.jarlure.project.layout.Layout;
import com.jarlure.project.screen.Screen;
import com.jarlure.project.screen.screenstate.operation.Operation;
import com.jme3.app.Application;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractScreenState implements ScreenState{

    protected Application app;
    protected Screen screen;
    protected List<Operation> operations=new ArrayList<>(0);
    private boolean initialized;
    private boolean enabled;

    @Override
    public final boolean isInitialized() {
        return initialized;
    }

    @Override
    public final void initialize(Application app, Screen screen) {
        this.app=app;
        this.screen=screen;
        this.enabled=true;
        initialize();
        initialized=true;
        if (enabled) onEnable();
    }

    protected void initialize(){
        for (Operation operation:operations){
            operation.initialize();
        }
    }

    @Override
    public void cleanup() {
        setEnabled(false);
        initialized=false;
        for (Operation operation:operations){
            operation.cleanup();
        }
    }

    @Override
    public final boolean isEnabled() {
        return enabled;
    }

    @Override
    public final void setEnabled(boolean enabled) {
        if (this.enabled==enabled)return;
        this.enabled = enabled;
        if (!isInitialized())return;
        if (enabled) onEnable();
        else onDisable();
    }

    protected void onEnable() {
        for (Operation operation:operations){
            operation.onEnable();
        }
    }

    protected void onDisable() {
        for (Operation operation:operations){
            operation.onDisable();
        }
    }

    @Override
    public void setLayout(Layout layout) {
        for (Operation operation:operations){
            operation.setLayout(layout);
        }
    }

    @Override
    public void loadData(Bundle bundle) {
        for (Operation operation:operations){
            operation.loadData(bundle);
        }
    }

    @Override
    public void saveData(Bundle bundle) {
        for (Operation operation:operations){
            operation.saveData(bundle);
        }
    }

    @Override
    public void update(float tpf) {
        for (Operation operation:operations){
            operation.update(tpf);
        }
    }

    @Override
    public final Screen getScreen() {
        return screen;
    }

    @Override
    public final <T extends Operation> T getOperation(Class<T> operationClass) {
        for (Operation operation : operations) {
            if (operationClass.isAssignableFrom(operation.getClass())) {
                return (T) operation;
            }
        }
        return null;
    }

}
