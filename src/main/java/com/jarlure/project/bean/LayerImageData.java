package com.jarlure.project.bean;

import com.jarlure.project.util.StringHandler;
import com.jarlure.ui.util.ImageHandler;
import com.jme3.export.*;
import com.jme3.texture.Image;

import java.io.IOException;

public class LayerImageData implements Savable, Cloneable {
    private String name;
    private int top;
    private int left;
    private int bottom;
    private int right;
    private Image img;

    public LayerImageData() {
    }

    public LayerImageData(Image img) {
        this(img, 0, 0);
    }

    /**
     * 图层图片数据。用于存储PS中图层数据的数据结构
     *
     * @param img    图片
     * @param left   图层的左边位置水平坐标x值
     * @param bottom 图层底边位置垂直坐标y值
     */
    public LayerImageData(Image img, int left, int bottom) {
        this.img = img;
        this.left = left;
        this.bottom = bottom;
        this.right = left + img.getWidth();
        this.top = bottom + img.getHeight();
    }

    /**
     * 图层图片数据。用于存储PS中图层数据的数据结构
     *
     * @param top    图层的顶边位置垂直坐标y值
     * @param bottom 图层的底边位置垂直坐标y值
     * @param left   图层的左边位置水平坐标x值
     * @param right  图层的右边位置水平坐标x值
     */
    public LayerImageData(int top, int bottom, int left, int right) {
        this.top = top;
        this.bottom = bottom;
        this.left = left;
        this.right = right;
    }

    public LayerImageData(LayerImageData src) {
        this.name = src.name;
        this.top = src.top;
        this.bottom = src.bottom;
        this.left = src.left;
        this.right = src.right;
        this.img = src.img;
    }

    /**
     * 获取图层名
     *
     * @return 图层名
     */
    public String getName() {
        return this.name;
    }

    /**
     * 设置图层名
     *
     * @param name 图层名
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取图层的顶边位置垂直坐标y值
     *
     * @return 图层的顶边位置垂直坐标y值
     */
    public int getTop() {
        return this.top;
    }

    /**
     * 设置图层的顶边位置垂直坐标y值
     *
     * @param top 图层的顶边位置垂直坐标y值
     */
    public void setTop(int top) {
        this.top = top;
    }

    /**
     * 获取图层的左边位置水平坐标x值
     *
     * @return 图层的左边位置水平坐标x值
     */
    public int getLeft() {
        return this.left;
    }

    /**
     * 设置图层的左边位置水平坐标x值
     *
     * @param left 图层的左边位置水平坐标x值
     */
    public void setLeft(int left) {
        this.left = left;
    }

    /**
     * 获取图层的底边位置垂直坐标y值
     *
     * @return 图层的底边位置垂直坐标y值
     */
    public int getBottom() {
        return this.bottom;
    }

    /**
     * 设置图层的底边位置垂直坐标y值
     *
     * @param bottom 图层的底边位置垂直坐标y值
     */
    public void setBottom(int bottom) {
        this.bottom = bottom;
    }

    /**
     * 获取图层的右边位置水平坐标x值
     *
     * @return 图层的右边位置水平坐标x值
     */
    public int getRight() {
        return this.right;
    }

    /**
     * 设置图层的右边位置水平坐标x值
     *
     * @param right 图层的右边位置水平坐标x值
     */
    public void setRight(int right) {
        this.right = right;
    }

    /**
     * 获取图片
     *
     * @return 图片
     */
    public Image getImg() {
        return this.img;
    }

    /**
     * 设置图片
     *
     * @param img 图片
     */
    public void setImg(Image img) {
        this.img = img;
    }

    /**
     * 获取图层的宽度
     *
     * @return 图层的宽
     */
    public int getWidth() {
        return this.right - this.left;
    }

    /**
     * 获取图层的高度
     *
     * @return 图层的高
     */
    public int getHeight() {
        return this.top - this.bottom;
    }

    @Override
    public String toString() {
        return StringHandler.toString(this);
    }

    /**
     * 浅拷贝。共用同一个名称和图片引用
     *
     * @return 该图层数据的复制品
     */
    public LayerImageData clone() {
        try {
            return (LayerImageData) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    public void read(JmeImporter im) {
        InputCapsule capsule = im.getCapsule(this);
        try {
            int[] pos = capsule.readIntArray("pos", null);
            if (pos != null) {
                top = pos[0];
                bottom = pos[1];
                left = pos[2];
                right = pos[3];
            }
            byte[] img = capsule.readByteArray("img", null);
            if (img != null) {
                int[] size = capsule.readIntArray("size", null);
                this.img = ImageHandler.decompress(img, size[0], size[1]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void write(JmeExporter ex) {
        OutputCapsule capsule = ex.getCapsule(this);
        try {
            if (left != right && top != bottom) {
                int[] pos = new int[]{top, bottom, left, right};
                capsule.write(pos, "pos", null);
            }
            if (img != null) {
                int[] size = new int[]{img.getWidth(), img.getHeight()};
                byte[] img = ImageHandler.compress(this.img,0.7f);
                capsule.write(size, "size", null);
                capsule.write(img, "img", null);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
