package com.jarlure.project.util;

import com.jarlure.project.lambda.BooleanFunction3Obj;
import com.jarlure.project.lambda.ObjectFunction3Obj;
import com.jarlure.ui.util.ImageHandler;
import com.jme3.export.InputCapsule;
import com.jme3.export.OutputCapsule;
import com.jme3.export.Savable;
import com.jme3.export.binary.BinaryExporter;
import com.jme3.texture.Image;
import com.jme3.util.IntMap;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SavableHelper {

    private static final Logger LOG = Logger.getLogger(SavableHelper.class.getName());
    private static final String KEY = ".K";
    private static final String VALUE = ".V";
    private static final String SIZE = ".SZ";

    /**
     * 保存为j3o文件
     *
     * @param j3oFile 保存到该文件中
     * @param data    数据
     */
    public static void saveAsJ3OData(File j3oFile, Savable data) {
        BinaryExporter exporter = BinaryExporter.getInstance();
        if (j3oFile.exists()) j3oFile.delete();
        try {
            exporter.save(data, j3oFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 读取数据并设置进对象实例中。用法详见write()方法。
     *
     * @param obj         对象实例
     * @param capsule     输入流
     * @param interceptor 拦截器。你可以在拦截器中拦截你不想自动设置的值。拦截器能够得到的参数依次为：变量值（Object）、
     *                    成员变量名（String）、成员变量字段（Field）
     */
    public static void read(Object obj, InputCapsule capsule, BooleanFunction3Obj<Object,String,Field> interceptor) {
        if (obj == null) return;
        for (Field field : obj.getClass().getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) continue;//过滤掉静态成员变量
            if (Modifier.isFinal(field.getModifiers())) continue;//过滤掉成员常量
            String varyName = field.getName();
            Object varyValue = read(capsule, varyName);
            if (varyValue == null) continue;
            boolean accessible = field.isAccessible();
            field.setAccessible(true);
            if (interceptor == null || interceptor.apply(varyValue, varyName, field)) {
                try {
                    field.set(obj, varyValue);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            field.setAccessible(accessible);
        }
    }

    /**
     * 从对象实例中获取成员变量并保存。该方法是一种自动化保存数据的方法。即使没有实现Savable接口，也能够通过代理类保存其
     * 成员变量。
     *
     * @param obj     对象实例
     * @param capsule 输出流
     * @param filter  过滤器。你可以在过滤器中过滤你不想自动保存的值。过滤器能够得到的参数依次为：变量值（Object）、
     *                成员变量名（String）、成员变量字段（Field）
     */
    public static void write(Object obj, OutputCapsule capsule, ObjectFunction3Obj<Object,String,Field,Object> filter) {
        if (obj == null) return;
        for (Field field : obj.getClass().getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) continue;//过滤掉静态成员变量
            if (Modifier.isFinal(field.getModifiers())) continue;//过滤掉成员常量
            boolean accessible = field.isAccessible();
            field.setAccessible(true);
            String varyName = field.getName();
            Object varyValue = null;
            try {
                varyValue = field.get(obj);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            field.setAccessible(accessible);
            if (filter != null) varyValue = filter.apply(varyValue, varyName, field);
            if (varyValue == null) continue;

            write(capsule, varyName, varyValue);
        }
    }

    private static Object read(InputCapsule capsule, String name) {
        try {
            String clazzName = capsule.readString(name, null);
            if (clazzName == null) return null;
            Class clazz = getClassForLoad(clazzName);
            if (clazz == null) return null;
            //Array
            if (clazz.isArray()) {
                if (clazz.equals(boolean[].class)) return capsule.readBooleanArray(name + VALUE, null);
                if (clazz.equals(byte[].class)) return capsule.readByteArray(name + VALUE, null);
                if (clazz.equals(short[].class)) return capsule.readShortArray(name + VALUE, null);
                if (clazz.equals(int[].class)) return capsule.readIntArray(name + VALUE, null);
                if (clazz.equals(long[].class)) return capsule.readLongArray(name + VALUE, null);
                if (clazz.equals(float[].class)) return capsule.readFloatArray(name + VALUE, null);
                if (clazz.equals(double[].class)) return capsule.readDoubleArray(name + VALUE, null);
                if (clazz.equals(char[].class)) return capsule.readString(name + VALUE, "").toCharArray();
                if (clazz.equals(String[].class)) return capsule.readShortArray(name + VALUE, null);
                if (clazz.equals(Savable[].class)) return capsule.readSavableArray(name + VALUE, null);
                List list = new ArrayList();
                while (true) {
                    StringBuilder builder = new StringBuilder(name.length() + VALUE.length() + 3);
                    Object value = read(capsule, builder.append(name).append(VALUE).append(list.size()).toString());
                    if (value == null) break;
                    list.add(value);
                }
                if (list.isEmpty()) return null;
                Object array = Array.newInstance(list.get(0).getClass(), list.size());
                for (int i = 0; i < list.size(); i++) {
                    Array.set(array, i, list.get(i));
                }
                return array;
            }
            //List
            else if (List.class.isAssignableFrom(clazz)) {
                List list = (List) clazz.newInstance();
                Savable[] valueArray = capsule.readSavableArray(name + VALUE, null);
                for (int i = 0; i < valueArray.length; i++) {
                    Object value = valueArray[i];
                    if (value == null) {
                        StringBuilder builder = new StringBuilder(name.length() + VALUE.length() + 3);
                        value = read(capsule, builder.append(name).append(VALUE).append(list.size()).toString());
                    }
                    list.add(value);
                }
                return list;
            }
            //Map
            else if (Map.class.isAssignableFrom(clazz)) {
                Map map = (Map) clazz.newInstance();
                while (true) {
                    StringBuilder builder = new StringBuilder(name.length() + KEY.length() + 3);
                    Object key = read(capsule, builder.append(name).append(KEY).append(map.size()).toString());
                    if (key == null) break;
                    builder = new StringBuilder(name.length() + VALUE.length() + 3);
                    Object value = read(capsule, builder.append(name).append(VALUE).append(map.size()).toString());
                    if (value == null) break;
                    map.put(key, value);
                }
                return map;
            }
            //IntMap
            else if (IntMap.class.isAssignableFrom(clazz)) {
                IntMap map = (IntMap) clazz.newInstance();
                while (true) {
                    StringBuilder builder = new StringBuilder(name.length() + VALUE.length() + 3);
                    Object value = read(capsule, builder.append(name).append(VALUE).append(map.size()).toString());
                    if (value == null) break;
                    builder = new StringBuilder(name.length() + KEY.length() + 3);
                    int key = capsule.readInt(builder.append(name).append(KEY).append(map.size()).toString(), 0);
                    map.put(key, value);
                }
                return map;
            }
            //Image
            else if (Image.class.isAssignableFrom(clazz)) {
                byte[] imgValue = capsule.readByteArray(name + VALUE, null);
                if (imgValue == null) return null;
                int[] size = capsule.readIntArray(name + SIZE, null);
                if (size == null) return null;
                return ImageHandler.decompress(imgValue, size[0], size[1]);
            }
            //Savable
            else if (Savable.class.isAssignableFrom(clazz)) {
                return capsule.readSavable(name + VALUE, null);
            }
            //String
            else if (String.class.isAssignableFrom(clazz)) {
                return capsule.readString(name + VALUE, null);
            }
            //Class
            else if (Class.class.isAssignableFrom(clazz)) {
                return clazz;
            }
            //boolean
            else if (Boolean.class.isAssignableFrom(clazz)) {
                return capsule.readBoolean(name + VALUE, false);
            }
            //short
            else if (Short.class.isAssignableFrom(clazz)) {
                return capsule.readShort(name + VALUE, (short) 0);
            }
            //int
            else if (Integer.class.isAssignableFrom(clazz)) {
//                return Integer.parseInt(capsule.readString(name+VALUE,"0"));
                return capsule.readInt(name + VALUE, 0);
            }
            //long
            else if (Long.class.isAssignableFrom(clazz)) {
                return capsule.readLong(name + VALUE, 0);
            }
            //float
            else if (Float.class.isAssignableFrom(clazz)) {
                return capsule.readFloat(name + VALUE, 0);
            }
            //double
            else if (Double.class.isAssignableFrom(clazz)) {
                return capsule.readDouble(name + VALUE, 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void write(OutputCapsule capsule, String name, Object data) {
        try {
            if (data == null) return;
            capsule.write(getClassNameForSave(data.getClass()), name, null);
            //Array
            if (data.getClass().isArray()) {
                if (data instanceof boolean[]) capsule.write((boolean[]) data, name + VALUE, null);
                else if (data instanceof byte[]) capsule.write((byte[]) data, name + VALUE, null);
                else if (data instanceof short[]) capsule.write((short[]) data, name + VALUE, null);
                else if (data instanceof int[]) capsule.write((int[]) data, name + VALUE, null);
                else if (data instanceof long[]) capsule.write((long[]) data, name + VALUE, null);
                else if (data instanceof float[]) capsule.write((float[]) data, name + VALUE, null);
                else if (data instanceof double[]) capsule.write((double[]) data, name + VALUE, null);
                else if (data instanceof char[]) capsule.write(new String((char[]) data), name + VALUE, null);
                else if (data instanceof String[]) capsule.write((String[]) data, name + VALUE, null);
                else if (data instanceof Savable[]) capsule.write((Savable[]) data, name + VALUE, null);
                else {
                    int length = Array.getLength(data);
                    if (length > Byte.MAX_VALUE)
                        throw new UnsupportedOperationException("自定义数组长度超过了上限。请将数组手动转换为可支持的数组类型或列表类型后再试");
                    for (int i = 0; i < length; i++) {
                        StringBuilder builder = new StringBuilder(name.length() + VALUE.length() + 3);
                        write(capsule, builder.append(name).append(VALUE).append(i).toString(), Array.get(data, i));
                    }
                }
            }
            //List
            else if (data instanceof List) {
                List list = (List) data;
                Savable[] value = new Savable[list.size()];
                for (int i = 0, n = 0; i < list.size(); i++) {
                    Object child = list.get(i);
                    if (child instanceof Savable) {
                        value[i] = (Savable) child;
                    } else {
                        StringBuilder builder = new StringBuilder(name.length() + VALUE.length() + 3);
                        write(capsule, builder.append(name).append(VALUE).append(i).toString(), list.get(i));
                        n++;
                        if (n > Byte.MAX_VALUE)
                            throw new UnsupportedOperationException("数据长度超过了上限。请将数据手动转换为可支持的数组类型或列表类型后再试");
                    }
                }
                capsule.write(value, name + VALUE, null);
            }
            //Map
            else if (data instanceof Map) {
                int i = 0;
                for (Object obj : ((Map) data).entrySet()) {
                    Map.Entry entry = (Map.Entry) obj;
                    StringBuilder builder = new StringBuilder(name.length() + KEY.length() + 3);
                    write(capsule, builder.append(name).append(KEY).append(i).toString(), entry.getKey());
                    builder = new StringBuilder(name.length() + VALUE.length() + 3);
                    write(capsule, builder.append(name).append(VALUE).append(i).toString(), entry.getValue());
                    i++;
                }
                if (i > Byte.MAX_VALUE) throw new UnsupportedOperationException("数据长度超过了上限。请将数据手动转换为可支持的数组类型或列表类型后再试");
            }
            //IntMap
            else if (data instanceof IntMap) {
                int i = 0;
                Iterator<IntMap.Entry> it = ((IntMap) data).iterator();
                while (it.hasNext()) {
                    IntMap.Entry entry = it.next();
                    StringBuilder builder = new StringBuilder(name.length() + KEY.length() + 3);
                    capsule.write(entry.getKey(), builder.append(name).append(KEY).append(i).toString(), 0);
                    builder = new StringBuilder(name.length() + VALUE.length() + 3);
                    write(capsule, builder.append(name).append(VALUE).append(i).toString(), entry.getValue());
                    i++;
                }
                if (i > Byte.MAX_VALUE) throw new UnsupportedOperationException("数据长度超过了上限。请将数据手动转换为可支持的数组类型或列表类型后再试");
            }
            //Image
            else if (data instanceof Image) {
                Image img = (Image) data;
                capsule.write(new int[]{img.getWidth(), img.getHeight()}, name + SIZE, null);
                byte[] imgValue = ImageHandler.compress(img,0.7f);
                capsule.write(imgValue, name + VALUE, null);
            }
            //Savable
            else if (data instanceof Savable) {
                capsule.write((Savable) data, name + VALUE, null);
            }
            //String
            else if (data instanceof String) {
                capsule.write((String) data, name + VALUE, null);
            }
            //Class
            else if (data instanceof Class) {
                capsule.write(data.getClass().getName(), name + VALUE, null);
            }
            //boolean
            else if (data instanceof Boolean) {
                capsule.write((boolean) data, name + VALUE, false);
            }
            //byte
            else if (data instanceof Byte) {
                capsule.write((byte) data, name + VALUE, 0);
            }
            //short
            else if (data instanceof Short) {
                capsule.write((short) data, name + VALUE, 0);
            }
            //int
            else if (data instanceof Integer) {
                capsule.write((int) data, name + VALUE, 0);
            }
            //long
            else if (data instanceof Long) {
                capsule.write((long) data, name + VALUE, 0);
            }
            //float
            else if (data instanceof Float) {
                capsule.write((float) data, name + VALUE, 0);
            }
            //double
            else if (data instanceof Double) {
                capsule.write((double) data, name + VALUE, 0);
            }
            //异常报告
            else LOG.log(Level.WARNING, "无法保存名为{0}值为{1}类型为{2}的数据！", new Object[]{name, data, data.getClass()});
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Class getClassForLoad(String className) throws ClassNotFoundException {
        if (className == null) return null;
        switch (className) {
            case "b":
                return Boolean.class;
            case "B":
                return Byte.class;
            case "s":
                return Short.class;
            case "I":
                return Integer.class;
            case "L":
                return Long.class;
            case "F":
                return Float.class;
            case "D":
                return Double.class;
            case "S":
                return String.class;
            default:
                return Class.forName(className);
        }
    }

    private static String getClassNameForSave(Class clazz) {
        if (clazz == null) return null;
        if (Boolean.class.equals(clazz)) return "b";
        if (Byte.class.equals(clazz)) return "B";
        if (Short.class.equals(clazz)) return "s";
        if (Integer.class.equals(clazz)) return "I";
        if (Long.class.equals(clazz)) return "L";
        if (Float.class.equals(clazz)) return "F";
        if (Double.class.equals(clazz)) return "D";
        if (String.class.equals(clazz)) return "S";
        return clazz.getName();
    }

}
