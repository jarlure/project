package com.jarlure.project.bean.entitycomponent;

import com.simsilica.es.EntityComponent;

public class Task implements EntityComponent {

    private Runnable task;

    public Task(Runnable task){
        this.task=task;
    }

    public Runnable getTask() {
        return task;
    }

}
