package iot.unipi.it.Coap;
import org.json.simple.JSONObject;

import iot.unipi.it.Coap.Resources.Conditioner;

public class CoapNetworkHandler {
    private Conditioner conditioner_actuator = new Conditioner();

    private static CoapNetworkHandler instance = null;

    public static CoapNetworkHandler getInstance() {
        if (instance == null)
            instance = new CoapNetworkHandler();

        return instance;
    }

    /* REGISTER AND UNREGISTER DEVICES */
    public void registerConditioner(String ip) {
        conditioner_actuator.addConditionerActuator(ip);
    }

    public void unregisterConditioner(String ip) {
        conditioner_actuator.deleteConditionerActuator(ip);
    }
    
    /* GET METHODS */
    public String getConditionerStatus(int index) {
        JSONObject json = conditioner_actuator.getStatus(index);
        return json.toJSONString();
    }

    public String getConditionerSwitchStatus(int index) {
        int result = conditioner_actuator.getSwitchStatus(index);
        if (result == 1)
            return "on";
        else if (result == 0)
            return "off";
        else
            return "ERROR";
    }

    /* SET METHODS */
    public void changeConditionerStatus(int temperature, int fanSpeed, int humidity) {
        conditioner_actuator.setStatus(temperature, fanSpeed, humidity);
    }

    public void changeConditionerSwitchStatus(String status) {
        conditioner_actuator.setSwitchStatus(status);
    }

    // General functions
    public void printAllDevices() {
        conditioner_actuator.printDevices();
        // rainSensor.printDevice();
        // soilMoistureNetwork.printDevices();
        // tapActuator.printDevice();
    }
}