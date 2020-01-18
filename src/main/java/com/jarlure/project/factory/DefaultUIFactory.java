package com.jarlure.project.factory;

import com.jarlure.project.bean.LayerImageData;
import com.jarlure.project.util.file.code.CodeParser;
import com.jarlure.ui.bean.Direction;
import com.jarlure.ui.component.*;
import com.jarlure.ui.converter.DynamicCoord;
import com.jarlure.ui.converter.PercentConverter;
import com.jarlure.ui.converter.ScrollConverter;
import com.jarlure.ui.effect.*;
import com.jarlure.ui.property.*;
import com.jarlure.ui.util.ImageHandler;
import com.jme3.math.Vector3f;
import com.jme3.texture.Image;

public final class DefaultUIFactory extends AbstractUIFactory {

    //类型
    public static final String Node = "Node";
    public static final String Picture = "Picture";
    public static final String ProgressBar = "ProgressBar";
    public static final String Slider = "Slider";
    public static final String Panel = "Panel";
    public static final String ScrollBar = "ScrollBar";
    public static final String Dialog = "Dialog";

    //关键字
    public static final String BACKGROUND = "background";//子组件名字包含有该关键字时，父组件的位置设置为该子组件的位置
    public static final String BUTTON = "button";
    public static final String SCROLL = "scroll";

    @Override
    public UIComponent create(String type, String name) {
        if (type.equals(Node)) {
            UIComponent node = new UINode(name);

            return node;
        }

        throw new UnsupportedOperationException("该工厂无法以默认参数生产" + type + "类型的组件");
    }

