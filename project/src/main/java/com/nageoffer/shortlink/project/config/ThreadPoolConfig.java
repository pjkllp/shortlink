package com.nageoffer.shortlink.project.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 统计任务专用线程池配置
 */
@Configuration
public class ThreadPoolConfig {

    /**
     * 统计任务线程池（处理sendLinkStatsMessage的异步任务）
     * @return 配置好的线程池
     */
    @Bean(name = "statsExecutor") // 给线程池起个名字，方便注入
    public ExecutorService statsExecutor() {
        // 1. 线程命名工厂（方便日志排查问题）
        ThreadFactory threadFactory = new ThreadFactory() {
            private final AtomicInteger threadCount = new AtomicInteger(1);
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("stats-thread-" + threadCount.getAndIncrement()); // 线程名：stats-thread-1、2、3...
                thread.setDaemon(false); // 非守护线程（确保任务执行完再退出）
                return thread;
            }
        };

        // 2. 核心参数配置（针对你的场景优化）
        int corePoolSize = 20; // 核心线程数（常驻线程，即使空闲也不销毁）
        int maximumPoolSize = 50; // 最大线程数（核心线程不够时，最多扩容到50）
        long keepAliveTime = 60; // 非核心线程空闲超时时间（60秒没活干就销毁）
        TimeUnit unit = TimeUnit.SECONDS;
        // 任务队列（核心线程忙时，任务先放队列，队列满了再扩容线程）
        BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>(10000); // 队列大小10000，足够你的场景

        // 3. 拒绝策略（队列满+线程到最大数时的兜底策略）
        // CallerRunsPolicy：让提交任务的线程（比如Tomcat主线程）临时执行，避免任务丢失
        RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();

        // 4. 创建线程池
        return new ThreadPoolExecutor(
                corePoolSize,
                maximumPoolSize,
                keepAliveTime,
                unit,
                workQueue,
                threadFactory,
                handler
        );
    }
}