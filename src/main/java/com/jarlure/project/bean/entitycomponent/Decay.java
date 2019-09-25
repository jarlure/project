package com.jarlure.project.bean.entitycomponent;

import com.simsilica.es.EntityComponent;

public class Decay implements EntityComponent {

    private float time;

    public Decay() {
    }

    /**
     * 死亡标记。当实体被该标记标记时，DecayState将会对其进行倒计时。若该标记中的time<0，那么DecayState将会删除关联该标
     * 记的实体。类似于GC的垃圾回收机制。
     *
     * @param time 死亡倒计时。DecayState每次都会检查并减少该值。当该值<0时删除该标记关联的实体
     */
    public Decay(float time) {
        this.time = time;
    }

    public float getTime() {
        return time;
    }

    public void setTime(float time) {
        this.time = time;
    }

}
