package com.example.demo.controller;

import com.alibaba.fastjson.JSON;
import com.example.demo.entity.DelayJob;
import com.example.demo.entity.Job;
import com.example.demo.serivce.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Jashin
 */
@RestController
@RequestMapping("delay")
@RequiredArgsConstructor
public class DelayController {

    private final JobService jobService;

    @RequestMapping("/add")
    public String addDefJob(Job request){
        DelayJob delayJob = jobService.addDefJob(request);
        return JSON.toJSONString(delayJob);
    }

    @RequestMapping("/pop")
    public String getProcessJob(String topic){
        Job process = jobService.getProcessJob(topic);
        return JSON.toJSONString(process);
    }

    @RequestMapping("/finish")
    public String finishJob(Long jobId){
        jobService.finishJob(jobId);
        return "success";
    }

    @RequestMapping("/delete")
    public String deleteJob(Long jobId){
        jobService.deleteJob(jobId);
        return "success";
    }

}
