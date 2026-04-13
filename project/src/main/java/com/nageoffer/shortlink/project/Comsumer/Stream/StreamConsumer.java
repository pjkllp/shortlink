package com.nageoffer.shortlink.project.Comsumer.Stream;

import jakarta.annotation.PostConstruct;
import org.springframework.data.redis.connection.stream.*;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public interface StreamConsumer {


    ExecutorService streamConsumerPool =new ThreadPoolExecutor(
            2,4,3000, TimeUnit.SECONDS,new ArrayBlockingQueue<>(200)
    );

    @PostConstruct
    void initGroup();

    void consumeStream();

    void consume(String consumerName);

    List<MapRecord<String,Object,Object>> getRecords(String group,String consumerName,String stream);
}
