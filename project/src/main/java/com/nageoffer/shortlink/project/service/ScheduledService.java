package com.nageoffer.shortlink.project.service;

public interface ScheduledService {

    void replayFailedMessages();

    void trimRedisStreams();
}
