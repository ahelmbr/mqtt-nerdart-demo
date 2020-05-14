# Workshop MQTT deep dive
These exercises aim to provide a use case scenario in which most MQTT feature can be practically applied.

General Use Case is described in the parent directory.

## Use Case 3 - Theft prevention for art pieces

* Art pieces are equipped with small smart devices that have MQTT and GPS capabilities
* Periodically these devices transmit their current GPS coordinates to an individual topic ('room/piece_id/position')
* The devices also use a Will topic ('room/piece_id/status') to update their online status in case of connection loss
* In the security control room there is backend service connected to a monitoring dashboard
* This service compares GPS coordinates sent by the pieces against a "should" values for each individual art piece
* In case of an offline status message or a mismatch in GPS coordinates an alarm ('alarm') is triggered at the monitoring dashboard to inform the security guards to check out the rooms/pieces    

## Components involved

In order to achieve our desired behaviour, we need the following components with MQTT capabilities

* A MQTT Client for simulating GPS chip 
* A MQTT Client for backend security service 
* A monitoring dashboard MQTT.fx (with script)
* An MQTT broker as the central communications hub (public broker)


## Exercises 

The following lists a number of exercises, designed to make the workshop participants familiar with MQTT in general as well as specific features by getting a hands on experience.

### Exercise 1: Theft prevention 

**Devices**
GPS and MQTT enabled security modules on art pieces.
* each art piece connects to the broker, using a persistent session, LWT "offline" topic: room/room-id/security/artpiece-id/status, retained
* send "online" to LWT topic after successful connect, retained
* periodically send current GPS coordinates on room/room-id/security/artpiece-id/position


**Monitor Control Unit**
Monitoring Dashboard in Security Room
* uses username and password to connect 
* listening to security/+/alarm
* when message is received, checking room and type (status,position)
* displaying yellow (status) or red (position) alarm with room on dashboard

**How to Start:** 
* Use MQTT.fx to subscribe and listen to the topics


**Backend** 
Backend service checking online status and coordinates

_GPS Monitoring_ 
* listen on topic 'room/room-id/security/artpiece-id/position' 
* if payload is  "GPS coordinates" service reads artpiece-id from topic and checks sent coordinates vs values from an external source (database, file, ...)
* if values mismatch room is read from topic and message with room name is sent to 'security/position/alarm'

_Status Monitoring_
* listen on topic 'room/room-id/security/artpiece-id/status' 
* if payload is "offline", room is read from topic and message with room name is sent to 'security/status/alarm'




**How to implement** 
* Use the HiveMQ MQTT Client Library MQTT5AsyncClient implementation.

### Finally
Take a look at the HiveMQ control center

 

