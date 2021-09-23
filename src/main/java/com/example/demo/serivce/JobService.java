package com.example.demo.serivce;

import com.example.demo.constant.JobStatus;
import com.example.demo.container.DelayBucket;
import com.example.demo.container.JobPool;
import com.example.demo.container.ReadyQueue;
import com.example.demo.entity.DelayJob;
import com.example.demo.entity.Job;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author Jashin
 */
@Service
@RequiredArgsConstructor
public class JobService {

    private final DelayBucket delayBucket;

    private final ReadyQueue readyQueue;

    private final JobPool jobPool;

    /**
     * 添加任务
     * @param job
     * @return
     */
    public DelayJob addDefJob(Job job){
        job.setStatus(JobStatus.READY);
        jobPool.addJob(job);
        DelayJob delayJob = new DelayJob(job);
        delayBucket.addDelayJob(delayJob);
        return delayJob;
    }

    /**
     * 获取任务
     */
    public Job getProcessJob(String topic){
        //拿到任务
        DelayJob delayJob = readyQueue.popJob(topic);
        if (delayJob == null && delayJob.getJobId() == 0L){
            return new Job();
        }
        Job job = jobPool.getJob(delayJob.getJobId());
        //元数据已经删除 则取下一个
        if (job == null){
            job = getProcessJob(topic);
        }else {
            job.setStatus(JobStatus.RESERVED);
            delayJob.setDelayDate(System.currentTimeMillis() + job.getTtrTime());
            jobPool.addJob(job);
            delayBucket.addDelayJob(delayJob);
        }
        return job;
    }

    /**
     * 完成一个执行的任务
     */
    public void finishJob(Long jobId){
        jobPool.removeDelayJob(jobId);
    }

    /**
     * 删除一个执行的任务
     */
    public void deleteJob(Long jobId){
        jobPool.removeDelayJob(jobId);
    }

}
