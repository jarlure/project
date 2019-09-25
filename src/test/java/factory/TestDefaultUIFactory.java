package factory;

import com.jarlure.project.bean.LayerImageData;
import com.jarlure.project.factory.DefaultUIFactory;
import com.jarlure.project.factory.UIFactory;
import com.jarlure.ui.component.UIComponent;
import com.jarlure.ui.effect.SwitchEffect;
import com.jarlure.ui.property.AABB;
import com.jarlure.ui.property.ChildrenProperty;
import com.jarlure.ui.system.AssetManager;
import com.jarlure.ui.system.InputManager;
import com.jarlure.ui.system.UIRenderState;
import com.jarlure.ui.util.ImageHandler;
import com.jme3.app.SimpleApplication;
import com.jme3.texture.Image;

public class TestDefaultUIFactory extends SimpleApplication {

    public static void main(String[] args) {
        TestDefaultUIFactory app = new TestDefaultUIFactory();
        app.setShowSettings(false);
        app.start();
    }

    private float timer;
    private UIComponent ideaIcon;
    private UIComponent dialog;
    private UIComponent button;
    private UIComponent dot9Button;

    public TestDefaultUIFactory() {
        super(new UIRenderState());
    }

    @Override
    public void simpleInitApp() {
        AssetManager.initialize(this);
        InputManager.initialize(this);

        //Picture:多张相同位置尺寸，按顺序放进SwitchEffect中
        Image ideaIcon_nothingImg = ImageHandler.loadImage("src/test/resources/IDEA图标/IDEA图标.png");
        Image ideaIcon_moveonImg = ImageHandler.loadImage("src/test/resources/IDEA图标/IDEA图标（移入）.png");
        Image ideaIcon_selectImg = ImageHandler.loadImage("src/test/resources/IDEA图标/IDEA图标（选中）.png");
        LayerImageData ideaIcon_nothing = new LayerImageData(ideaIcon_nothingImg,0,0);
        LayerImageData ideaIcon_moveon = new LayerImageData(ideaIcon_moveonImg,0,0);
        LayerImageData ideaIcon_select = new LayerImageData(ideaIcon_selectImg,0,0);
        LayerImageData[] ideaIconData=new LayerImageData[]{ideaIcon_nothing,ideaIcon_moveon,ideaIcon_select};

        //Picture:多张不同位置尺寸（大、小、小），合成一张图
        Image dialog_noButtonImg = ImageHandler.loadImage("src/test/resources/对话框/对话框（否按钮）.png");
        Image dialog_yesButtonImg = ImageHandler.loadImage("src/test/resources/对话框/对话框（是按钮）.png");
        Image dialog_backgroundImg = ImageHandler.loadImage("src/test/resources/对话框/对话框（背景框）.png");
        LayerImageData dialog_noButton = new LayerImageData(dialog_noButtonImg,100+107,6);
        LayerImageData dialog_yesButton = new LayerImageData(dialog_yesButtonImg,100+21,6);
        LayerImageData dialog_background = new LayerImageData(dialog_backgroundImg,100,0);
        LayerImageData[] dialogData=new LayerImageData[]{dialog_background,dialog_yesButton,dialog_noButton};

        //Picture:多张除第一张外相同位置尺寸，第一张图分别与其余图合并一次然后除第一张图外按顺序放进SwitchEffect中
        Image button_nothingImg = ImageHandler.loadImage("src/test/resources/按钮/按钮.png");
        Image button_textImg = ImageHandler.loadImage("src/test/resources/按钮/按钮文本.png");
        Image button_pressedImg = ImageHandler.loadImage("src/test/resources/按钮/按钮（按下）.png");
        LayerImageData button_nothing = new LayerImageData(button_nothingImg,300,0);
        LayerImageData button_text = new LayerImageData(button_textImg,300+24,5);
        LayerImageData button_pressed = new LayerImageData(button_pressedImg,300,0);
        LayerImageData[] buttonData = new LayerImageData[]{button_nothing,button_pressed,button_text};

        //Picture:多张除第一张是点九图外其余相同位置尺寸，按顺序放进SwitchEffect中
        Image dot9Button_nothingImg = ImageHandler.loadImage("src/test/resources/按钮/点九按钮.png");
        Image dot9Button_pressedImg = ImageHandler.loadImage("src/test/resources/按钮/点九按钮（按下）.png");
        LayerImageData dot9Button_nothing = new LayerImageData(dot9Button_nothingImg,400,0);
        LayerImageData dot9Button_pressed = new LayerImageData(dot9Button_pressedImg,400-1,0-1);
        LayerImageData[] dot9ButtonData = new LayerImageData[]{dot9Button_nothing,dot9Button_pressed};

        UIFactory factory = new DefaultUIFactory();
        ideaIcon = factory.create(DefaultUIFactory.Picture,"IDEA_ICON",ideaIconData);
        dialog = factory.create(DefaultUIFactory.Picture,"DIALOG",dialogData);
        button = factory.create(DefaultUIFactory.Picture,"BUTTON",buttonData);
        dot9Button = factory.create(DefaultUIFactory.Picture,"DOT9_BUTTON",dot9ButtonData);
        dot9Button.get(AABB.class).setWidth(100);

        ChildrenProperty childrenProperty = getStateManager().getState(UIRenderState.class).getNode().get(ChildrenProperty.class);
        childrenProperty.attachChild(ideaIcon);
        childrenProperty.attachChild(dialog);
        childrenProperty.attachChild(button);
        childrenProperty.attachChild(dot9Button);
    }

    @Override
    public void simpleUpdate(float tpf) {
        timer+=tpf;
        if (timer<1)return;
        timer-=1;
        //测试是否存在SwitchEffect、图片是否按顺序播放、图片是否正确
        ideaIcon.get(SwitchEffect.class).switchToNext();
        button.get(SwitchEffect.class).switchToNext();
        dot9Button.get(SwitchEffect.class).switchToNext();
    }

}