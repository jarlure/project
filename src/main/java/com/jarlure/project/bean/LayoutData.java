package com.jarlure.project.bean;

import com.jarlure.project.util.SavableHelper;
import com.jme3.export.*;
import com.jme3.texture.Image;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LayoutData implements Savable {

    private static final String REFERENCE = "ref";//图片引用关系在序列化时的键值

    private int layoutWidth;
    private int layoutHeight;
    private List<LayerImageData> imgList;

    /**
     * 获得布局宽度。等于PS画布宽度
     * @return  布局宽度
     */
    public int getLayoutWidth() {
        return layoutWidth;
    }

    /**
     * 设置布局宽度。等于PS画布宽度
     * @param layoutWidth   布局宽度
     */
    public void setLayoutWidth(int layoutWidth) {
        this.layoutWidth = layoutWidth;
    }

    /**
     * 获得布局高度。等于PS画布高度
     * @return  布局高度
     */
    public int getLayoutHeight() {
        return layoutHeight;
    }

    /**
     * 设置布局高度。等于PS画布高度
     * @param layoutHeight  布局高度
     */
    public void setLayoutHeight(int layoutHeight) {
        this.layoutHeight = layoutHeight;
    }

    /**
     * 获取图层数据列表
     * @return  图层数据列表
     */
    public List<LayerImageData> getImgList() {
        return imgList;
    }

    /**
     * 设置图层数据列表
     * @param imgList   图层数据列表
     */
    public void setImgList(List<LayerImageData> imgList) {
        this.imgList = imgList;
    }

    @Override
    public void write(JmeExporter ex) throws IOException {
        Bundle bundle = new Bundle();
        Helper.packImageReference(imgList,bundle);//保存图片引用关系
        OutputCapsule capsule = ex.getCapsule(this);
        capsule.write(bundle,Bundle.class.getSimpleName(),null);
        SavableHelper.write(this, capsule, null);
    }

    @Override
    public void read(JmeImporter im) throws IOException {
        InputCapsule capsule = im.getCapsule(this);
        SavableHelper.read(this, capsule, null);
        Bundle bundle = (Bundle) capsule.readSavable(Bundle.class.getSimpleName(),null);
        Helper.unpackImageReference(imgList,bundle);
    }

    private static class Helper{

        private static void packImageReference(List<LayerImageData> layerImgDataList, Bundle bundle){
            if (layerImgDataList==null || layerImgDataList.isEmpty() || bundle==null)return;
            List<Integer> imgReferenceList = new ArrayList<>(4);
            for (int i=0,imgIndex=0,len=layerImgDataList.size();i<len;i++){
                LayerImageData data_i = layerImgDataList.get(i);
                Image img = data_i.getImg();
                if (img==null)continue;
                for (int j=i+1;j<len;j++){
                    LayerImageData data_j = layerImgDataList.get(j);
                    if (data_j.getImg()!=img)continue;
                    data_j.setImg(null);
                    imgReferenceList.add(j);
                }
                if (imgReferenceList.isEmpty())continue;
                imgReferenceList.add(0,i);
                int[] imgReferenceArray = new int[imgReferenceList.size()];{
                    for (int j=0;j<imgReferenceArray.length;j++){
                        imgReferenceArray[j]=imgReferenceList.get(j);
                    }
                }
                imgReferenceList.clear();
                bundle.put(REFERENCE+imgIndex,imgReferenceArray);
                imgIndex++;
            }
        }

        private static void unpackImageReference(List<LayerImageData> layerImgDataList, Bundle bundle){
            if (layerImgDataList==null || layerImgDataList.isEmpty() || bundle==null)return;
            for (int i=0;;i++){//恢复图片引用
                String refIndex = REFERENCE+i;
                if (!bundle.exist(refIndex))break;
                int[] imgReferenceArray = bundle.get(refIndex);
                Image img = layerImgDataList.get(imgReferenceArray[0]).getImg();
                for (int index:imgReferenceArray){
                    layerImgDataList.get(index).setImg(img);
                }
            }
        }

    }

}