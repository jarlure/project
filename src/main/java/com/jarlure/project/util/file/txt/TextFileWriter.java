package com.jarlure.project.util.file.txt;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

public class TextFileWriter {

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

    private File file;
    private BufferedWriter writer;
    private UnicodeType unicode = UnicodeType.NULL;

    /**
     * 文本文件生成器。
     *
     * @param file 生成文件的指定路径
     */
    public TextFileWriter(File file) {
        if (file.exists()) throw new IllegalStateException("文件" + file + "已存在！");
        this.file = file;
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
     * 打开输出流。如果该输出流已经打开，则直接返回true
     *
     * @return true如果输出流已经打开。false如果无法打开输出流。
     */
    private boolean open() {
        if (writer != null) return true;
        try {
            OutputStreamWriter pw;
            pw = new OutputStreamWriter(new FileOutputStream(file), unicode.getName());
            writer = new BufferedWriter(pw);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 写一行文本
     *
     * @param line 一行文本
     */
    public void writeLine(String line) {
        if (!open()) return;
        try {
            writer.append(line);
            writer.newLine();
        } catch (IOException e) {
        }
    }

    /**
     * 写一行文本。
     *
     * @param line    一行文本
     * @param unicode 字符编码
     */
    public void writeLine(String line, String unicode) {
        if (!open()) return;
        try {
            line = new String(line.getBytes(), unicode);
        } catch (UnsupportedEncodingException e) {
        }
        try {
            writer.append(line);
            writer.newLine();
        } catch (IOException e) {
        }
    }

    /**
     * 关闭输出流
     */
    public void close() {
        if (writer == null) return;
        try {
            writer.flush();
            writer.close();
            writer = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}