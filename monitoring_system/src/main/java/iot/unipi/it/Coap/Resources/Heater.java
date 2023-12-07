package iot.unipi.it.Coap.Resources;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


import java.util.ArrayList;
import java.util.List;
import iot.unipi.it.Logger.Logger;

//controllare HEATER
public class Heater {
    private List<CoapClient> clientHeaderList = new ArrayList<>();
    // private List<CoapObserveRelation> observeActuatorList = new ArrayList<>();
    private int temperatureDetected = 24;

    // SERVER SIDE
    public void addHeaderActuator(String ip) {
        Logger.log("The header actuator: [" + ip + "] is now registered");

        // Add the header actuator to the list
        CoapClient newHeaderActuator = new CoapClient("coap://[" + ip + "]/heater_actuator");
        // observeActuatorList.add(newHeaderActuator.observe(
        //         new CoapHandler() {
        //             public void onLoad(CoapResponse response) {
        //                 getHandler(response);
        //             }

        //             public void onError() {
        //                 System.err.println("OBSERVING FAILED");
        //             }
        //         }));
        // IrrigationSystemDbManager.insertTemperature(temperatureDetected);
        clientHeaderList.add(newHeaderActuator);
    }

    public void deleteHeaderActuator(String ip) {
        for (int i = 0; i < clientHeaderList.size(); i++) {
            if (clientHeaderList.get(i).getURI().equals(ip)) {
                clientHeaderList.remove(i);
                // observeActuatorList.get(i).proactiveCancel();
                // observeActuatorList.remove(i);
                // remove from db
            }
        }
    }

    // public void cutAllConnection() {
    // observeSensor.proactiveCancel();
    // }

    // CLIENT SIDE
    public void printDevices() {
        System.out.println("Heater actuators:");
        for (CoapClient cc : clientHeaderList)
            System.out.println("\t" + cc.getURI());
        System.out.println("");
    }

    public int getTemperature(int i) {
        CoapClient client = clientHeaderList.get(i);
        CoapResponse response = client.get();
        return getHandler(response);
    }

    public int getHandler(CoapResponse response) {
    if (response != null) {
        System.out.println(response.getResponseText());
        JSONParser parser = new JSONParser();
        try {
            JSONObject json = (JSONObject) parser.parse(response.getResponseText());
            return ((Long) json.get("temperature")).intValue();
        } catch (ParseException e) {
            e.printStackTrace();
            return 404; // or any other error code
        }
    } else {
        System.out.println("Request failed");
        return 404;
    }
}

    public void putTemperature(String value) {
        String msg = "temperature=" + value;
        for (int i = 0; i < clientHeaderList.size(); i++) {
            CoapClient client = clientHeaderList.get(i);
            client.put(new CoapHandler() {

                public void onLoad(CoapResponse response) {
                    if (response != null) {
                        if (!response.isSuccess())
                            System.out.println("Something went wrong with the actuator");
                    }
                }

                public void onError() {
                    System.err.println("[ERROR: RipeningNotifier " + client.getURI() + "] ");
                }

            }, msg, MediaTypeRegistry.TEXT_PLAIN);
        }
    }

}