    @Override
    public UIComponent create(String type, String name, LayerImageData... data) {
        /*
         * 图层处理：
         * 1.无图图层×1 =>(添加1x1透明图片)=> 普通图层×1
         * 2.点九图图层×1 =>(裁剪点九图数据像素)=> 普通图层×1 + 点九图数据
         * 3.同尺寸位置图层×(N-1) + 点九图位置图层×1 =>(2)=> 普通图层×N + 点九图数据
         * 4.同尺寸位置图层×(N-1) + 位于其它图层内部的图层×1 =>(同尺寸位置图层分别与之合并)=> 同尺寸位置图层×(N-1)
         * 5.同尺寸位置图层×(N-2) + 点九图位置图层×1 + 位于其它图层内部的图层×1 =(2,4)=> 普通图层×(N-1) + 点九图数据
         * 6.不同尺寸位置图层×N =(合并)=> 普通图层×1
         *
         * 创建Picture：
         * 普通图层×1 => Picture(图层[0]图片、尺寸、位置)
         * 普通图层×1 + 点九图数据 => Picture(图层[0]图片、尺寸、位置、点九图效果)
         * 同尺寸位置图层×N => Picture(图层[0]图片、尺寸、位置、切换图片效果)
         * 同尺寸位置图层×N + 点九图数据 => Picture(图层[0]图片、尺寸、位置、点九图效果、切换图片效果)
         */
        if (type.equals(Picture)) {
            int indexOfSameSizeFrom0To = Helper.indexOfSameSizeFrom0To(data);
            boolean isAllSameSize = indexOfSameSizeFrom0To == data.length - 1;
            boolean isAllSameSizeExceptTheLastOne = indexOfSameSizeFrom0To == data.length - 2;
            boolean isAllSameSizeExceptTheLastTwo = indexOfSameSizeFrom0To == data.length - 3;
            boolean mayTheLastOneBeNinePatchImage = (data.length == 1 || isAllSameSizeExceptTheLastOne) && Helper.mayBeNinePatchImage(data.length - 1, data);
            boolean mayTheLastTwoBeNinePatchImage = isAllSameSizeExceptTheLastTwo && Helper.mayBeNinePatchImage(data.length - 2, data);
            boolean[][] edge = new boolean[4][];
            if (mayTheLastOneBeNinePatchImage)
                data = Helper.tryToCutNinePatchImageAndCheck(data.length - 1, data, edge);
            boolean isTheLastOneBeNinePatchImage = mayTheLastOneBeNinePatchImage && !Helper.isEmptyArray(edge);
            if (mayTheLastTwoBeNinePatchImage)
                data = Helper.tryToCutNinePatchImageAndCheck(data.length - 2, data, edge);
            boolean isTheLastTwoBeNinePatchImage = mayTheLastTwoBeNinePatchImage && !Helper.isEmptyArray(edge);
            if (isTheLastOneBeNinePatchImage || isTheLastTwoBeNinePatchImage) {
                indexOfSameSizeFrom0To = Helper.indexOfSameSizeFrom0To(data);
                isAllSameSize = indexOfSameSizeFrom0To == data.length - 1;
                isAllSameSizeExceptTheLastOne = indexOfSameSizeFrom0To == data.length - 2;
            }
            boolean combineTextAndButton = isAllSameSizeExceptTheLastOne && Helper.isTheLastOneInOthers(data);
            boolean combineToOneBigImage = !isAllSameSize && !combineTextAndButton;
            if (combineTextAndButton) data = Helper.combineTextAndButton(data);
            if (combineToOneBigImage) data = Helper.combineToOneBigImage(data);

            LayerImageData data0 = data[0];
            UIComponent picture;
            if (data0.getImg() == null) {
                picture = new Picture(name, data0.getWidth(), data0.getHeight());
            } else {
                picture = new Picture(name, data0.getImg());
            }
            picture.move(data0.getLeft(), data0.getBottom());

            if (isTheLastTwoBeNinePatchImage || isTheLastOneBeNinePatchImage) {
                ImageProperty imageProperty = edge[0] == null && edge[2] == null ? null : picture.get(ImageProperty.class);
                TextProperty textProperty = edge[1] == null && edge[3] == null ? null : picture.get(TextProperty.class);
                NinePatchEffect ninePatchEffect = new NinePatchEffect(data0.getImg(), edge, imageProperty, textProperty);
                picture.set(NinePatchEffect.class, ninePatchEffect);

                picture.get(SpatialProperty.class).addPropertyListener((property, oldValue, newValue) -> {
                    if (SpatialProperty.Property.WORLD_SCALE.equals(property)) {
                        AABB box = picture.get(AABB.class);
                        float width = box.getWidth();
                        float height = box.getHeight();
                        picture.get(NinePatchEffect.class).setSize(width, height);
                    }
                });
                ninePatchEffect.setSize(data0.getWidth(), data0.getHeight());//初始化填充位置
            }
            if (data.length > 1) {
                ImageProperty imageProperty;
                if (!picture.exist(NinePatchEffect.class)) imageProperty = picture.get(ImageProperty.class);
                else {
                    imageProperty = new ImageProperty() {
                        @Override
                        public void setImage(Image img) {
                            picture.get(NinePatchEffect.class).setSrc(img);
                        }
                    };
                }
                SwitchEffect switchEffect = new SwitchEffect(imageProperty);
                for (int i = 0; i < data.length; i++) {
                    Image img = data[i].getImg();
                    if (img == null) img = ImageHandler.createEmptyImage(1, 1);
                    switchEffect.addImage(i, img);
                }
                picture.set(SwitchEffect.class, switchEffect);
            }

            return picture;
        }

        if (type.equals(ProgressBar)) {
            UIComponent progressBar = new Picture(name, data[0].getImg());
            progressBar.move(data[0].getLeft(), data[0].getBottom());

            AABB progressBarAABB = progressBar.get(AABB.class);
            boolean vertical = progressBarAABB.getWidth() >= progressBarAABB.getHeight();
            Direction dir;
            if (vertical) {
                if (data.length > 2 && 2 * progressBarAABB.getXCenter() < data[2].getLeft() + data[2].getRight())
                    dir = Direction.LEFT;
                else dir = Direction.RIGHT;
            } else {
                if (data.length > 2 && 2 * progressBarAABB.getYCenter() < data[2].getBottom() + data[2].getTop())
                    dir = Direction.DOWN;
                else dir = Direction.UP;
            }
            progressBar.set(PercentConverter.class, new PercentConverter() {
                @Override
                public float getPercent(float x, float y) {
                    AABB box = progressBar.get(AABB.class);
                    float value, maxValue;
                    switch (dir) {
                        case RIGHT:
                            value = x - box.getXLeft();
                            maxValue = box.getWidth();
                            break;
                        case LEFT:
                            value = box.getXRight() - x;
                            maxValue = box.getWidth();
                            break;
                        case UP:
                            value = y - box.getYBottom();
                            maxValue = box.getHeight();
                            break;
                        case DOWN:
                            value = box.getYTop() - y;
                            maxValue = box.getHeight();
                            break;
                        default:
                            throw new IllegalStateException();
                    }
                    return value / maxValue;
                }

                @Override
                public float getX(float percent) {
                    AABB box = progressBar.get(AABB.class);
                    switch (dir) {
                        case LEFT:
                            percent = 1 - percent;
                        case RIGHT:
                            return box.getXLeft() + percent * box.getWidth();
                        case UP:
                        case DOWN:
                            return box.getXCenter();
                        default:
                            throw new IllegalStateException();
                    }
                }

                @Override
                public float getY(float percent) {
                    AABB box = progressBar.get(AABB.class);
                    switch (dir) {
                        case LEFT:
                        case RIGHT:
                            return box.getYCenter();
                        case DOWN:
                            percent = 1 - percent;
                        case UP:
                            return box.getYBottom() + percent * box.getHeight();
                        default:
                            throw new IllegalStateException();
                    }
                }
            });
            progressBar.get(PercentProperty.class).addInputPropertyFilter((value) -> {
                if (value < 0) return 0f;
                if (value > 1) return 1f;
                return value;
            });

            ProgressEffect progressEffect = new ProgressEffect(data[0].getImg(), data[1].getImg(), progressBar.get(ImageProperty.class));
            progressBar.set(ProgressEffect.class, progressEffect);
            progressBar.get(PercentProperty.class).addPropertyListener((oldValue, newValue) -> {
                float percent = newValue;
                progressBar.get(ProgressEffect.class).setPercent(percent);
            });

            return progressBar;
        }

        if (type.equals(Panel)) {
            Panel panel;
            if (data[0].getImg() == null) {
                panel = new Panel(name, data[0].getWidth(), data[0].getHeight());
            } else {
                panel = new Panel(name, data[0].getImg());
            }
            panel.move(data[0].getLeft(), data[0].getBottom());

            AABB box = panel.get(AABB.class);
            panel.get(RangeProperty.class).setRange(box.getXLeft(), box.getYBottom(), box.getXRight(), box.getYTop());

            if (data.length > 2) {
                float width = data[0].getLeft() + data[1].getWidth();
                float height = data[0].getBottom() + data[1].getHeight();
                AABB panelAABB = panel.get(AABB.class);
                DynamicCoord size = new DynamicCoord(panelAABB, width, height);
                DynamicCoord pos1 = new DynamicCoord(panelAABB, 0.5f * (data[1].getLeft() + data[1].getRight()), 0.5f * (data[1].getBottom() + data[1].getTop()));
                DynamicCoord pos2 = new DynamicCoord(panelAABB, 0.5f * (data[2].getLeft() + data[2].getRight()), 0.5f * (data[2].getBottom() + data[2].getTop()));

                OrderEffect orderEffect = new OrderEffect(panel.get(ElementProperty.class), size, pos1, pos2);
                panel.set(OrderEffect.class, orderEffect);
            }

            return panel;
        }

        throw new UnsupportedOperationException("该工厂无法以图层参数生产" + type + "类型的组件");
    }

