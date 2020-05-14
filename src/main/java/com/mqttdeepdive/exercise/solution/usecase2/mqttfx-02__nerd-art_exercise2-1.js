//script can be executed in mqttfx - client is connected with 30 s keepalive
//script subscribes a specific topic

var topic = "room/+/temperature/command";

function execute(action) {
    out("UseCase2 Command Monitor: " + action.getName());
    out("subscribe to " + topic);
    mqttManager.subscribe(topic);

    action.setExitCode(0);
    action.setResultText("done.");
    out("UseCase2 Command Monitor: Done");
    return action;
}

function out(message) {
    output.print(message);
}
