package com.jarlure.project.bean.commoninterface;

public interface Interceptor<T> {

    /**
     * 拦截器的拦截方法。例如：已知密码是123456，外部传入“password=000000”会返回false；传入“password=123456”返回true
     *
     * @param obj   要检查的对象
     * @param extra 额外信息
     * @return true如果通过拦截器；false如果被拦截器拦截
     */
    boolean pass(T obj, Object... extra);

}
