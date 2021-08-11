package com.example.demo.handler;

import com.alibaba.fastjson.JSON;
import com.example.demo.constant.DelayConfig;
import com.example.demo.constant.JobStatus;
import com.example.demo.container.DelayBucket;
import com.example.demo.container.JobPool;
import com.example.demo.container.ReadyQueue;
import com.example.demo.entity.DelayJob;
import com.example.demo.entity.Job;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;

/**
 * @author yunyou
 */
@Slf4j
@Data
@AllArgsConstructor
public class DelayJobHandler implements Runnable{

    /**
     * 延迟队列
     */
    private DelayBucket delayBucket;

    /**
     * 任务池
     */
    private JobPool jobPool;

    private ReadyQueue readyQueue;

    /**
     * 索引
     */
    private int index;

    @Override
    public void run() {
        log.info("延迟任务开始执行");
        while (true) {
            try {
                DelayJob delayJob = delayBucket.getFirstDelayTime(index);
                //没有任务
                if (delayJob == null){
                    sleep();
                    continue;
                }
                // 发现延时任务
                // 延迟时间没到
                if (delayJob.getDelayDate() > System.currentTimeMillis()){
                    sleep();
                    continue;
                }
                Job job = jobPool.getJob(delayJob.getJobId());

                //延迟任务元数据不存在
                if (ObjectUtils.isEmpty(job)){
                    log.info("移除不存在任务:{}", JSON.toJSONString(delayJob));
                    delayBucket.removeDelayTime(index,delayJob);
                    continue;
                }

                JobStatus status = job.getStatus();
                if (JobStatus.RESERVED.equals(status)){
                    log.info("处理超时任务:{}",JSON.toJSONString(status));
                    //超时任务
                    processTtrJob(delayJob,job);
                }else {
                    log.info("处理延时任务:{}",JSON.toJSONString(job));
                    //延时任务
                    processDelayJob(delayJob,job);
                }
            }catch (Exception e){
                log.error("扫描DelayBucket出错:{}", (Object) e.getStackTrace());
                sleep();
            }
        }
    }

    /**
     * 处理ttr的任务
     */
    private void processTtrJob(DelayJob delayJob, Job job){
        job.setStatus(JobStatus.DELAY);
        //修改任务池状态
        jobPool.addJob(job);
        //设置到等待处理任务
        readyQueue.pushJob(delayJob);
        //移除delayBucket中的任务（从延迟任务队列中删除）
        delayBucket.removeDelayTime(index,delayJob);
    }

    /**
     * 处理延时任务
     */
    private void processDelayJob(DelayJob delayJob,Job job){
        job.setStatus(JobStatus.READY);
        //修改任务池状态
        jobPool.addJob(job);
        //设置到待处理任务
        readyQueue.pushJob(delayJob);
        //移除bucket的任务
        delayBucket.removeDelayTime(index,delayJob);
    }

    private void sleep(){
        try {
            Thread.sleep(DelayConfig.SLEEP_TIME);
        } catch (InterruptedException e) {
            log.error("",e);
        }
    }
}
