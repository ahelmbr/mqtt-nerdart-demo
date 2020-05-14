package com.mqttdeepdive.exercise.solution.usecase2;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.datatypes.MqttTopicFilter;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5Subscription;

import java.nio.ByteBuffer;

import static com.mqttdeepdive.exercise.common.Utils.*;

public class NerdArtClimateBackend {

    private static final String CLIMATE_CONTROLLER_BACKEND = "ClimateControllerBackend";
    private static final String TOPIC_ROOMS_TEMP = "room/+/temperature";
    private static final String TOPIC_ROOMS_HUM = "room/+/humidity";
    private static final String DECREASE = "DOWN";
    private static final String INCREASE = "UP";

    public static void main(String[] args) throws Exception {

        startClimateControllerBackend();
        idle(3);
    }

    public static void startClimateControllerBackend() {
        final Mqtt5AsyncClient client = MqttClient.builder()
                .serverHost(BROKER_HIVEMQ_ADR)
                .serverPort(1884)
                .useMqttVersion5()
                .identifier(CLIMATE_CONTROLLER_BACKEND)
                .buildAsync();

        client.connectWith()
                .keepAlive(KEEP_ALIVE)
                .send()
                .whenComplete((connAck, throwable) -> {
                    if (throwable != null) {
                        printMsg("Climate Backend", "ERROR   - connect client " + client.getConfig().getClientIdentifier().get() + " failed with reason: " + throwable.getMessage());
                    } else {
                        printMsg("Climate Backend", "SUCCESS - connect client " + client.getConfig().getClientIdentifier().get());
                        doSubscribeToRooms(client);
                    }
                });

        
        addDisconnectOnRuntimeShutDownHock(client);
    }

    private static void doSubscribeToRooms(Mqtt5AsyncClient client) {

        printMsg("Climate Backend", "Start subscribing to rooms temperature and humidity topic");

        final Mqtt5Subscription subscriptionTemperature = Mqtt5Subscription.builder().topicFilter(TOPIC_ROOMS_TEMP).qos(MqttQos.AT_LEAST_ONCE).build();
        final Mqtt5Subscription subscriptionHumidity = Mqtt5Subscription.builder().topicFilter(TOPIC_ROOMS_HUM).qos(MqttQos.AT_LEAST_ONCE).build();

        client.subscribeWith()
                .addSubscription(subscriptionTemperature)
                .addSubscription(subscriptionHumidity)
                .callback(publish -> {
                    printMsg("Climate Backend", "Message received on topics " + publish.getTopic() + " with payload " + new String(publish.getPayloadAsBytes()) + " with QoS: " + publish.getQos().getCode());
                    doPublishCommand(client, publish);
                })
                .send()
                .whenComplete((subAck, throwable) -> {
                    if (throwable != null) {
                        printMsg("Climate Backend", "Subscription to topics failed: " + throwable.getMessage());
                    } else {
                        printMsg("Climate Backend", "Successful subscribed to topics: " + TOPIC_ROOMS_TEMP + ", " + TOPIC_ROOMS_HUM);
                    }
                });
    }

    private static void doPublishCommand(Mqtt5AsyncClient client, Mqtt5Publish publish) {

        final Integer val = getIntValueFromPayload(publish);
        printMsg("Climate Backend", "Try publish to topics: " + publish.getTopic() + "/command");

        if (MqttTopicFilter.of(TOPIC_ROOMS_TEMP).matches(publish.getTopic())) {
            if (val != null) {
                final String result = temperatureControl(val);
                publishCommand(client, result, publish.getTopic().toString());
            }
        } else if (MqttTopicFilter.of(TOPIC_ROOMS_HUM).matches(publish.getTopic())) {
            if (val != null) {
                final String result = humidityControl(val);
                publishCommand(client, result, publish.getTopic().toString());
            }
        } else {
            printMsg("Climate Backend", "WARN    - Topic not match to command filter: " + publish.getTopic().toString());
        }
    }

    private static Integer getIntValueFromPayload(Mqtt5Publish publish) {
        ByteBuffer buffer = (publish.getPayload().isPresent()) ? publish.getPayload().get() : null;
        if (buffer != null) {
            byte[] dst = new byte[buffer.limit()];
            try {
                // deep copy necessary
                buffer.get(dst);
                return Integer.valueOf(new String(dst));
            } catch (Exception any) {
                printMsg("Climate Backend", "Cannot read integer value from Payload, error" + any.getMessage());
            }
        }
        return null;
    }

    private static void publishCommand(Mqtt5AsyncClient client, String payload, String topic) {
        if (payload != null) {
            final Mqtt5Publish command = Mqtt5Publish.builder()
                    .topic(topic + "/command")
                    .qos(MqttQos.AT_LEAST_ONCE)
                    .contentType("TEXT")
                    .payload(payload.getBytes())
                    .build();

            client.publish(command).whenComplete((publishResult, throwable) -> {
                if (throwable != null) {
                    printMsg("Climate Backend", "ERROR   - publish to topic: " + topic + " failed: " + throwable.getMessage());
                } else {
                    printMsg("Climate Backend", "SUCCESS - publish msg '" + payload + "' to topic: " + topic + "/command");
                }
            });
        }
    }

    private static String temperatureControl(Integer val) {
        return (val < 15) ? INCREASE : (val > 20) ? DECREASE : null;
    }

    private static String humidityControl(Integer val) {
        return (val < 40) ? INCREASE : (val > 60) ? DECREASE : null;
    }

}
