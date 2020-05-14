package com.mqttdeepdive.exercise.solution.usecase1;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5ClientBuilder;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.mqttdeepdive.exercise.common.Utils.*;

public class NerdArtInformationService {

    private static final String INFORMATION_SERVICE_CLIENT = "InformationService";

    public static void main(String[] args) {
        startInformationService();
    }

    public static void startInformationService() {

        final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        Runtime.getRuntime().addShutdownHook(new Thread(executor::shutdown));

        final Mqtt5AsyncClient client = MqttClient.builder()
                .serverHost(BROKER_HIVEMQ_ADR)
                .serverPort(BROKER_HIVEMQ_PORT)
                .useMqttVersion5()
                .identifier(INFORMATION_SERVICE_CLIENT)
                .buildAsync();

        final Runnable runInformationService = () -> publishInformation(client);

        client.connectWith().send().whenComplete((connAck, throwable) -> {
            if (throwable != null) {
                printMsg("Information Service", "ERROR   - connect client " + client.getConfig().getClientIdentifier().get() + " failed with reason: " + throwable.getMessage());
            } else {
                client.subscribeWith()
                        .topicFilter(TOPIC_INTEREST + "#")
                        .qos(MqttQos.AT_LEAST_ONCE)
                        .callback(publish -> {
                            final String xx = new String(publish.getPayloadAsBytes());
                            printMsg("Information Service", "INFO   - Message received: " + new String(publish.getPayloadAsBytes()) + " with QoS: " + publish.getQos().getCode());
                        })
                        .send();
                printMsg("Information Service", "SUCCESS - connect client " + client.getConfig().getClientIdentifier().get());
                executor.scheduleAtFixedRate(runInformationService, 0, 10, TimeUnit.SECONDS);
            }
        });

        addDisconnectOnRuntimeShutDownHock(client.toAsync());
    }

    private static void publishInformation(Mqtt5AsyncClient client) {
        if (client.getConfig().getState().isConnected()) {

            for (Map.Entry<String, String> entry : information.entrySet()) {
                final String topic = TOPIC_INTEREST + entry.getKey();
                final String message = entry.getValue();

                client.publishWith()
                        .topic(topic)
                        .qos(MqttQos.AT_LEAST_ONCE)
                        .payload(message.getBytes())
                        .send()
                        .whenComplete((publishResult, throwable) -> {
                            if (throwable != null) {
                                printMsg("Information Service", "ERROR   - publish to topic: " + topic + " failed: " + throwable.getMessage());
                            } else {
                                printMsg("Information Service", "SUCCESS - publish msg '" + message.substring(0, 10) + "... ' to topic: " + topic);
                            }
                        });
            }
        }
    }

}
