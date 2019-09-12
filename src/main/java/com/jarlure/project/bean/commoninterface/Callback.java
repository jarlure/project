package com.jarlure.project.bean.commoninterface;

public interface Callback<T> {

    /**
     * 回调函数。例如当任务执行完毕后调用回调函数通知任务完成并将任务返回值作为参数传进回调函数中。
     *
     * @param obj   返回对象。通常是任务执行完毕后的返回值
     * @param extra 额外信息
     */
    void onDone(T obj, Object... extra);

    /**
     * 如果任务执行出错，可以调用该函数通知任务失败并将状态吗作为参数传进回调函数中。
     *
     * @param statuscode 错误状态码
     */
    default void onFailed(int statuscode) {
    }

}
