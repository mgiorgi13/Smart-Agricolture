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

public class Irrigation {
    private List<CoapClient> clientIrrigationSwitchList = new ArrayList<>();
    
    // private List<CoapObserveRelation> observeActuatorList = new ArrayList<>();

    // SERVER SIDE
    public void addIrrigationActuator(String ip) {
        Logger.log("The Irrigation actuator: [" + ip + "] is now registered");

        // Add the Irrigation actuator to the list
        CoapClient newIrrigationSwitch = new CoapClient("coap://[" + ip + "]/irrigation_switch");
        // observeActuatorList.add(newIrrigationActuator.observe(
        // new CoapHandler() {
        // public void onLoad(CoapResponse response) {
        // getHandler(response);
        // }

        // public void onError() {
        // System.err.println("OBSERVING FAILED");
        // }
        // }));
        // IrrigationSystemDbManager.insertTemperature(temperatureDetected);
        
        clientIrrigationSwitchList.add(newIrrigationSwitch);
    }

    public void deleteIrrigationActuator(String ip) {
        for (int i = 0; i < clientIrrigationSwitchList.size(); i++) {
            if (clientIrrigationSwitchList.get(i).getURI().equals(ip)) {
                clientIrrigationSwitchList.remove(i);
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
        int i = 0;
        System.out.println("Irrigation actuators:");
        for (CoapClient cc : clientIrrigationSwitchList)
            System.out.println("\t"+ i + ": "+ cc.getURI());
        System.out.println("");
    }


    public JSONObject getStatusHandler(CoapResponse response) {
        System.out.println("Response: " + response.getResponseText());
        if (response != null) {
            JSONParser parser = new JSONParser();
            JSONObject json = new JSONObject();
            try {
                json = (JSONObject) parser.parse(response.getResponseText());
                return json;
            } catch (ParseException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            System.out.println("Request failed");
            return null;
        }
    }

    public void getSwitchStatus() {
        for (int i = 0; i<clientIrrigationSwitchList.size(); i++) {
            CoapClient client = clientIrrigationSwitchList.get(i);
            CoapResponse response = client.get();
            if (getSwitchHandler(response) == 1)
                System.out.println("\t"+ i + ": " + "on");
            else
                System.out.println("\t"+ i + ": " + "off");
        }
    }

    public int getSwitchHandler(CoapResponse response) {
        if (response != null) {
            JSONParser parser = new JSONParser();
            JSONObject json = new JSONObject();
            String status;
            try {
                json = (JSONObject) parser.parse(response.getResponseText());
                status = (String) json.get("switch");
                if (status.equals("on"))
                    return 1;
                else
                    return 0;
            } catch (ParseException e) {
                e.printStackTrace();
                return -1;
            }
        } else {
            System.out.println("Request failed");
            return -1;
        }
    }

    public void setStatus(int temp, int fan, int humid, int mode) {
        JSONObject json = new JSONObject();
        json.put("temperature", temp);
        json.put("fanSpeed", fan);
        json.put("humidity", humid);
        json.put("mode", mode);
        String msg = json.toString();
        System.out.println("sending: "+msg);
        for (int i = 0; i < clientIrrigationSwitchList.size(); i++) {
            CoapClient client = clientIrrigationSwitchList.get(i);
            client.put(new CoapHandler() {

                public void onLoad(CoapResponse response) {
                    if (response != null) {
                        if (!response.isSuccess())
                            System.out.println("Something went wrong with the actuator");
                    }
                }

                public void onError() {
                    System.err.println("[ERROR: Irrigation " + client.getURI() + "] ");
                }

            }, msg, MediaTypeRegistry.APPLICATION_JSON);
        }
    }

    public void setSwitchStatus(String status, int index) {
        JSONObject json = new JSONObject();
        json.put("switch", status);
        String msg = json.toString();

     
        CoapClient client = clientIrrigationSwitchList.get(index);
        client.put(new CoapHandler() {

            public void onLoad(CoapResponse response) {
                if (response != null) {
                    if (!response.isSuccess())
                        System.out.println("Something went wrong with the switch actuator");
                }
            }

            public void onError() {
                System.err.println("[ERROR: Irrigation " + client.getURI() + "] ");
            }

        }, msg, MediaTypeRegistry.APPLICATION_JSON);
    
    }

}