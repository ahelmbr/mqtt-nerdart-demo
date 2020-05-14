package com.mqttdeepdive.exercise.test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MultiThreading {
    public static void main(final String[] args) {
        final ExecutorService executorService = Executors.newCachedThreadPool();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                System.out.println("Thread 1");
            }
        });
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                System.out.println("Thread 2");
            }
        });
        System.out.println("Main");
    }
}
