package com.mqttdeepdive.exercise.solution.usecase1;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.mqttdeepdive.exercise.common.Utils;
import org.apache.commons.lang3.RandomUtils;

import java.util.HashSet;

import static com.mqttdeepdive.exercise.common.Utils.*;

public class NerdArtHandheld {

    private static final String VISITOR = "Visitor-Anja";

    public static void main(String[] args) throws Exception {
        startVisitorHandheld();
        idle(3);
    }

    public static void startVisitorHandheld() {

        final Mqtt5AsyncClient client = MqttClient.builder()
                .serverHost(BROKER_HIVEMQ_ADR)
                .serverPort(BROKER_HIVEMQ_PORT)
                .useMqttVersion5()
                .identifier(VISITOR)
                .buildAsync();

        client.connectWith()
                .keepAlive(KEEP_ALIVE)
                .send()
                .whenComplete((connAck, throwable) -> {
                    if (throwable != null) {
                        printMsg("Handheld", "ERROR   - connect client " + client.getConfig().getClientIdentifier().get() + " failed with reason: " + throwable.getMessage());
                    } else {
                        printMsg("Handheld", "SUCCESS - connect client " + client.getConfig().getClientIdentifier().get());
                        doSubscribeToInterests(client);
                    }
                });

        addDisconnectOnRuntimeShutDownHock(client);
    }

    private static void doSubscribeToInterests(Mqtt5AsyncClient client) {

        final HashSet<String> interests = chooseInterests(3);

        for (final String interest : interests) {

            client.subscribeWith()
                    .topicFilter(interest)
                    .qos(MqttQos.AT_LEAST_ONCE)
                    .callback(publish -> {

                        final String xx = new String(publish.getPayloadAsBytes());
                        printMsg("Handheld", "INFO   - Message received: " + new String(publish.getPayloadAsBytes()) + " with QoS: " + publish.getQos().getCode());
                    })
                    .send()
                    .whenComplete((subAck, throwable) -> {
                        if (throwable != null) {
                            printMsg("Handheld", "ERROR   - subscription to topic " + interest + " failed: " + throwable.getMessage());
                        } else {
                            printMsg("Handheld", "SUCCESS - subscribed to topic: " + interest);
                        }
                    });
        }
    }

    private static HashSet<String> chooseInterests(int i) {
        final HashSet<String> interestList = new HashSet<>(i);
        do {
            String key = Utils.interests[RandomUtils.nextInt(0, information.size())];
            interestList.add(TOPIC_INTEREST + key);
        } while (interestList.size() < 3);

        return interestList;
    }

}
