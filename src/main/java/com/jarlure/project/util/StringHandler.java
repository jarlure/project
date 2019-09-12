package com.jarlure.project.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

public class StringHandler {

    /**
     * 统计输入字符串的字节数。中文字符为双字节，其他字符为单字节
     *
     * @param str
     * @return
     */
    public static int getNumberOfBytes(String str) {
        int total = 0;
        for (char c : str.toCharArray()) {
            //判断是单字节还是双字节
            total += getNumberOfBytes(c);
        }
        return total;
    }

    public static int getNumberOfBytes(char c) {
        return c > 0x80 ? 2 : 1;
    }

    /**
     * 将传入对象的所有非静态成员变量及其当前值转化为一行字符串。
     *
     * @param obj 传入对象。例如：Vector3f.ZERO
     * @return 一行字符串。例如："Vector3f:x=0.0,y=0.0,z=0.0"
     */
    public static String toString(Object obj) {
        Class clazz = obj.getClass();
        StringBuilder str = new StringBuilder();
        str.append(clazz.getSimpleName()).append(':');
        try {
            for (Field field : clazz.getDeclaredFields()) {
                boolean isAccessible = field.isAccessible();
                field.setAccessible(true);
                if (Modifier.isStatic(field.getModifiers())) continue;//过滤掉静态成员变量
                String name = field.getName();
                Object value = field.get(obj);
                str.append(name).append('=').append(value).append(',');
                field.setAccessible(isAccessible);
            }
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return str.substring(0, str.length() - 1);
    }


    public static String toString(String[] text, String... prefix_interval_suffix) {
        return toString(Arrays.asList(text), prefix_interval_suffix);
    }

    /**
     * 合并文本数组
     *
     * @param text                   文本数组。例如：数组[0]="name=背景"、数组[1]="type=图片"、数组[2]="link=null"
     * @param prefix_interval_suffix 前缀、间隔填充、后缀。例如："组件（","，","）"
     * @return 字符串。例如："组件（name=背景，type=图片，link=null）"
     */
    public static String toString(List<String> text, String... prefix_interval_suffix) {
        if (text.isEmpty()) return "";
        String prefix = "";
        String interval = "";
        String suffix = "";
        if (prefix_interval_suffix.length == 1) interval = prefix_interval_suffix[0];
        else if (prefix_interval_suffix.length >= 3) {
            prefix = prefix_interval_suffix[0];
            interval = prefix_interval_suffix[1];
            suffix = prefix_interval_suffix[2];
        }

        StringBuilder result = new StringBuilder(prefix);
        result.append(prefix).append(text.get(0));
        for (int i = 1, len = text.size(); i < len; i++) {
            result.append(interval).append(text.get(i));
        }
        result.append(suffix);
        return result.toString();
    }

    /**
     * 给数组中的每个字符串添加前缀（和后缀）
     *
     * @param text          字符串数组。例如：数组[0]="name"、数组[1]="address";
     * @param prefix_suffix 前缀（和后缀）。如果长度为1，则表示为前缀；如果长度为2，则表示前缀和后缀。例如："String "，";"
     * @return 数组[0]="String name;"、数组[1]="String address;";
     */
    public static String[] connect(String[] text, String... prefix_suffix) {
        String prefix = prefix_suffix.length == 0 ? "" : prefix_suffix[0];
        String suffix = prefix_suffix.length < 2 ? "" : prefix_suffix[1];
        String[] array = new String[text.length];
        for (int i = 0; i < array.length; i++) {
            String str = text[i];
            if (str == null) str = "";
            if (prefix_suffix.length > 0) {
                str = prefix + str + suffix;
            }
            array[i] = str;
        }
        return array;
    }

    /**
     * 连接数组A和数组B，并给数组中的每个字符串添加前缀、间隔填充、后缀。
     *
     * @param textA                  字符串数组A。如果数组A长度小于数组B长度，可以认为数组A的长度无限大，其中没有设置值
     *                               的部分为空字符串：""
     * @param textB                  字符传数组B。如果数组B长度小于数组A长度，可以认为数组B的长度无限大，其中没有设置值
     *                               的部分为空字符串：""
     * @param prefix_interval_suffix 前缀、间隔填充、后缀。当长度为1时表示只有间隔填充。
     * @return  连接后的新数组C。数组C的长度是数组A和数组B中长度较长的那个
     */
    public static String[] connect(String[] textA, String[] textB, String... prefix_interval_suffix) {
        String prefix = "";
        String interval = "";
        String suffix = "";
        if (prefix_interval_suffix.length == 1) interval = prefix_interval_suffix[0];
        else if (prefix_interval_suffix.length >= 3) {
            prefix = prefix_interval_suffix[0];
            interval = prefix_interval_suffix[1];
            suffix = prefix_interval_suffix[2];
        }
        String[] array = new String[Math.max(textA.length, textB.length)];
        for (int i = 0; i < array.length; i++) {
            String strA = i < textA.length ? textA[i] : "";
            String strB = i < textB.length ? textB[i] : "";
            StringBuilder builder = new StringBuilder(prefix.length() + strA.length() + interval.length() + strB.length() + suffix.length());
            builder.append(prefix).append(strA).append(interval).append(strB).append(suffix);
            array[i] = builder.toString();
        }
        return array;
    }

}