    @Override
    public UIComponent create(String type, String name, UIComponent... children) {
        if (type.equals(Node)) {
            UIComponent node = new UINode(name);

            node.get(ChildrenProperty.class).attachChild(children);

            return node;
        }

        if (type.equals(Slider)) {
            UIComponent slider = new UINode(name);
            UIComponent progressBar = Helper.find(children, ProgressBar);
            UIComponent button = Helper.find(children, BUTTON);

            Vector3f location = progressBar.get(SpatialProperty.class).getLocalTranslation();
            slider.get(SpatialProperty.class).setLocalTranslation(location);
            slider.get(ChildrenProperty.class).attachChild(children);

            slider.set(PercentProperty.class, progressBar.get(PercentProperty.class));
            slider.set(PercentConverter.class, progressBar.get(PercentConverter.class));
            slider.get(PercentProperty.class).addPropertyListener((oldValue, newValue) -> {
                float percent = newValue;
                PercentConverter converter = slider.get(PercentConverter.class);
                float x = converter.getX(percent);
                float y = converter.getY(percent);
                AABB box = button.get(AABB.class);
                button.move(x - box.getXCenter(), y - box.getYCenter());
            });
            slider.get(PercentProperty.class).setPercent(0);

            return slider;
        }

        if (type.equals(ScrollBar)) {
            UIComponent scrollBar = new UINode(name);
            UIComponent scroll = Helper.find(children, SCROLL);

            float[] locInfo = Helper.getMinXYandMaxXY(children);
            scrollBar.move(locInfo[0], locInfo[1]);
            scrollBar.get(ChildrenProperty.class).attachChild(children);

            AABB scrollBarBox = scrollBar.get(AABB.class);
            float minX = scrollBarBox.getXLeft();
            float maxX = scrollBarBox.getXRight();
            float minY = scrollBarBox.getYBottom();
            float maxY = scrollBarBox.getYTop();
            if (scrollBarBox.getWidth() > scrollBarBox.getHeight()) {//水平
                float interval;
                if (scroll.get(AABB.class).getXCenter() < scrollBarBox.getXCenter()) {
                    interval = scroll.get(AABB.class).getXLeft() - scrollBarBox.getXLeft();
                } else {
                    interval = scrollBarBox.getXRight() - scroll.get(AABB.class).getXRight();
                }
                minX = scrollBarBox.getXLeft() + interval;
                maxX = scrollBarBox.getXRight() - interval;
            } else {//垂直
                float interval;
                if (scroll.get(AABB.class).getYCenter() > scrollBarBox.getYCenter()) {
                    interval = scrollBarBox.getYTop() - scroll.get(AABB.class).getYTop();
                } else {
                    interval = scroll.get(AABB.class).getYBottom() - scrollBarBox.getYBottom();
                }
                minY = scrollBarBox.getYBottom() + interval;
                maxY = scrollBarBox.getYTop() - interval;
            }
            ScrollConverter scrollConverter = new ScrollConverter(minX, minY, maxX, maxY);
            scrollBar.set(ScrollConverter.class, scrollConverter);

            return scrollBar;
        }

        if (type.equals(Dialog)) {
            UIComponent dialog = new UINode(name);

            float[] locInfo = Helper.getMinXYandMaxXY(children);
            dialog.move(locInfo[0], locInfo[1]);
            dialog.get(ChildrenProperty.class).attachChild(children);

            return dialog;
        }

        if (type.equals(Panel)) {
            float[] side;{
                UIComponent background = Helper.find(children, BACKGROUND);
                if (background != null) side = Helper.getMinXYandMaxXY(background);
                else side = Helper.getMinXYandMaxXY(children);
            }
            UIComponent panel = new Panel(name, (int) (side[2] - side[0]), (int) (side[3] - side[1]));
            panel.move(side[0], side[1]);

            ChildrenProperty childrenProperty = panel.get(ChildrenProperty.class);
            childrenProperty.attachChild(children);

            return panel;
        }

        throw new UnsupportedOperationException("该工厂无法以组件参数生产" + type + "类型的组件");
    }

