# Workshop MQTT deep dive
These exercises aim to provide a use case scenario in which most MQTT feature can be practically applied.

General Use Case is described in the parent directory.

## Use Case 1: Personalized Information for individual visitor  

* Customer personalises his experience by entering his peronsal data and interests into the handheld device that is handed to him at the entrance
* Based on what he enters, he will receive interesting information and facts for the topics he is interested in

## Components involved

In order to achieve our desired behaviour, we need the following components with MQTT capabilities

* A handheld device for visitors (MQTT Client) -> HiveMQ MQTT Client Library
* An information server, periodically publishing facts on certain topics (MQTT Client) -> HiveMQ MQTT Client Library
* An MQTT broker as the central communications hub (public broker or localhost)


## Exercises

The following lists a number of exercises, designed to make the workshop participants familiar with MQTT in general as well as specific features by getting a hands on experience.



### Exercise 1: Configure and connect your hand held device

This exercise is supposed to simulate the first part of the customized user experience use case.

> Create an MQTT client with the following attributes
* Client ID = first letter of first name + (maximum of) 7 letters of last name (like Anja)
* Broker Address = 'tcp://localhost:1883'
* keepAlive = 30seconds


**How to start:** 
* Start your local HiveMQ Broker 
* Start with MQTT FX and connect to local HiveMQ and subscribe to 3 of the Topics ("drip-painting", "oil", "abstract", "landscape" or "portrait")
* Finally: Use the HiveMQ MQTT Client Library MQTT5AsyncClient implementation. 

* Extend the client publish code, via the use of payload-format and determining whether text, picture or video should be displayed.

~~~~
Excerpt:
...

Mqtt5AsyncClient client = MqttClient.builder().serverHost("localhost")
                                    .useMqttVersion5().identifier("Anja").buildAsync();
client.connectWith()
                .keepAlive(30)
                .send()
                .whenComplete((mqtt5ConnAck, throwable) -> {
                    if (throwable != null) {
                        System.out.println("Connection failed");

                    } else {
                        System.out.println("Connection established");                       
                    }
                });
                
~~~~

Show your interest in 3 of the topics "drip-painting", "oil", "abstract", "landscape" or "portrait"
by adding Subscriptions with QoS=1 on these topics to your connected client.
Do so by creating 3 separate MqttSubscription objects and adding these subscriptions asynchronously to your connection, in case of a successful connection establishment.

~~~~ 
Mqtt5Subscription subscription = Mqtt5Subscription.builder()
                                                  .topicFilter("portrait").qos(MqttQos.AT_LEAST_ONCE).build();
...
        
System.out.println("Connection established");
client.subscribeWith()
       .addSubscription(subscription)
        .callback(
          publish -> System.out.println("Message received: " + new String(publish.getPayloadAsBytes())+ " with QoS: "+publish.getQos().getCode()))
        .send()
        .whenComplete((subAck, throwable1) -> {
            if (throwable1 != null) {
                System.out.println("Subscription failed: "+throwable1.toString());
            } else {
                System.out.println("Subscription successful");
            }
        });
~~~~ 

### Exercise 2: Connect the information service and start periodically publishing news

A central service periodically provides useful information for all relevant topics.

* Establish an MQTT connection to the broker
* After successful connection establishment, start periodically (once very minute) sending information to the following topics for interest
* Use short, simple text passages to simulate
* non-retained, QoS=1  


 
### All 
Take a look at the HiveMQ control center

