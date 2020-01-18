package com.jarlure.project.bean;

import com.jme3.util.IntMap;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class Entity {

    private final IntMap<Map> itemMap;
    private Map defaultValue;

    public Entity() {
        this.itemMap = new IntMap<>();
    }

    /**
     * 表结构。概念类似于关系型数据库中的表。一张表对应N条数据。每条数据拥有N种类型和对应值。例如我们将name=张三,age=12
     * 封装到HashMap<String,Object>中，再将这个Map设置进Entity中，此时我们说Entity中有1条数据，该数据拥有两个属性：name
     * 和age，分别对应张三、12这两个值。这条数据的索引值为0。同时我们可以给数据设置默认值。例如我们设置默认sex属性为
     * male，当我们从表中获取索引值为0、属性为sex的值时就会得到male。
     *
     * @param defaultValue 存储着默认值的Map。该Map决定了默认数据的属性类型
     */
    public Entity(Map defaultValue) {
        this.itemMap = new IntMap<>();
        this.defaultValue = defaultValue;
    }

    /**
     * 设置默认值。
     *
     * @param key_value 属性-值 键值对。同时必须保证这些数据的类型是默认值的Map中可以存放的类型
     */
    public void setDefaultValue(Object... key_value) {
        if (key_value.length % 2 == 1) throw new IllegalArgumentException("必须是键值对的形式！");
        if (defaultValue == null) defaultValue = new HashMap();
        for (int i = 0; i < key_value.length; i += 2) {
            Object key = key_value[i];
            Object value = key_value[i + 1];
            defaultValue.put(key, value);
        }
    }

    /**
     * 获取给定属性的默认值
     *
     * @param key 给定的属性
     * @param <T> 任意类型
     * @return 该属性的默认值
     */
    public <T> T getDefaultValue(Object key) {
        if (defaultValue == null) return null;
        return (T) defaultValue.get(key);
    }

    /**
     * 清除所有默认值
     */
    public void clearDefaultValue() {
        defaultValue.clear();
    }

    /**
     * 当前表中总共数据条数
     *
     * @return 总共数据条数
     */
    public int size() {
        return itemMap.size();
    }

    /**
     * 判断表是否为空
     *
     * @return true如果表中没有任何数据；false如果表中至少有一条数据
     */
    public boolean isEmpty() {
        return itemMap.size() == 0;
    }

    /**
     * 添加一条数据
     *
     * @param item      存储数据的容器
     * @param key_value 属性-值 键值对。同时必须保证这些数据的类型是给定容器中可以存放的类型
     * @return 该数据的索引值
     */
    public int addItem(Map item, Object... key_value) {
        int index = size();
        addItem(index, item, key_value);
        return index;
    }

    /**
     * 添加一条数据
     *
     * @param index     该数据的索引值
     * @param item      存储数据的容器
     * @param key_value 属性-值 键值对。同时必须保证这些数据的类型是给定容器中可以存放的类型
     */
    public void addItem(int index, Map item, Object... key_value) {
        if (itemMap.containsKey(index)) throw new IllegalArgumentException("已存在索引值为" + index + "的数据！");
        setItem(index, item, key_value);
    }

    /**
     * 设置一条数据。新数据可能会覆盖掉同索引值的旧数据
     *
     * @param index     该数据的索引值
     * @param item      存储数据的容器
     * @param key_value 属性-值 键值对。同时必须保证这些数据的类型是给定容器中可以存放的类型
     */
    public void setItem(int index, Map item, Object... key_value) {
        if (key_value.length % 2 == 1) throw new IllegalArgumentException("必须是键值对的形式！");
        for (int i = 0; i < key_value.length; i += 2) {
            Object key = key_value[i];
            Object value = key_value[i + 1];
            item.put(key, value);
        }
        itemMap.put(index, item);
    }

    /**
     * 判断是否存在一条数据，它的索引值等于给定索引值
     *
     * @param index 给定索引值
     * @return true如果存在一条数据，它的索引值等于给定索引值；false如果没有任何数据的索引值等于给定索引值
     */
    public boolean existItem(int index) {
        return itemMap.containsKey(index);
    }

    /**
     * 获取所有数据的索引值
     *
     * @return 所有数据的索引值
     */
    public int[] getItems() {
        int[] index = new int[size()];
        int i = 0;
        Iterator<IntMap.Entry<Map>> it = itemMap.iterator();
        while (it.hasNext()) {
            IntMap.Entry<Map> entry = it.next();
            index[i] = entry.getKey();
            i++;
        }
        return index;
    }

    /**
     * 寻找拥有给定 属性-值 键值对的数据的索引值
     *
     * @param key_value 给定的 属性-值 键值对
     * @return 满足条件的数据的索引值。如果没有满足条件的数据，则返回-1
     */
    public int findItem(Object... key_value) {
        if (key_value.length % 2 == 1) throw new IllegalArgumentException("必须是键值对的形式！");
        Iterator<IntMap.Entry<Map>> it = itemMap.iterator();
        while (it.hasNext()) {
            IntMap.Entry<Map> entry = it.next();
            Map item = entry.getValue();
            boolean findout = true;
            for (int i = 0; i < key_value.length; i += 2) {
                Object key = key_value[i];
                Object value = key_value[i + 1];
                if (!item.containsKey(key)) {
                    findout = false;
                    break;
                }
                Object itemValue = item.get(key);
                if (itemValue == value) continue;
                if (value != null && value.equals(itemValue)) continue;
                findout = false;
                break;
            }
            if (findout) return entry.getKey();
        }
        return -1;
    }

    /**
     * 移除给定索引值对应的数据
     *
     * @param index 给定的索引值
     * @return true如果移除成功；false如果移除失败（表中没有该索引值）
     */
    public boolean removeItem(int index) {
        return itemMap.remove(index) != null;
    }

    /**
     * 设置给定索引值对应数据的给定属性的值
     *
     * @param index 给定的索引值
     * @param key   给定的属性
     * @param value 要设置的值
     */
    public void setValue(int index, Object key, Object value) {
        Map item = itemMap.get(index);
        if (item == null) return;
        item.put(key, value);
    }

    /**
     * 获取给定索引值对应数据的给定属性的值
     *
     * @param index 给定的索引值
     * @param key   给定的属性
     * @param <T>   任意类型
     * @return 给定索引值对应数据的给定属性的值
     */
    public <T> T getValue(int index, Object key) {
        Map item = itemMap.get(index);
        if (item == null) return null;
        if (defaultValue == null) {
            return (T) item.get(key);
        } else {
            return (T) item.getOrDefault(key, defaultValue.get(key));
        }
    }

    public String toString() {
        StringBuilder builder = new StringBuilder(64);
        Iterator<IntMap.Entry<Map>> it = itemMap.iterator();
        while (it.hasNext()) {
            IntMap.Entry<Map> entry = it.next();
            int i = entry.getKey();
            Map item = entry.getValue();
            builder.append("item:").append(i).append("\n");
            for (Object key : item.keySet()) {
                Object value = item.get(key);
                builder.append("  key:").append(key);
                if (value instanceof Entity) {
                    builder.append("  value:Entity").append("\n");
                } else {
                    builder.append("  value:").append(value).append("\n");
                }
            }
        }
        return builder.toString();
    }

}