    private static class Helper {

        private static int indexOfSameSizeFrom0To(LayerImageData[] data) {
            if (data.length < 2) return 0;
            LayerImageData data0 = data[0];
            int i = 1;
            for (; i < data.length; i++) {
                if (!isSameSize(data0, data[i])) break;
            }
            return i - 1;
        }

        private static boolean isSameSize(LayerImageData data1, LayerImageData data2) {
            if (data1 == data2) return true;
            if (data1.getTop() != data2.getTop()) return false;
            if (data1.getBottom() != data2.getBottom()) return false;
            if (data1.getLeft() != data2.getLeft()) return false;
            if (data1.getRight() != data2.getRight()) return false;
            return true;
        }

        private static boolean mayBeNinePatchImage(int index, LayerImageData[] data) {
            if (data[index].getImg() == null) return false;
            if (index == 0 && data.length == 1) return true;
            if (index - 1 < 0) return false;
            LayerImageData thisData = data[index];
            LayerImageData nextData = data[index - 1];
            boolean existEdge = false;
            int value = thisData.getTop() - nextData.getTop();
            if (value != 0 && value != 1) return false;
            if (value == 1) existEdge = true;
            value = thisData.getBottom() - nextData.getBottom();
            if (value != 0 && value != -1) return false;
            if (value == -1) existEdge = true;
            value = thisData.getLeft() - nextData.getLeft();
            if (value != 0 && value != -1) return false;
            if (value == -1) existEdge = true;
            value = thisData.getRight() - nextData.getRight();
            if (value != 0 && value != 1) return false;
            if (value == 1) existEdge = true;
            return existEdge;
        }

        private static LayerImageData[] tryToCutNinePatchImageAndCheck(int index, LayerImageData[] data, boolean[][] edge) {
            Image src = ImageHandler.cutNinePatchImage(data[index].getImg(), edge);
            if (src == null) return data;
            LayerImageData srcData = new LayerImageData(data[index]);
            srcData.setImg(src);
            if (edge[0] != null) srcData.setTop(srcData.getTop() - 1);
            if (edge[1] != null) srcData.setBottom(srcData.getBottom() + 1);
            if (edge[2] != null) srcData.setLeft(srcData.getLeft() + 1);
            if (edge[3] != null) srcData.setRight(srcData.getRight() - 1);
            if (data.length == 1) return new LayerImageData[]{srcData};
            LayerImageData neiData = index == 0 ? data[index + 1] : data[index - 1];
            if (isSameSize(srcData, neiData)) {
                LayerImageData[] newData = new LayerImageData[data.length];
                System.arraycopy(data, 0, newData, 0, newData.length);
                newData[index] = srcData;
                return newData;
            }
            edge[0] = null;
            edge[1] = null;
            edge[2] = null;
            edge[3] = null;
            return data;
        }

