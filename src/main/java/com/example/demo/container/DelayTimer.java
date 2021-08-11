package com.example.demo.container;

import com.example.demo.handler.DelayJobHandler;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;
import java.util.stream.IntStream;

/**
 * Timer负责实时扫描各个Bucket，并将delay时间大于等于当前时间的Job放入到对应的Ready Queue
 * @author yunyou
 */
@Component
@RequiredArgsConstructor
public class DelayTimer implements ApplicationListener<ContextRefreshedEvent> {

    private DelayBucket delayBucket;

    private JobPool jobPool;

    private ReadyQueue readyQueue;

    @Value("${thread.size}")
    private int length;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {

        ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setNameFormat("delay-timer").build();

        ExecutorService executorService = new ThreadPoolExecutor(
                length,
                length,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingDeque<>(),threadFactory,new ThreadPoolExecutor.AbortPolicy());
        IntStream.range(0,length)
                .forEach(i->executorService.execute(
                        new DelayJobHandler(
                                delayBucket,
                                jobPool,
                                readyQueue,
                                i
                        )
                ));

    }
}
