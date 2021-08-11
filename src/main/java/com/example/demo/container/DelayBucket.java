package com.example.demo.container;

import com.alibaba.fastjson.JSON;
import com.example.demo.entity.DelayJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.BoundZSetOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

/**
 * Delay Bucket是一组以时间为维度的有序队列，用来存放所有需要延迟的／已经被reserve的Job（这里只存放Job Id）
 * @author yunyou
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DelayBucket {


    private final RedisTemplate redisTemplate;

    private static AtomicInteger index = new AtomicInteger(0);

    @Value("${thread.size}")
    private int bucketSize;

    private final List<String> bucketNames = new ArrayList<>();

    @Bean
    public List<String> createBuckets(){
        IntStream.range(0,bucketSize)
                .forEach(a->bucketNames.add("bucket" + a));
        return bucketNames;
    }

    /**
     * 获得桶的名称
     */
    private String getThisBucketName(){
        int thisIndex = index.addAndGet(1);
        int i1 = thisIndex % bucketSize;
        return bucketNames.get(i1);
    }

    /**
     * 获得桶集合
     */
    private BoundZSetOperations getBucket(String bucketName){
        //通过BoundValueOperations设置值
        return redisTemplate.boundZSetOps(bucketName);
    }

    /**
     * 放入延时任务
     */
    private void addDelayJob(DelayJob job){
        log.info("添加延迟任务:{}", JSON.toJSONString(job));
        BoundZSetOperations bucket = getBucket(getThisBucketName());
        bucket.add(job,job.getDelayDate());
    }

    /**
     * 获得最新的延期任务
     */
    public DelayJob getFirstDelayTime(Integer index){
        String name = bucketNames.get(index);
        BoundZSetOperations bucket = getBucket(name);
        Set<ZSetOperations.TypedTuple> set = bucket.rangeWithScores(0,1);
        if (CollectionUtils.isEmpty(set)){
            return null;
        }
        ZSetOperations.TypedTuple typedTuple = (ZSetOperations.TypedTuple) set.toArray()[0];
        Object value = typedTuple.getValue();
        if (value instanceof DelayJob){
            return (DelayJob)value;
        }
        return null;
    }

    /**
     * 移除延时任务
     */
    public void removeDelayTime(Integer index, DelayJob delayJob){
        String name = bucketNames.get(index);
        BoundZSetOperations bucket = getBucket(name);
        bucket.remove(delayJob);
    }
}
