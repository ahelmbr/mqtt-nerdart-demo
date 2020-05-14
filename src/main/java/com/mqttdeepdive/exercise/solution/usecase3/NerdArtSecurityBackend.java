package com.mqttdeepdive.exercise.solution.usecase3;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.datatypes.MqttUtf8String;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.message.auth.Mqtt5SimpleAuth;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import org.apache.commons.lang3.RandomUtils;

import static com.mqttdeepdive.exercise.common.Utils.*;

public class NerdArtSecurityBackend {

    public static final String SECURITY_CONTROLLER_BACKEND_POSITION = "GPSController";
    public static final String SECURITY_CONTROLLER_BACKEND_STATUS = "StatusController";
    public static final String ARTPIECE_SECURITY_POSITION = "room/+/security/+/position";
    public static final String ARTPIECE_SECURITY_STATUS = "room/+/security/+/status";

    public static final String user = "Inspector";

    public static void main(String[] args) throws Exception {

        startGPSController();
        startStatusController();
        idle(5);
    }

    public static void startGPSController() {
        Mqtt5SimpleAuth simpleAUTH = Mqtt5SimpleAuth.builder()
                .username(user)
                .password("Barnaby".getBytes())
                .build();
        final Mqtt5AsyncClient client = MqttClient.builder()
                .serverHost(BROKER_HIVEMQ_ADR)
                .serverPort(BROKER_HIVEMQ_PORT)
                .useMqttVersion5()
                .simpleAuth(simpleAUTH)
                .identifier(SECURITY_CONTROLLER_BACKEND_POSITION)
                .buildAsync();

        client.connectWith()
                .keepAlive(KEEP_ALIVE)
                .cleanStart(false)
                .send()
                .whenComplete((connAck, throwable) -> {
                    if (throwable != null) {
                        printMsg(user, "ERROR   - connect client " + client.getConfig().getClientIdentifier().get() + " failed with reason: " + throwable.getMessage());
                    } else {
                        printMsg(user, "SUCCESS - connect client " + client.getConfig().getClientIdentifier().get());
                        doMonitorArtPieceSecurity(client);
                    }
                });

        addDisconnectOnRuntimeShutDownHock(client);
    }

    public static void startStatusController() {
        final Mqtt5AsyncClient client = MqttClient.builder()
                .serverHost(BROKER_HIVEMQ_ADR)
                .serverPort(BROKER_HIVEMQ_PORT)
                .useMqttVersion5()
                .identifier(SECURITY_CONTROLLER_BACKEND_STATUS)
                .buildAsync();

        client.connectWith()
                .keepAlive(KEEP_ALIVE)
                .cleanStart(false)
                .send()
                .whenComplete((connAck, throwable) -> {
                    if (throwable != null) {
                        printMsg(user, "Connection failed with reason: " + throwable.getMessage());
                    } else {
                        printMsg(user, "Connection for Client " + client.getConfig().getClientIdentifier().get() + " established");
                        doMonitorArtPieceStatus(client);
                    }
                });

        addDisconnectOnRuntimeShutDownHock(client);
    }

    private static void doMonitorArtPieceStatus(Mqtt5AsyncClient client) {

        printMsg(user, "Start subscribing to topic " + ARTPIECE_SECURITY_STATUS);

        client.subscribeWith()
                .topicFilter(ARTPIECE_SECURITY_STATUS)
                .qos(MqttQos.AT_LEAST_ONCE)
                .callback(publish -> {
                    doCheckStatusAndAlarm(client, publish);
                })
                .send()
                .whenComplete((subAck, throwable) -> {
                    if (throwable != null) {
                        printMsg(user, "ERROR   - subscription to topics failed: " + throwable.getMessage());
                    } else {
                        printMsg(user, "SUCCESS - subscribed to topics: " + ARTPIECE_SECURITY_STATUS);
                    }
                });
    }

    private static void doMonitorArtPieceSecurity(Mqtt5AsyncClient client) {

        printMsg(user, "Start subscribing to topic " + ARTPIECE_SECURITY_POSITION);

        client.subscribeWith()
                .topicFilter(ARTPIECE_SECURITY_POSITION)
                .qos(MqttQos.AT_LEAST_ONCE)
                .callback(publish -> {
                    doCheckPayloadAndAlarm(client, publish);
                })
                .send()
                .whenComplete((subAck, throwable) -> {
                    if (throwable != null) {
                        printMsg(user, "ERROR   - subscription to topics failed: " + throwable.getMessage());
                    } else {
                        printMsg(user, "SUCCESS - subscribed to topics: " + ARTPIECE_SECURITY_POSITION);
                    }
                });
    }

    private static void doCheckStatusAndAlarm(Mqtt5AsyncClient client, Mqtt5Publish publish) {
        printMsg(user, "Message received on topic " + publish.getTopic().toString() + " with payload " + new String(publish.getPayloadAsBytes()));
        final String[] topics = publish.getTopic().toString().split("/");
        final String roomId = topics[1];
        final String artPieceId = topics[3];
        printMsg(user, "check status for art piece ");
        if ("OFFLINE".equalsIgnoreCase(new String(publish.getPayloadAsBytes()))) {
            printMsg(user, "ALARM - Offline state for art piece: " + artPieceId);
            doAlarmStatus(client, artPieceId, roomId);
        }
    }

    private static void doCheckPayloadAndAlarm(Mqtt5AsyncClient client, Mqtt5Publish publish) {
        printMsg(user, "Message received on topic " + publish.getTopic().toString() + " with payload " + new String(publish.getPayloadAsBytes()));
        if (publish.getContentType().isPresent() &&
                MqttUtf8String.of("GPSCoordinates").equals(publish.getContentType().get())) {
            final String[] topics = publish.getTopic().toString().split("/");
            final String roomId = topics[1];
            final String artPieceId = topics[3];
            if (coordinatesChanged(artPieceId, new String(publish.getPayloadAsBytes()))) {
                printMsg(user, "ALARM - Coordinates Changed for art piece: " + artPieceId + " in Room " + roomId);
                doAlarmPositions(client, roomId);
            }
        }
    }

    private static void doAlarmPositions(Mqtt5AsyncClient client, String roomId) {
        publishCommand(client, roomId, TOPIC_POSITION_ALARM);
    }

    private static void doAlarmStatus(Mqtt5AsyncClient client, String artPieceId, String roomId) {
        final String msg = "Art piece: " + artPieceId + " in Room: " + roomId + " is offline!";
        publishCommand(client, msg, TOPIC_STATUS_ALARM);
    }

    private static boolean coordinatesChanged(String artPieceId, String valueFromPayload) {
        return RandomUtils.nextBoolean();
    }

    private static void publishCommand(Mqtt5AsyncClient client, String payload, String topic) {

        final Mqtt5Publish mqtt5Publish = Mqtt5Publish.builder()
                .topic(topic)
                .qos(MqttQos.AT_LEAST_ONCE)
                .contentType("TEXT")
                .payload(payload.getBytes())
                .build();

        client.publish(mqtt5Publish).whenComplete((mqtt5PublishResult, throwable) -> {
            if (throwable != null) {
                printMsg(user, "ERROR   - publish to topic: " + topic + " failed: " + throwable.getMessage());
            } else {
                printMsg(user, "SUCCESS - publish  msg '" + payload + "' to topic: " + topic);
            }
        });
    }

}
