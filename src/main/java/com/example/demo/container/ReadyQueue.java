package com.example.demo.container;

import com.alibaba.fastjson.JSON;
import com.example.demo.entity.DelayJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Ready Queue存放处于Ready状态的Job（这里只存放Job topic），以供消费程序消费。
 * @author yunyou
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReadyQueue {

    private final RedisTemplate redisTemplate;

    private final String NAME = "process.queue";

    private String getKey(String topic){
        return NAME + topic;
    }

    /**
     * 获得队列
     */
    private BoundListOperations getQueue(String topic){
        return redisTemplate.boundListOps(getKey(topic));
    }

    /**
     * 设置任务
     */
    public void pushJob(DelayJob delayJob){
        log.info("执行任务添加队列：{}",delayJob);
        getQueue(delayJob.getTopic()).leftPush(delayJob);
    }

    /**
     * 移除并获得任务
     */
    public DelayJob popJob(String topic){
        BoundListOperations listOperations = getQueue(topic);
        Object o = listOperations.leftPop();
        if (o instanceof DelayJob){
            log.info("执行队列取出任务：{}", JSON.toJSONString(o));
            return (DelayJob)o;
        }
        return null;
    }

}
