package com.mqttdeepdive.exercise.solution.usecase3;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import org.apache.commons.lang3.RandomUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.mqttdeepdive.exercise.common.Utils.*;

public class NerdArtArtPiecesGPSSensors {

    private final static String STATUS_TOPIC = "room/room-id/security/artpiece-id/status";
    private final static String GPS_TOPIC = "room/room-id/security/artpiece-id/position";

    public static void main(String[] args) {
        startArtPieceSensors();
    }

    public static void startArtPieceSensors() {
        ExecutorService executor = Executors.newFixedThreadPool(artPieces.length);
        Runtime.getRuntime().addShutdownHook(new Thread(executor::shutdown));

        for (int i = 0; i < artPieces.length; i++) {
            String artPieceId = artPieces[i];
            String roomId = rooms[i % rooms.length];

            final String statusTopic = STATUS_TOPIC.replace("room-id", roomId).replace("artpiece-id", artPieceId);
            Mqtt5AsyncClient client = startArtPieceSensor(statusTopic, artPieceId);

            final String positionTopic = GPS_TOPIC.replace("room-id", roomId).replace("artpiece-id", artPieceId);
            Runnable gpsService = () -> publishGPSCoordinates(positionTopic, client);
            executor.execute(gpsService);
        }
    }

    private static void publishGPSCoordinates(String topic, Mqtt5AsyncClient sensor) {
        while (true) {
            sleep(10);
            if (sensor.getConfig().getState().isConnected()) {
                publishCoordinates(sensor, topic);
            } else {
                printMsg(" GPSSensors ", "FAIL - publish to " + topic + ", REASON - sensor '" + sensor.getConfig().getClientIdentifier() + " not yet connected ");
            }
        }
    }

    private static Mqtt5AsyncClient startArtPieceSensor(String statusTopic, String artPieceId) {

        final Mqtt5AsyncClient client = MqttClient.builder()
                .serverHost(BROKER_HIVEMQ_ADR)
                .serverPort(BROKER_HIVEMQ_PORT)
                .useMqttVersion5()
                .identifier(artPieceId)
                .buildAsync();

        final Mqtt5Publish offline = Mqtt5Publish.builder()
                .topic(statusTopic)
                .qos(MqttQos.AT_LEAST_ONCE)
                .contentType("TEXT")
                .payload("OFFLINE".getBytes())
                .build();

        client.connectWith()
                .keepAlive(KEEP_ALIVE)
                .willPublish(offline)
                .cleanStart(true)
                .send()
                .whenComplete((connAck, throwable) -> {
                    if (throwable != null) {
                        printMsg(" GPSSensors ", "ERROR   - connect client " + client.getConfig().getClientIdentifier().get() + " failed with reason: " + throwable.getMessage());
                    } else {
                        printMsg(" GPSSensors ", "SUCCESS - connect client " + client.getConfig().getClientIdentifier().get());
                        doPublishOnlineState(client, statusTopic);
                        addDisconnectOnRuntimeShutDownHock(client);
                    }
                });

        return client;
    }

    private static void doPublishOnlineState(Mqtt5AsyncClient client, String topic) {
        final Mqtt5Publish online = Mqtt5Publish.builder()
                .topic(topic)
                .qos(MqttQos.AT_LEAST_ONCE)
                .payload("ONLINE".getBytes())
                .build();

        client.publish(online)
                .whenComplete((publishResult, throwable) -> {
                    if (throwable != null) {
                        printMsg(" GPSSensors ", "Publish to topic: " + topic + " failed: " + throwable.getMessage());
                    } else {
                        printMsg(" GPSSensors ", "Successful published status information to topic: " + topic);
                    }
                });


    }

    private static void publishCoordinates(Mqtt5AsyncClient client, String topic) {

        final String myposition = getCoordinateString();

        final Mqtt5Publish position = Mqtt5Publish.builder()
                .topic(topic)
                .qos(MqttQos.AT_LEAST_ONCE)
                .contentType("GPSCoordinates")
                .payload(myposition.getBytes())
                .build();

        client.publish(position).whenComplete((publishResult, throwable) -> {
            if (throwable != null) {
                printMsg(" GPSSensors ", "ERROR   - publish to topic: " + topic + " failed: " + throwable.getMessage());
            } else {
                printMsg(" GPSSensors ", "SUCCESS - publish  msg '" + myposition + "' to topic: " + topic);
            }
        });
    }

    private static String getCoordinateString() {
        return "" + RandomUtils.nextInt() + "," + RandomUtils.nextInt();
    }
}
