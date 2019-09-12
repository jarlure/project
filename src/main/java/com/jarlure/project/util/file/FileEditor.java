package com.jarlure.project.util.file;

import java.io.*;
import java.nio.channels.FileChannel;

public class FileEditor extends FileReader {

    public FileEditor(File file) {
        super(file);
    }

    @Override
    public boolean open() {
        if (isOpen()) return true;
        try {
            raf = new RandomAccessFile(file, "rw");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            raf = null;
            return false;
        }
        try {
            fc = raf.getChannel();
            //TODO:插入数据会使得尺寸变大，这里的size就不正确了
            long size = fc.size();
            bufferPointer = fc.map(FileChannel.MapMode.READ_WRITE, 0, size);
        } catch (IOException e) {
            e.printStackTrace();
            close();
            return false;
        }
        return true;
    }

    /**
     * 插入数据
     *
     * @param position 插入位置
     * @param content  插入内容
     */
    public void insert(long position, byte[] content) {
        FileOutputStream out = null;
        FileInputStream in = null;
        try {
            File tmp = File.createTempFile("tmp", null);
            tmp.deleteOnExit();
            out = new FileOutputStream(tmp);
            in = new FileInputStream(tmp);
            raf.seek(position);
            byte[] bbuf = new byte[64];
            int hasRead;
            while ((hasRead = raf.read(bbuf)) > 0) {
                out.write(bbuf, 0, hasRead);
            }
            raf.seek(position);
            raf.write(content);
            while ((hasRead = in.read(bbuf)) > 0) {
                raf.write(bbuf, 0, hasRead);
            }
            raf.seek(0);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) out.close();
                if (in != null) in.close();
            } catch (IOException ee) {
                ee.printStackTrace();
            }
        }
    }

    /**
     * 替换数据
     *
     * @param startPosition 替换数据的起始位置（包含）
     * @param endPosition   替换数据的结束位置（不包含）
     * @param content       替换内容
     */
    public void replace(long startPosition, long endPosition, byte[] content) {
        if (!open()) return;
        FileOutputStream out = null;
        FileInputStream in = null;
        try {
            File tmp = File.createTempFile("tmp", null);
            tmp.deleteOnExit();
            out = new FileOutputStream(tmp);
            in = new FileInputStream(tmp);
            raf.seek(endPosition);
            byte[] bbuf = new byte[64];
            int hasRead;
            while ((hasRead = raf.read(bbuf)) > 0) {
                out.write(bbuf, 0, hasRead);
            }
            raf.seek(startPosition);
            raf.write(content);
            while ((hasRead = in.read(bbuf)) > 0) {
                raf.write(bbuf, 0, hasRead);
            }
            raf.seek(0);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) out.close();
                if (in != null) in.close();
            } catch (IOException ee) {
                ee.printStackTrace();
            }
        }
    }

}