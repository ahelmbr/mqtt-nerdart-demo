# Workshop MQTT deep dive
These exercises aim to provide a use case scenario in which most MQTT feature can be practically applied.

General Use Case is described in the parent directory.

## Use Case 2: Automated control to ensure optimal temperature and humidity in exhibition rooms.

* Humidity and Temperature sensors periodically send values to broker on topics 'room/sensor_id/temperature' and 'room_id/sensor_id/humidity'
* Backend service analyzes these values via the use of wildcard subscriptions ('+/+/temperature and +/+/humidity)
* In case of these values mismatching in comparison to given 'should be' values, the back end service send commands to the corresponding air conditioning unit or humidity regulator unit 
  (room_id/ac_id/increase_temp / room_id/ac_id/decrease_temp or 
  room_id/hc_id/increase_humidity / room_id/hc_id/decrease_humidity)
* Smart AC (air conditioning unit) and HC (humidity control unit) listen individual command topics (room_id/ac_id/+) 
  and execute specific reactions to given commands

## Components involved

In order to achieve our desired behaviour, we need the following components with MQTT capabilities
* Smart sensors for temperature and humidity (MQTT Client) --> HiveMQ MQTT Client Library
* Smart controls for air conditioning and heater (MQTT Client) -> mqtt-cli sub client
* Backend service, controlling and adjusting temperature and humidity --> HiveMQ MQTT Client Library

* An MQTT broker as the central communications hub (public broker)

## Exercises

The following lists a number of exercises, designed to make the workshop participants familiar with MQTT in general as well as specific features by getting a hands on experience.

### Exercise 1: Smart sensors and air conditioning unit

We have 3 Rooms with expensive pictures, that have to be controlled

>Each room has 2 or more sensors that measure and transmit temperature and humidity data.

**MQTT Configuration Sensor **
* Configure MQTT Client with keepAlive = 30 seconds (30 min in reality, using this value for demonstration purposes)
* Set device-id = clientID  (A constant Identifier is needed here)
  > Send randomised Payload value of 1 - 100 for humidity and 1-30 for temperature on topics 'room/room-id/temperature' and 'room/room-id/humidity' 
  
  > Send every 15 seconds (15 minutes in reality)
  
  > Send as retained messages with QoS=2

**How to Start:** use a mqtt cli publish script to emulate the constraint device sensor

>Each room has an air conditioning and humidity control unit, which listens to MQTT commands that can be use to control it.

**MQTT Control Units**
* Connect a subscriber, listening to 'room/+/temperature/command' and 'room/+/humidity/command' 
* based on topic and payload the unit will increase/decrease humidity/temperature
* simulate via the use of console output

**How to Start:** 
* Use MQTT.fx to subscribe and listen to the topics


**Backend**
* in the backend connect a service that listens to 'room/+/temperature' and 'room/+/humidity'
* It checks every incoming message. 
  > humidity has to be between 60 and 70
 
  > temperature has to be between 15 and 20
* whenever these value are outside the range, 
  > the service will determine which room and device the value was coming from and send a command "up" or "down" to the topics 'room/+/temperature/command' and 'room/+/humidity/command' 


**How to implement** 
* Use the HiveMQ MQTT Client Library MQTT5AsyncClient implementation.

### Finally
Take a look at the HiveMQ control center

