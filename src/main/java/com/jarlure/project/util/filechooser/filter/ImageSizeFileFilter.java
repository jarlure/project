package com.jarlure.project.util.filechooser.filter;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

public class ImageSizeFileFilter extends FileFilter {

    private int width, height;

    /**
     * 用于swing文件选择器的文件过滤器。该过滤器会过滤掉尺寸不是给定宽高的png图片
     *
     * @param width  给定宽度
     * @param height 给定高度
     */
    public ImageSizeFileFilter(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public boolean accept(File f) {
        if (f.isDirectory()) return true;
        int index = f.getName().lastIndexOf('.');
        if (index == -1) return true;
        String format = f.getName().substring(index);
        Iterator<ImageReader> readers;
        switch (format) {
            case ".png":
                readers = ImageIO.getImageReadersByFormatName("png");
                break;
            case ".jpg":
                readers = ImageIO.getImageReadersByFormatName("jpg");
                break;
            case ".bmp":
                readers = ImageIO.getImageReadersByFormatName("bmp");
                break;
            default:
                return true;
        }
        return isSameSize(f, readers);
    }

    @Override
    public String getDescription() {
        return null;
    }

    private boolean isSameSize(File f, Iterator<ImageReader> readers) {
        try {
            ImageReader reader = readers.next();
            ImageInputStream iis = ImageIO.createImageInputStream(f);
            reader.setInput(iis, true);
            int imgWidth = reader.getWidth(0);
            int imgHeight = reader.getHeight(0);
            if (width == imgWidth && height == imgHeight) {
                return true;
            }
        } catch (IOException e) {
        }
        return false;
    }
}
