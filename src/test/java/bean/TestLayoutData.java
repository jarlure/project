package bean;

import com.jarlure.project.bean.LayoutData;
import com.jarlure.ui.system.AssetManager;
import com.jarlure.ui.system.InputManager;
import com.jarlure.ui.system.UIRenderState;
import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.FileLocator;

public class TestLayoutData extends SimpleApplication {

    public static void main(String[] args) {
        TestLayoutData app = new TestLayoutData();
        app.setShowSettings(false);
        app.start();
    }

    public TestLayoutData() {
        super(new UIRenderState());
    }

    @Override
    public void simpleInitApp() {
        AssetManager.initialize(this);
        InputManager.initialize(this);

        assetManager.registerLocator("E:\\", FileLocator.class);
        LayoutData data = (LayoutData) AssetManager.loadAsset("data.j3o");
    }

}