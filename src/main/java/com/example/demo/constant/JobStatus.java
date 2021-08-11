package com.example.demo.constant;

/**
 * @author yunyou
 * 任务状态
 */
public enum JobStatus {

    /**
     * 可执行状态 等待消费
     */
    READY,

    /**
     * 不可等待状态 等待时钟周期
     */
    DELAY,
    /**
     * 已被消费者读取，但还未得到消费者的响应
     */
    RESERVED,
    /**
     * 已被消费完成或者已被删除
     */
    DELETED;
}