        private static boolean isEmptyArray(boolean[][] edge) {
            for (boolean[] subArray : edge) {
                if (subArray != null) return false;
            }
            return true;
        }

        private static boolean isTheLastOneInOthers(LayerImageData[] data) {
            LayerImageData theLastOne = data[data.length - 1];
            for (int i = data.length - 2; i >= 0; i--) {
                LayerImageData datai = data[i];
                if (theLastOne.getTop() - datai.getTop() > -1) return false;
                if (theLastOne.getBottom() - datai.getBottom() < 1) return false;
                if (theLastOne.getLeft() - datai.getLeft() < 1) return false;
                if (theLastOne.getRight() - datai.getRight() > -1) return false;
            }
            return true;
        }

        private static LayerImageData[] combineTextAndButton(LayerImageData[] data) {
            LayerImageData[] newData = new LayerImageData[data.length - 1];
            LayerImageData textData = data[data.length - 1];
            for (int i = 0; i < newData.length; i++) {
                LayerImageData buttonData = new LayerImageData(data[i]);
                Image img = buttonData.getImg();
                if (img == null) img = ImageHandler.createEmptyImage(buttonData.getWidth(), buttonData.getHeight());
                else img = ImageHandler.clone(img);
                ImageHandler.drawCombine(img, textData.getImg(), textData.getLeft() - buttonData.getLeft(), textData.getBottom() - buttonData.getBottom());
                buttonData.setImg(img);
                newData[i] = buttonData;
            }
            return newData;
        }

        private static LayerImageData[] combineToOneBigImage(LayerImageData[] data) {
            LayerImageData bigImgData = new LayerImageData(data[0]);
            for (LayerImageData layerImageData : data) {
                if (layerImageData.getTop() > bigImgData.getTop()) bigImgData.setTop(layerImageData.getTop());
                if (layerImageData.getBottom() < bigImgData.getBottom())
                    bigImgData.setBottom(layerImageData.getBottom());
                if (layerImageData.getLeft() < bigImgData.getLeft()) bigImgData.setLeft(layerImageData.getLeft());
                if (layerImageData.getRight() > bigImgData.getRight()) bigImgData.setRight(layerImageData.getRight());
            }
            Image bigImg = ImageHandler.createEmptyImage(bigImgData.getWidth(), bigImgData.getHeight());
            for (LayerImageData datai : data) {
                if (datai.getImg() == null) continue;
                ImageHandler.drawCombine(bigImg, datai.getImg(), datai.getLeft() - bigImgData.getLeft(), datai.getBottom() - bigImgData.getBottom());
            }
            bigImgData.setImg(bigImg);
            return new LayerImageData[]{bigImgData};
        }

        private static UIComponent find(UIComponent[] list, String... keywords) {
            String type = keywords[keywords.length - 1];
            for (UIComponent component : list) {
                String name = (String) component.get(UIComponent.NAME);
                if (!type.isEmpty()) {
                    if (!CodeParser.endsWith(name, type)) continue;
                    if (keywords.length == 1) return component;
                }
                boolean contains = true;
                for (int i = keywords.length - 2; i >= 0; i--) {
                    if (!CodeParser.contains(name, keywords[i])) {
                        contains = false;
                        break;
                    }
                }
                if (contains) return component;
            }
            return null;
        }

        private static float[] getMinXYandMaxXY(UIComponent... children) {
            float minX = Integer.MAX_VALUE;
            float maxX = 0;
            float minY = minX;
            float maxY = minX;
            float x, y;
            for (UIComponent component : children) {
                AABB box = component.get(AABB.class);
                x = box.getXLeft();
                y = box.getYBottom();
                if (x < minX) minX = x;
                if (y < minY) minY = y;
                x = box.getXRight();
                y = box.getYTop();
                if (maxX < x) maxX = x;
                if (maxY < y) maxY = y;
            }
            return new float[]{minX, minY, maxX, maxY};
        }

    }

}