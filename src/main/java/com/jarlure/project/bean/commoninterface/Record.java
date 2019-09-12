package com.jarlure.project.bean.commoninterface;

public interface Record {

    /**
     * 撤销
     */
    void undo();

    /**
     * 重做
     */
    void redo();

}