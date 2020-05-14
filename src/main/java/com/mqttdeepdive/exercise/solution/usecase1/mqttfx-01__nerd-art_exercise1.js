//script can be executed in mqttfx
//script subscribes to 3 topics

var Thread = Java.type("java.lang.Thread");

var interests = ["drip-painting", "oil", "abstract", "landscape", "portrait"];

function execute(action) {
    out("UseCase1-exercise1  Script: " + action.getName());

    for (var i = 0; i < 3; i++) {
        subscribe(interests[Math.floor(Math.random() * 5)]);
        Thread.sleep(500);
    }

    action.setExitCode(0);
    action.setResultText("done.");
    out("Exercise1 Script: Done");
    return action;
}

function subscribe(topic) {
    out("subscribe to " + topic);
    mqttManager.subscribe("interests/" + topic);
}

function out(message) {
    output.print(message);
}
