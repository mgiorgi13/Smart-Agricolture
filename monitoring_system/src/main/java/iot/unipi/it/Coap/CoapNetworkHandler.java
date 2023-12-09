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

    public void activateHeater(int temperature, int fanSpeed) {
        if (temperature < 18 || temperature > 30) {
            System.out.println("Temperature must be between 18 and 30");
            return;
        }
        if (fanSpeed <= 0 || fanSpeed > 100) {
            System.out.println("Fan speed must be between 1 and 100");
            return;
        }
        conditioner_actuator.setStatus(temperature, fanSpeed, 0, 1);
        conditioner_actuator.setSwitchStatus("on");

    }

    public void activateCooler(int temperature, int fanSpeed) {
        if (temperature < 18 || temperature > 30) {
            System.out.println("Temperature must be between 18 and 30");
            return;
        }
        if (fanSpeed <= 0 || fanSpeed > 100) {
            System.out.println("Fan speed must be between 1 and 100");
            return;
        }
        conditioner_actuator.setStatus(temperature, fanSpeed, 0, 2);
        conditioner_actuator.setSwitchStatus("on");

    }

    public void activateHumidifier(int fanSpeed, int humidity) {
        if (fanSpeed <= 0 || fanSpeed > 100) {
            System.out.println("Fan speed must be between 1 and 100");
            return;
        }
        if (humidity <= 0 || humidity > 100) {
            System.out.println("Humidity must be between 1 and 100");
            return;
        }
        conditioner_actuator.setStatus(0, fanSpeed, humidity, 3);
        conditioner_actuator.setSwitchStatus("on");

    }

    public void activateWind(int fanSpeed) {
        if (fanSpeed <= 0 || fanSpeed > 100) {
            System.out.println("Fan speed must be between 1 and 100");
            return;
        }
        conditioner_actuator.setStatus(0, fanSpeed, 0, 4);
        conditioner_actuator.setSwitchStatus("on");

    }

    public void turnOffConditioner() {
        conditioner_actuator.setSwitchStatus("off");
    }

    // General functions
    public void printAllDevices() {
        conditioner_actuator.printDevices();
        // rainSensor.printDevice();
        // soilMoistureNetwork.printDevices();
        // tapActuator.printDevice();
    }
}