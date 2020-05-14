//script can be executed in mqttfx - client is connected with 30 s keepalive
//script subscribes a specific topic

var topic = "security/+/alarm";

function execute(action) {
    out("UseCase3-exercise Script: " + action.getName());
    out("subscribe to " + topic);
    mqttManager.subscribe(topic);

    action.setExitCode(0);
    action.setResultText("done.");
    out("UseCase3-exercise Script: Done");
    return action;
}

function out(message) {
    output.print(message);
}
