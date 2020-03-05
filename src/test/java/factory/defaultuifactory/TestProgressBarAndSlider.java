package factory.defaultuifactory;

import com.jarlure.project.bean.LayerImageData;
import com.jarlure.project.factory.DefaultUIFactory;
import com.jarlure.project.factory.UIFactory;
import com.jarlure.ui.component.UIComponent;
import com.jarlure.ui.property.PercentProperty;
import com.jarlure.ui.system.AssetManager;
import com.jarlure.ui.system.InputManager;
import com.jarlure.ui.system.UIRenderState;
import com.jarlure.ui.util.ImageHandler;
import com.jme3.app.SimpleApplication;
import com.jme3.math.ColorRGBA;
import com.jme3.texture.Image;

public class TestProgressBarAndSlider extends SimpleApplication {

    public static void main(String[] args) {
        TestProgressBarAndSlider app = new TestProgressBarAndSlider();
        app.setShowSettings(false);
        app.start();
    }

    public TestProgressBarAndSlider() {
        super(new UIRenderState());
    }

    @Override
    public void simpleInitApp() {
        AssetManager.initialize(this);
        InputManager.initialize(this);

        Image progressBarImg = ImageHandler.createEmptyImage(100,5);
        Image progressBarFullImg = ImageHandler.createEmptyImage(100,5);
        Image btnImg = ImageHandler.createEmptyImage(10,10);
        ImageHandler.drawColor(progressBarImg, ColorRGBA.DarkGray);
        ImageHandler.drawColor(progressBarFullImg,ColorRGBA.Green);
        ImageHandler.drawColor(btnImg, ColorRGBA.White);
        LayerImageData progressBar_empty = new LayerImageData(progressBarImg,100,100);
        LayerImageData progressBar_full = new LayerImageData(progressBarFullImg,100,100);
        LayerImageData buttonIcon = new LayerImageData(btnImg,100,100);

        UIFactory factory = new DefaultUIFactory();
        UIComponent progressBar = factory.create(DefaultUIFactory.ProgressBar,"progressBar",progressBar_empty,progressBar_full);
        UIComponent button = factory.create(DefaultUIFactory.Picture,"button",buttonIcon);
        UIComponent slider = factory.create(DefaultUIFactory.Slider,"slider",progressBar,button);

        stateManager.getState(UIRenderState.class).attachChildToNode(slider);

        slider.get(PercentProperty.class).setPercent(0.5f);
    }

}