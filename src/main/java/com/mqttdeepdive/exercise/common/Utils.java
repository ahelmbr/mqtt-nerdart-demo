package com.mqttdeepdive.exercise.common;

import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;

import java.util.HashMap;

public class Utils {

    public static final String BROKER_HIVEMQ_ADR = "localhost";
    public static final int BROKER_HIVEMQ_PORT = 1883;
    public static final int KEEP_ALIVE = 30;

    public static final String TOPIC_INTEREST = "interest/";
    public static final String TOPIC_POSITION_ALARM = "security/position/alarm";
    public static final String TOPIC_STATUS_ALARM = "security/status/alarm";

    public static String[] rooms = {"1", "2", "3"};
    public static String[] artPieces = {"Picture_Pollock_1", "Picture_Pollock_2", "Picture_Kandinsky_3", "Picture_Kandinsky_4", "Picture_Rembrandt_5",
            "Picture_Richter_6", "Picture_Rembrandt_7", "Picture_Richter_8", "Picture_Picasso_9", "Picture_Miro_10", "Picture_Mondrian_11", "Picture_Marc_12"};

    public static HashMap<String, String> information = new HashMap<>();

    public static String[] interests = {"Drip_painting", "Oil_painting", "Abstract_art", "Landscape_art", "Figurative_art"};

    static {
        information.putIfAbsent(interests[0], "Drip painting is a form of abstract art in which paint is dripped or poured on to the canvas. This style of action painting was experimented with in the first half of the twentieth century by such artists as Francis Picabia, André Masson and Max Ernst, who employed drip painting in his works The Bewildered Planet, and Young Man Intrigued by the Flight of a Non-Euclidean Fly (1942).[1] Ernst used the novel means of painting Lissajous figures by swinging a punctured bucket of paint over a horizontal canvas.. ...");
        information.putIfAbsent(interests[1], "Oil painting is the process of painting with pigments with a medium of drying oil as the binder. Commonly used drying oils include linseed oil, poppy seed oil, walnut oil, and safflower oil. The choice of oil imparts a range of properties to the oil paint, such as the amount of yellowing or drying time. Certain differences, depending on the oil, are also visible in the sheen of the paints. An artist might use several different oils in the same painting depending on specific pigments and effects desired. The paints themselves also develop a particular consistency depending on the medium. The oil may be boiled with a resin, such as pine resin or frankincense, to create a varnish prized for its body and gloss....");
        information.putIfAbsent(interests[2], "Abstract art uses a visual language of shape, form, color and line to create a composition which may exist with a degree of independence from visual references in the world.[1] Western art had been, from the Renaissance up to the middle of the 19th century, underpinned by the logic of perspective and an attempt to reproduce an illusion of visible reality. The arts of cultures other than the European had become accessible and showed alternative ways of describing visual experience to the artist. By the end of the 19th century many artists felt a need to create a new kind of art which would encompass the fundamental changes taking place in technology, science and philosophy. The sources from which individual artists drew their theoretical arguments were diverse, and reflected the social and intellectual preoccupations in all areas of Western culture at that time.");
        information.putIfAbsent(interests[3], "Landscape art, is the depiction of landscapes in art – natural scenery such as mountains, valleys, trees, rivers, and forests, especially where the main subject is a wide view – with its elements arranged into a coherent composition. In other works, landscape backgrounds for figures can still form an important part of the work. Sky is almost always included in the view, and weather is often an element of the composition. Detailed landscapes as a distinct subject are not found in all artistic traditions, and develop when there is already a sophisticated tradition of representing other subjects.");
        information.putIfAbsent(interests[4], "Figurative art, sometimes written as figurativism, describes artwork (particularly paintings and sculptures) that is clearly derived from real object sources and so is, by definition, representational. The term is often in contrast to abstract art: Since the arrival of abstract art the term figurative has been used to refer to any form of modern art that retains strong references to the real world. ... ");
    }


    public static void sleep(int seconds) {
        try {
            Thread.sleep(1000 * seconds);
        } catch (InterruptedException e) {
            System.out.print(e.getMessage());
        }
    }

    public static void idle(int sleepInterval) throws InterruptedException {
        while (true) {
            sleep(sleepInterval);
            System.out.print(".");
        }
    }

    static void disconnectOnExit(Mqtt5AsyncClient client) {
        if (client != null) {
            System.out.println("Disconnect Client " + client.getConfig().getClientIdentifier().get());
            client.disconnect();
        }
    }

    public static void addDisconnectOnRuntimeShutDownHock(Mqtt5AsyncClient client) {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                disconnectOnExit(client);
            }
        });
    }

    public static void printMsg(String from, String msg) {
        System.out.println(from + ": " + msg);
    }
}
