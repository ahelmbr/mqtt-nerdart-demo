#!/usr/bin/env bash
## Set device-id (A constant Identifier is needed here)
## Send randomised Payload value of 1 - 100 for humidity and 1-30 for temperature on topics 'room/roomID/temp' and 'room/roomID/hum'
## Send every 15 seconds (15 minutes in reality)
## Send as retained messages with QoS=2
## Rooms 1,2 and 3
## Set clean Start to no and set session expiry

## use mqtt cli commandline tool from https://github.com/hivemq/mqtt-cli

while true
do
    for i in 1 2 3
    do
        VALUE=$(( $RANDOM % 29 +1))
        echo "publish temperature $VALUE of room $i"

        mqtt pub -p 1884 --no-cleanStart -se 3600 -i "DeviceTemp$i" -t "room/$i/temperature" -m "$VALUE" -q 1 -r -v -V5

        VALUE=$(( $RANDOM % 99 +1))
        echo "publish humidity $VALUE of room $i"
        mqtt pub -p 1884 --no-cleanStart -se 3600 -i "DeviceHumidity$i" -t "room/$i/humidity" -m "$VALUE" -q 1 -r -v -V5

        echo "sleep ..... "
        sleep 5
    done

done

