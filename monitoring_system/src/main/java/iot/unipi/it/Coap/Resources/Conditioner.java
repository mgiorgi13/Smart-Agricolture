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

public class Conditioner {
    private List<CoapClient> clientConditionerList = new ArrayList<>();
    private List<CoapClient> clientConditionerSwitchList = new ArrayList<>();

    // private List<CoapObserveRelation> observeActuatorList = new ArrayList<>();

    // SERVER SIDE
    public void addConditionerActuator(String ip) {
        Logger.log("The conditioner actuator: [" + ip + "] is now registered");

        // Add the conditioner actuator to the list
        CoapClient newConditionerActuator = new CoapClient("coap://[" + ip + "]/conditioner_actuator");
        CoapClient newConditionerSwitch = new CoapClient("coap://[" + ip + "]/conditioner_switch");
        // observeActuatorList.add(newConditionerActuator.observe(
        // new CoapHandler() {
        // public void onLoad(CoapResponse response) {
        // getHandler(response);
        // }

        // public void onError() {
        // System.err.println("OBSERVING FAILED");
        // }
        // }));
        // IrrigationSystemDbManager.insertTemperature(temperatureDetected);
        clientConditionerList.add(newConditionerActuator);
        clientConditionerSwitchList.add(newConditionerSwitch);
    }

    public void deleteConditionerActuator(String ip) {
        for (int i = 0; i < clientConditionerList.size(); i++) {
            if (clientConditionerList.get(i).getURI().equals(ip)) {
                clientConditionerList.remove(i);
                clientConditionerSwitchList.remove(i);
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
        System.out.println("Conditioner actuators:");
        for (CoapClient cc : clientConditionerList)
            System.out.println("\t" + cc.getURI());
        System.out.println("");
    }

    public JSONObject getStatus(int i) {
        CoapClient client = clientConditionerList.get(i);
        CoapResponse response = client.get();
        return getStatusHandler(response);
    }

    public JSONObject getStatusHandler(CoapResponse response) {
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

    public int getSwitchStatus(int i) {
        CoapClient client = clientConditionerSwitchList.get(i);
        CoapResponse response = client.get();
        return getSwitchHandler(response);
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

    public void setStatus(int temp, int fan, int humid) {
        JSONObject json = new JSONObject();
        json.put("temperature", temp);
        json.put("fanSpeed", fan);
        json.put("humidity", humid);
        String msg = json.toString();

        for (int i = 0; i < clientConditionerList.size(); i++) {
            CoapClient client = clientConditionerList.get(i);
            client.put(new CoapHandler() {

                public void onLoad(CoapResponse response) {
                    if (response != null) {
                        if (!response.isSuccess())
                            System.out.println("Something went wrong with the actuator");
                    }
                }

                public void onError() {
                    System.err.println("[ERROR: Conditioner " + client.getURI() + "] ");
                }

            }, msg, MediaTypeRegistry.APPLICATION_JSON);
        }
    }

    public void setSwitchStatus(String status) {
        JSONObject json = new JSONObject();
        json.put("switch", status);
        String msg = json.toString();

        for (int i = 0; i < clientConditionerSwitchList.size(); i++) {
            CoapClient client = clientConditionerSwitchList.get(i);
            client.put(new CoapHandler() {

                public void onLoad(CoapResponse response) {
                    if (response != null) {
                        if (!response.isSuccess())
                            System.out.println("Something went wrong with the switch actuator");
                    }
                }

                public void onError() {
                    System.err.println("[ERROR: Conditioner " + client.getURI() + "] ");
                }

            }, msg, MediaTypeRegistry.APPLICATION_JSON);
        }
    }

}