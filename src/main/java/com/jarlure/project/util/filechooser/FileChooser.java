package com.jarlure.project.util.filechooser;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class FileChooser {

    public enum Type{
        OpenFile,SaveFile
    }

    private Type styleType;
    private String filePath, name, description;
    private Map<Class, FileFilter> filterMap = new HashMap<>();
    private FileFilter filter = new FileFilter() {
        @Override
        public boolean accept(File f) {
            for (FileFilter filter : filterMap.values()) {
                boolean accept = filter.accept(f);
                if (!accept) return false;
            }
            return true;
        }

        @Override
        public String getDescription() {
            return description;
        }
    };

    public FileChooser() {
        this(Type.OpenFile);
    }

    /**
     * swing的文件选择器。
     * @param styleType 打开/储存为。这两种类型显示的文字提示不一样。
     */
    public FileChooser(Type styleType){
        this.styleType=styleType;
        setSystemLookAndFeel();
    }

    private void setSystemLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException t) {
        }
    }

    public void open(String title, String path) {
        if (EventQueue.isDispatchThread()) {
            jOpenDispatchThread(title, path);
        } else {
            jOpenInvokeAndWait(title, path);
        }
    }

    private void jOpenDispatchThread(String title, String path) {
        configureJFileChooser(title, path);
    }

    private void jOpenInvokeAndWait(String title, String path) {
        try {
            EventQueue.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    configureJFileChooser(title, path);
                }
            });
        } catch (InterruptedException | InvocationTargetException e) {
        }
    }

    private void configureJFileChooser(String title, String path) {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle(title);
        File fdir = null;
        if (path != null) {
            fdir = new File(path);
        }
        if (fdir != null) {
            fc.setCurrentDirectory(fdir);
        }
        fc.setFileFilter(filter);
        fc.setAcceptAllFileFilterUsed(false);
        int returnVal;
        if (styleType==Type.SaveFile) returnVal=fc.showSaveDialog(new JLabel());
        else returnVal=fc.showOpenDialog(new JLabel());
        if (returnVal != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = fc.getSelectedFile();
        filePath = file.getAbsolutePath();
        name = file.getName();
    }

    public void addFilter(FileFilter filter) {
        filterMap.put(filter.getClass(), filter);
        if (description == null) setDescription(filter.getDescription());
    }

    public FileFilter getFilter(Class filterName) {
        return filterMap.get(filterName);
    }

    public void removeFilter(Class filterName){
        filterMap.remove(filterName);
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getName() {
        return name;
    }
}