package bean;

import com.jarlure.project.bean.Bundle;
import com.jarlure.project.util.SavableHelper;
import com.jme3.app.SimpleApplication;

import java.io.File;

public class TestSaveData extends SimpleApplication {

    public static void main(String[] args) {
        SimpleApplication app = new TestSaveData();
        app.setShowSettings(false);
        app.start();
    }

    @Override
    public void simpleInitApp() {
        byte[][] img = new byte[3001][3001];
        img[3000][3000]=127;
        Bundle bundle = new Bundle();
        bundle.put("img",img);
        SavableHelper.saveAsJ3OData(new File("src/test/resources/Data/img.j3o"),bundle);
//        保存后不会马上写入磁盘，读取的内容为旧值
//        bundle = (Bundle) assetManager.loadAsset("Data/img.j3o");
//        img = bundle.get("img");
//        System.out.println(img[3000][3000]);
    }
}
