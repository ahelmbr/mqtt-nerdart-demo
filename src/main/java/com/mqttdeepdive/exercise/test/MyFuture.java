package com.mqttdeepdive.exercise.test;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MyFuture {
    public static void main(final String[] args) throws Exception {
        final ExecutorService executorService = Executors.newCachedThreadPool();
        final Future<String> future = executorService.submit(
                new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        return "hello";
                    }
                });
        System.out.println(future.get());
    }
}
