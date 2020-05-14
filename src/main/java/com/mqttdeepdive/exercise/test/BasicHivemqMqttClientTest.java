package com.mqttdeepdive.exercise.test;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3BlockingClient;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class BasicHivemqMqttClientTest {

    final String host = "localhost";
    final int port = 1883;
    final String clientIdSubscribe = "clientIdSub";
    final String clientIdPublish = "clientIdPub";
    final String topic = "subjectdirectory.changed";
    final String message = "test payload";
    final int publishCount = 5000;

    @Test
    public void testManyPublishes() {
        final Mqtt3AsyncClient publisher = createAndConnect(this.clientIdPublish);
        final Mqtt3AsyncClient subscriber = createAndConnect(this.clientIdSubscribe);

        AtomicInteger count2 =new AtomicInteger(0);

        final TestHandler subscriptionHandler = new TestHandler();

        subscribeBlocking(subscriber, subscriptionHandler);
        //waitSomeSeconds(2);

        publishMessages(publisher, count2);


        waitSomeSeconds(1000);


        Assertions.assertEquals(this.publishCount, count2.get());
    }

    private Mqtt3AsyncClient createAndConnect(final String clientId) {
        // we have create the connection in the sync thread
        final Mqtt3BlockingClient blockingClient =
                MqttClient.builder().useMqttVersion3().identifier(clientId).automaticReconnectWithDefaultConfig().serverHost(this.host).serverPort(this.port).buildBlocking();
        // and now we will switch to async client.
        return this.connect(blockingClient);
    }

    private Mqtt3AsyncClient connect(final Mqtt3BlockingClient blockingClient) {
        blockingClient.connectWith().simpleAuth().username("user").password("password".getBytes()).applySimpleAuth().cleanSession(false).send();
        return blockingClient.toAsync();
    }

    private void subscribe(final Mqtt3AsyncClient client, final TestHandler handler) {
        client.subscribeWith()
                .topicFilter(this.topic)
                .qos(MqttQos.AT_LEAST_ONCE)
                .callback(publish -> {
                    handler.onMessage(publish.getPayloadAsBytes()); })
                .send()
                .whenComplete((subAck, throwable) -> {
                    if (throwable != null) {
                        Assertions.fail(throwable);
                    }
        });

    }

    private void subscribeBlocking(final Mqtt3AsyncClient client, final TestHandler handler) {
        client.subscribeWith()
                .topicFilter(this.topic)
                .qos(MqttQos.AT_MOST_ONCE)
                .callback(publish -> {
                     handler.onMessage(publish.getPayloadAsBytes()); })
                .send().join();
    }


    private void publishMessages(final Mqtt3AsyncClient client, AtomicInteger count2) {

        for (int i = 0; i < this.publishCount; i++) {
            final String payload = this.message.concat(String.valueOf(i));
            client.publishWith().topic(this.topic).payload(payload.getBytes())
                    .qos(MqttQos.AT_LEAST_ONCE).send()
                    .whenComplete((mqtt3Publish, throwable) -> {
                        if (throwable != null) {
                           Assertions.fail(throwable);
                        } else {
                            System.out.println("message arrived: " + new String(payload) );
                            count2.incrementAndGet();
                        }


            });
        }
    }

    private void waitSomeSeconds(final int secs) {
        try {
            Thread.sleep(secs * 10);
        } catch (final InterruptedException e) {
            Assertions.fail(e);
        }
    }

    static class TestHandler {

        private AtomicInteger count=new AtomicInteger(0);

        public TestHandler() {
        }

        public void onMessage(final byte[] payload) {
            System.out.println("message arrived: " + new String(payload) );
            count.incrementAndGet();
        }

        public int getCallCount() {
            return this.count.get();
        }
    }
}