package iot.unipi.it.Coap;
import iot.unipi.it.Coap.Resources.Heater;

public class CoapNetworkHandler {
    // private AirQuality airQuality = new AirQuality();
    // private Light light = new Light();
    // private PresenceSensor presenceSensor = new PresenceSensor();
    private Heater heaterActuator = new Heater();
    private static CoapNetworkHandler instance = null;

    public static CoapNetworkHandler getInstance() {
        if (instance == null)
            instance = new CoapNetworkHandler();

        return instance;
    }

    /* REGISTER AND UNREGISTER DEVICES */
    public void registerHeater(String ip) {
        heaterActuator.addHeaderActuator(ip);
    }

    public void unregisterHeater(String ip) {
        heaterActuator.deleteHeaderActuator(ip);
    }
    
    /* GET METHODS */
    public int getTemperature(int index) {
        return heaterActuator.getTemperature(index);
    }

    /* SET METHODS */
    public void changeTemperature(int value) {
        heaterActuator.putTemperature(String.valueOf(value));
    }

    // General functions
    public void printAllDevices() {
        heaterActuator.printDevices();
        // rainSensor.printDevice();
        // soilMoistureNetwork.printDevices();
        // tapActuator.printDevice();
    }
}