package com.example.demo.container;

import com.alibaba.fastjson.JSON;
import com.example.demo.constant.JobStatus;
import com.example.demo.entity.Job;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * @author Jashin
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class JobPool {

    private final RedisTemplate redisTemplate;

    private final String NAME = "job.pool";

    /**
     * Job Poll存放的Job元信息，只需要K/V形式的结构即可。key为job id，value为job struct。
     * 通过BoundHashOperations设置值
     */
    private BoundHashOperations getPool(){
        BoundHashOperations ops = redisTemplate.boundHashOps(NAME);
        return ops;
    }

    /**
     * 添加任务
     * @param job
     */
    public void addJob(Job job){
        log.info("任务池添加任务:{}", JSON.toJSONString(job));
        getPool().put(job.getId(),job);
    }

    /**
     * 获得任务
     * @param jobId
     * @return
     */
    public Job getJob(Long jobId){
        Object o = getPool().get(jobId);
        if (o instanceof Job){
            return (Job)o;
        }
        return null;
    }


    /**
     * 移除任务
     * @param jobId
     */
    public void removeDelayJob(Long jobId){
        log.info("任务池移除任务,{}",jobId);
        Job job = getJob(jobId);
        Optional.ofNullable(job)
                .ifPresent(job1 -> job1.setStatus(JobStatus.DELETED));
        //移除任务
        getPool().delete(jobId);
    }

}
