package com.mqttdeepdive.exercise.solution;

import com.mqttdeepdive.exercise.solution.usecase1.NerdArtHandheld;
import com.mqttdeepdive.exercise.solution.usecase1.NerdArtInformationService;
import com.mqttdeepdive.exercise.solution.usecase2.NerdArtClimateBackend;
import com.mqttdeepdive.exercise.solution.usecase3.NerdArtArtPiecesGPSSensors;
import com.mqttdeepdive.exercise.solution.usecase3.NerdArtSecurityBackend;

import static com.mqttdeepdive.exercise.common.Utils.idle;

public class NerdArt {

    public static void main(String[] args) throws Exception {

        // Use case 3
        System.out.println("START GPS SENSORS AND BACKEND");
        NerdArtArtPiecesGPSSensors.startArtPieceSensors();
        NerdArtSecurityBackend.startGPSController();
        NerdArtSecurityBackend.startStatusController();

        //Backend for use case 2
        System.out.println("START CLIMATE CONTROLLER BACKEND");
        NerdArtClimateBackend.startClimateControllerBackend();

        // Use Case 1
        System.out.println("START VISITOR HANDHELD");
        NerdArtHandheld.startVisitorHandheld();

        System.out.println("START INFORMATION CONTROLLER");
        NerdArtInformationService.startInformationService();

        //do not exit
        idle(3);
    }


}
