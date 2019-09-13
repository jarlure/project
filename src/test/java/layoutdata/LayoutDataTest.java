package layoutdata;

import com.jarlure.ui.system.AssetManager;
import com.jarlure.ui.system.InputManager;
import com.jarlure.ui.system.UIRenderState;
import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.FileLocator;

public class LayoutDataTest extends SimpleApplication {

    public static void main(String[] args) {
        LayoutDataTest app = new LayoutDataTest();
        app.setShowSettings(false);
        app.start();
    }

    public LayoutDataTest() {
        super(new UIRenderState());
    }

    @Override
    public void simpleInitApp() {
        AssetManager.initialize(this);
        InputManager.initialize(this);

        assetManager.registerLocator("C:\\Users\\Administrator\\Desktop", FileLocator.class);
        Object obj = AssetManager.loadAsset("tt.j3o");
        System.out.println();
    }

}