package com.jarlure.project.util.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import sun.misc.Cleaner;
import sun.nio.ch.DirectBuffer;

public class FileReader {
    protected File file;
    protected RandomAccessFile raf;
    protected FileChannel fc;
    protected MappedByteBuffer bufferPointer;

    /**
     * 文件读取器
     *
     * @param file
     */
    public FileReader(File file) {
        if (!file.exists()) throw new IllegalArgumentException("文件不存在");
        if (!file.isFile()) throw new IllegalArgumentException("请确认参数file是文件");
        this.file = file;
    }

    /**
     * 打开输入流。如果该输入流已经打开，则直接返回true
     *
     * @return true如果输入流已经打开。false如果无法打开输入流。
     */
    public boolean open() {
        if (isOpen()) return true;
        try {
            raf = new RandomAccessFile(file, "r");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            raf = null;
            return false;
        }
        try {
            fc = raf.getChannel();
            long size = fc.size();
            bufferPointer = fc.map(FileChannel.MapMode.READ_ONLY, 0, size);
        } catch (IOException e) {
            e.printStackTrace();
            close();
            return false;
        }
        return true;
    }

    /**
     * 判断输入流是否已经打开
     *
     * @return true如果已经打开；false如果没有打开
     */
    public boolean isOpen() {
        return bufferPointer != null;
    }

    /**
     * 关闭输入流。
     *
     * @return true如果已经关闭输入流；false如果无法关闭
     */
    public boolean close() {
        if (bufferPointer != null) {
            Cleaner cleaner = ((DirectBuffer) bufferPointer).cleaner();
            if (cleaner != null) cleaner.clean();
            bufferPointer = null;
        }
        if (fc != null) try {
            fc.close();
            fc = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (raf != null) try {
            raf.close();
            raf = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fc == null && raf == null && bufferPointer == null;
    }

    /**
     * 获得缓冲指针。
     *
     * @return 缓冲指针。
     */
    public MappedByteBuffer getBufferPointer() {
        return bufferPointer;
    }
}
