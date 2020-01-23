package com.jarlure.project.bean;

public interface Record {

    /**
     * 撤销
     *
     * @return 成功返回true；否则返回false
     */
    boolean undo();

    /**
     * 重做
     *
     * @return 成功返回true；否则返回false
     */
    boolean redo();

    /**
     * 释放资源
     */
    void release();

}
