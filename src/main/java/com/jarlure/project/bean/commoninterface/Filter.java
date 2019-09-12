package com.jarlure.project.bean.commoninterface;

public interface Filter<T> {

    /**
     * 过滤器的过滤方法。例如外部传进“msg=你好 state=0”，通过过滤器后得到“你好”
     *
     * @param obj   要过滤的对象
     * @param extra 额外信息
     * @return 过滤后的对象
     */
    T filter(T obj, Object... extra);

}
