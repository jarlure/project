package layerimagedata;

import com.jarlure.project.bean.LayerImageData;
import com.jarlure.ui.util.ImageHandler;
import com.jme3.texture.Image;

public class LayerImageDataTest {

    public static void main(String[] args) {
        Image img = ImageHandler.createEmptyImage(100,100);
        LayerImageData data1 = new LayerImageData(img,0,0);
        LayerImageData data2 = data1.clone();
        System.out.println(data1.getImg()==data2.getImg());
    }

}
