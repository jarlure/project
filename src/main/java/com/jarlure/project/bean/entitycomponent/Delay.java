package com.jarlure.project.bean.entitycomponent;

import com.jarlure.project.lambda.VoidFunction;
import com.simsilica.es.EntityComponent;

public class Delay implements EntityComponent {

    private float time;
    private VoidFunction callback;

    public Delay(VoidFunction callback) {
        this.callback = callback;
    }

    /**
     * 延时标记。当实体被该标记标记时，DelayState将会对其进行倒计时。若该标记中的time<0，那么DelayState将会调用该标记携
     * 带的Callback的回调函数，并删除关联该标记的实体。
     *
     * @param time     方法延时。DelayState每次都会检查并减少该值。当该值<0时执行回调函数并删除该标记关联的实体
     * @param callback 回调函数
     */
    public Delay(float time, VoidFunction callback) {
        this.callback = callback;
        this.time = time;
    }

    public VoidFunction getCallback() {
        return callback;
    }

    public float getTime() {
        return time;
    }

    public void setTime(float time) {
        this.time = time;
    }

}
