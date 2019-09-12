package com.jarlure.project.util.file.txt;

import com.jarlure.project.util.file.FileReader;

import java.io.File;
import java.nio.MappedByteBuffer;

public class TextFileReader {

    private enum UnicodeType {
        NULL("NULL"), UTF_8("UTF-8"), GBK("GBK");

        private String name;

        UnicodeType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

    }

    private FileReader reader;
    private UnicodeType unicode = UnicodeType.NULL;

    /**
     * 文本文件读取器。支持java、txt、json格式。
     *
     * @param file 文本文件
     */
    public TextFileReader(File file) {
        reader = new FileReader(file);
        String name = file.getName();
        int dotIndex = name.lastIndexOf(".");
        String extension = name.substring(dotIndex);
        switch (extension) {
            case ".java":
                unicode = UnicodeType.UTF_8;
                break;
            case ".txt":
                unicode = UnicodeType.GBK;
                break;
            case ".json":
                unicode = UnicodeType.UTF_8;
                break;
        }
    }

    /**
     * 读取一行文本。该方法会自动打开输入流。
     *
     * @return 一行文本
     */
    public String readLine() {
        if (!reader.open()) return null;
        MappedByteBuffer buffer = reader.getBufferPointer();
        try {
            byte[] byteArray = readLine(buffer);
            if (byteArray == null) return null;
            String result = new String(byteArray, unicode.getName());
            return result;
        } catch (Exception e) {
        }
        return null;
    }

    public int getBufferPosition() {
        if (reader.open()) return reader.getBufferPointer().position();
        return -1;
    }

    public void setBufferPosition(int position) {
        if (reader.open()) reader.getBufferPointer().position(position);
    }

    /**
     * 关闭输入流
     */
    public void close() {
        reader.close();
    }

    private byte[] readLine(MappedByteBuffer buffer) {
        if (buffer.position() >= buffer.limit()) return null;
        int startPosition = buffer.position();
        byte b = 0;
        boolean existR = false;
        while (true) {
            if (buffer.position() >= buffer.limit()) break;
            b = buffer.get();
            if (b == '\r') {
                b = buffer.get();
                if (b == '\n') {
                    existR = true;
                    break;
                }
            }
            if (b == '\n') break;
        }
        int endPosition = buffer.position();
        int length = endPosition - startPosition;
        if (b == '\n') length--;
        if (existR) length--;
        byte[] byteArray = new byte[length];
        buffer.position(startPosition);
        buffer.get(byteArray);
        buffer.position(endPosition);
        return byteArray;
    }

}

