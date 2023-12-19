package iot.unipi.it.Coap.Resources;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.List;
import iot.unipi.it.Logger.Logger;

public class Window {
    private List<CoapClient> clientWindowSwitchList = new ArrayList<>();

    // SERVER SIDE
    public void addWindowActuator(String ip) {
        Logger.log("The Window actuator: [" + ip + "] is now registered");

        // Add the Window actuator to the list
        CoapClient newWindowSwitch = new CoapClient("coap://[" + ip + "]/window_switch");
        clientWindowSwitchList.add(newWindowSwitch);
    }

    public void deleteWindowActuator(String ip) {
        for (int i = 0; i < clientWindowSwitchList.size(); i++) {
            if (clientWindowSwitchList.get(i).getURI().equals(ip)) {
                clientWindowSwitchList.remove(i);
            }
        }
    }

    // CLIENT SIDE
    public void printDevices() {
        int i = 0;
        System.out.println("Window actuators:");
        for (CoapClient cc : clientWindowSwitchList)
            System.out.println("\t" + i + ": " + cc.getURI());
        System.out.println("");
    }

    public int getSwitchStatus(int i) {
        CoapClient client = clientWindowSwitchList.get(i);
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

    public void setSwitchStatus(int index, String status) {
        JSONObject json = new JSONObject();
        json.put("switch", status);
        String msg = json.toString();

        CoapClient client = clientWindowSwitchList.get(index);
        client.put(new CoapHandler() {

            public void onLoad(CoapResponse response) {
                if (response != null) {
                    if (!response.isSuccess())
                        System.out.println("Something went wrong with the switch actuator");
                }
            }

            public void onError() {
                System.err.println("[ERROR: Window " + client.getURI() + "] ");
            }

        }, msg, MediaTypeRegistry.APPLICATION_JSON);

    }

    public boolean registrationTerminated() {
        return clientWindowSwitchList.size() > 0;
    }

}