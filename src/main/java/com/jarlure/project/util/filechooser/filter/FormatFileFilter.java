package com.jarlure.project.util.filechooser.filter;

import javax.swing.filechooser.FileFilter;
import java.io.File;

public class FormatFileFilter extends FileFilter {
    private String[] format;

    /**
     * 用于swing文件选择器的文件过滤器。该过滤器会过滤掉不是指定格式的文件
     * @param formatName    指定格式。例如：png
     */
    public FormatFileFilter(String... formatName) {
        format = formatName;
    }

    @Override
    public boolean accept(File f) { //设定可用的文件的后缀名
        if (f.isDirectory()) return true;
        for (String format : format) {
            if (f.getName().endsWith("." + format)) return true;
        }
        return false;
    }

    @Override
    public String getDescription() {
        StringBuilder builder = new StringBuilder();
        for (String format : format) {
            builder.append("*.").append(format).append(';');
        }
        return builder.substring(0, builder.length() - 1);
    }
}

