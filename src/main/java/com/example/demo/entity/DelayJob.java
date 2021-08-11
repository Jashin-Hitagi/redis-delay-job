package com.example.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * 任务引用的对象
 * @author yunyou
 */

@Data
@AllArgsConstructor
public class DelayJob implements Serializable {

    /**
     * 延迟任务唯一标识
     */
    private long jobId;

    /**
     * 任务的执行时间
     */
    private long delayDate;

    /**
     * 任务类型(具体业务类型 任务名)
     */
    private String topic;

    public DelayJob(Job job){
        this.jobId = job.getId();
        this.delayDate = System.currentTimeMillis() + job.getDelayTime();
        this.topic = job.getTopic();
    }

    public DelayJob(Object value, Double score){
        this.jobId = Long.parseLong(String.valueOf(value));
        this.delayDate = System.currentTimeMillis() + score.longValue();
    }

}
