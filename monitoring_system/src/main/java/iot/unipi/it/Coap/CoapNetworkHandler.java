package iot.unipi.it.Coap;

import org.json.simple.JSONObject;

import iot.unipi.it.Coap.Resources.Conditioner;
import iot.unipi.it.Coap.Resources.Irrigation;
import iot.unipi.it.Coap.Resources.Window;

public class CoapNetworkHandler {
    private Conditioner conditioner_actuator = new Conditioner();
    private Window window_actuator = new Window();
    private Irrigation irrigation_actuator = new Irrigation();
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
        int mode = ((Long) json.get("mode")).intValue();

        switch (mode) {
            case 1:
                json.put("mode", "heater");
                break;
            case 2:
                json.put("mode", "heater_humidifier");
                break;
            case 3:
                json.put("mode", "humidifier");
                break;
            case 4:
                json.put("mode", "wind");
                break;
            default:
                json.put("mode", "none");
                break;
        }

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

    public void activateHeater(int index, int temperature, int fanSpeed) {
        if (temperature < 10 || temperature > 38) {
            System.out.println("Temperature must be between 18 and 30");
            return;
        }
        if (fanSpeed <= 0 || fanSpeed > 100) {
            System.out.println("Fan speed must be between 1 and 100");
            return;
        }
        conditioner_actuator.setStatus(index, temperature, fanSpeed, 0, 1);
        conditioner_actuator.setSwitchStatus(index, "on");

    }

    public void activateHeaterHumidifier(int index, int temperature, int fanSpeed, int humidity) {
        if (temperature < 10 || temperature > 38) {
            System.out.println("Temperature must be between 18 and 30");
            return;
        }
        if (fanSpeed <= 0 || fanSpeed > 100) {
            System.out.println("Fan speed must be between 1 and 100");
            return;
        }
        if (humidity <= 0 || humidity > 100) {
            System.out.println("Humidity must be between 1 and 100");
            return;
        }
        conditioner_actuator.setStatus(index, temperature, fanSpeed, humidity, 2);
        conditioner_actuator.setSwitchStatus(index, "on");

    }

    public void activateHumidifier(int index, int fanSpeed, int humidity) {
        if (fanSpeed <= 0 || fanSpeed > 100) {
            System.out.println("Fan speed must be between 1 and 100");
            return;
        }
        if (humidity <= 0 || humidity > 100) {
            System.out.println("Humidity must be between 1 and 100");
            return;
        }
        conditioner_actuator.setStatus(index, 0, fanSpeed, humidity, 3);
        conditioner_actuator.setSwitchStatus(index, "on");

    }

    public void activateWind(int index, int fanSpeed) {
        if (fanSpeed <= 0 || fanSpeed > 100) {
            System.out.println("Fan speed must be between 1 and 100");
            return;
        }
        conditioner_actuator.setStatus(index, 0, fanSpeed, 0, 4);
        conditioner_actuator.setSwitchStatus(index, "on");

    }

    public void turnOffConditioner(int index) {
        conditioner_actuator.setSwitchStatus(index, "off");
    }

    /* REGISTER AND UNREGISTER DEVICES */
    public void registerWindow(String ip) {
        window_actuator.addWindowActuator(ip);
    }

    public void unregisterWindow(String ip) {
        window_actuator.deleteWindowActuator(ip);
    }

    /* GET METHODS */

    public String getWindowSwitchStatus(int index) {
        int result = window_actuator.getSwitchStatus(index);
        if (result == 1)
            return "on";
        else if (result == 0)
            return "off";
        else
            return "ERROR";
    }

    /* SET METHODS */

    public void turnOnWindow(int index) {
        window_actuator.setSwitchStatus(index, "on");
    }

    public void turnOffWindow(int index) {
        window_actuator.setSwitchStatus(index, "off");
    }

    /* REGISTER AND UNREGISTER DEVICES */
    public void registerIrrigation(String ip) {
        irrigation_actuator.addIrrigationActuator(ip);
    }

    public void unregisterIrrigation(String ip) {
        irrigation_actuator.deleteIrrigationActuator(ip);
    }

    /* GET METHODS */

    public void getIrrigationSwitchStatus() {
        irrigation_actuator.getSwitchStatus();
    }

    /* SET METHODS */

    public void turnOnIrrigation(int index) {
        irrigation_actuator.setSwitchStatus(index, "on");
    }

    public void turnOffIrrigation(int index) {
        irrigation_actuator.setSwitchStatus(index, "off");
    }

    // General functions
    public void printAllDevices() {
        conditioner_actuator.printDevices();
        window_actuator.printDevices();
        irrigation_actuator.printDevices();
    }

    public boolean checkDeviceSoilHumidity(int index) {
        return irrigation_actuator.checkDeviceSoilHumidity(index);
    }

    public boolean registrationTerminated() {
        return conditioner_actuator.registrationTerminated() && window_actuator.registrationTerminated()
                && irrigation_actuator.registrationTerminated();
    }
}