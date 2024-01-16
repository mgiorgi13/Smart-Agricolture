package iot.unipi.it;

import java.net.SocketException;
import java.util.ArrayList;

import org.eclipse.paho.client.mqttv3.MqttException;

import iot.unipi.it.Coap.CoapNetworkHandler;
import iot.unipi.it.Coap.CoapRegistrationServer;
import iot.unipi.it.Database.MysqlManager;
import iot.unipi.it.MQTT.MQTThandler;

public class Automation {
    private static final int conditionerIndex = 0;
    private static final int windowIndex = 0;

    public static void main(String[] args) {
        // get from args start_temp and start_hum as int
        if (args.length < 2) {
            System.out.println("Please provide start_temp and start_hum as arguments.");
            System.exit(1);
        }

        // Parse the arguments to integers
        int start_temp = Integer.parseInt(args[0]);
        int start_hum = Integer.parseInt(args[1]);

        MQTThandler mqttHandler = null;
        try {
            mqttHandler = new MQTThandler();
        } catch (MqttException e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        CoapRegistrationServer coapRegistrationServer;
        try {
            coapRegistrationServer = new CoapRegistrationServer();
            coapRegistrationServer.start();
        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        CoapNetworkHandler coapNetworkHandler = CoapNetworkHandler.getInstance();

        //low humidity
        //java -jar target/automation_system-1.0-SNAPSHOT-jar-with-dependencies.jar 10 20
        //java -jar target/automation_system-1.0-SNAPSHOT-jar-with-dependencies.jar 25 20
        //java -jar target/automation_system-1.0-SNAPSHOT-jar-with-dependencies.jar 38 20
        //medium humidity
        //java -jar target/automation_system-1.0-SNAPSHOT-jar-with-dependencies.jar 10 50
        //java -jar target/automation_system-1.0-SNAPSHOT-jar-with-dependencies.jar 25 50
        //java -jar target/automation_system-1.0-SNAPSHOT-jar-with-dependencies.jar 38 50
        //high humidity 
        //java -jar target/automation_system-1.0-SNAPSHOT-jar-with-dependencies.jar 10 78
        //java -jar target/automation_system-1.0-SNAPSHOT-jar-with-dependencies.jar 25 78

        double get_temperature = 0.0;
        int low_temperature = 15;
        int normal_temperature = 25;
        int high_temperature = 35;

        double get_humidity = 0.0;
        int low_humidity = 25;
        int normal_humidity = 50;
        int high_humidity = 75;

        int low_soil_humidity = 20;
        int normal_soil_humidity = 65;
        int normal_fan_speed = 50;

        ArrayList<Integer> nodeId = new ArrayList<>();
        ArrayList<Double> get_soil_humidity = new ArrayList<>();
        int timeSec = 20;
        MysqlManager.deleteAllRecords("humidity");
        MysqlManager.deleteAllRecords("temperature");
        MysqlManager.deleteAllRecords("soilHumidity");

        try {
            int count = 4;
            // loop for wainting until we reach starting condition
            while (true) {
                get_humidity = MysqlManager.selectHumidity(timeSec);
                get_temperature = MysqlManager.selectTemperature(timeSec);
                // wait for mqtt sensor to start publishing
                if (get_humidity == 0.0) {
                    System.err.println("Waiting for humidity and temperature data");
                    Thread.sleep(1000);
                    continue;
                } else if (count == 4) {
                    mqttHandler.sendTemperatureHumidityCondition(start_temp, 5, start_hum, 5);
                    count--;
                }
                // wait for all coap resource to be registered
                if (!coapNetworkHandler.registrationTerminated()) {
                    System.out.println("Waiting for registration of all actuators");
                    Thread.sleep(1000);
                    continue;
                }
                if (get_humidity == start_hum && get_temperature == start_temp)
                    count--;
                if (count == 0) {
                    System.out.println("Starting condition reached");
                    break;
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        while (true) {
            try {
                get_humidity = MysqlManager.selectHumidity(timeSec);
                get_temperature = MysqlManager.selectTemperature(timeSec);

                if (get_humidity <= low_humidity) {
                    System.out.println("Condition: Humidity is low-->" + get_humidity);
                    // se temperatura è bassa e switch = off
                    if (get_temperature <= low_temperature) {
                        System.out.println("\tCondition: Temperature is low-->" + get_temperature);
                        coapNetworkHandler.turnOffWindow(windowIndex);
                        // coapNetworkHandler.turnOffConditioner(conditionerIndex);
                        coapNetworkHandler.activateHeaterHumidifier(conditionerIndex, normal_temperature,
                                normal_fan_speed, normal_humidity); // temperature, fanspeed, humidity
                        mqttHandler.sendTemperatureHumidityCondition(normal_temperature, 1, normal_humidity, 3);
                        System.out.println(
                                "\t\t Action: [turn off windows] + [turn on heater] + [turn on humidifier] + [turn on fan]");
                    }
                    // se temperatura è normale e switch = off
                    if (low_temperature < get_temperature && get_temperature < high_temperature) {
                        System.out.println("\tCondition: Temperature is normal-->" + get_temperature);
                        coapNetworkHandler.turnOffWindow(windowIndex);
                        // coapNetworkHandler.turnOffConditioner(conditionerIndex);
                        coapNetworkHandler.activateHumidifier(conditionerIndex, normal_fan_speed, normal_humidity); // fan,
                                                                                                                    // humidify
                        mqttHandler.sendTemperatureHumidityCondition(-1, 0, normal_humidity, 3);
                        System.out.println(
                                "\t\t Action: [turn off windows] + [turn off heater] + [turn on humidifier] + [turn on fan]");
                    }
                    // se temperatura è alta e switch = off
                    if (get_temperature >= high_temperature) {
                        System.out.println("\tCondition: Temperature is high-->" + get_temperature);
                        coapNetworkHandler.turnOnWindow(windowIndex);
                        coapNetworkHandler.turnOffConditioner(conditionerIndex);
                        coapNetworkHandler.activateHumidifier(conditionerIndex, normal_fan_speed, normal_humidity);

                        mqttHandler.sendTemperatureHumidityCondition(normal_temperature, 1, normal_humidity, 3);
                        System.out.println(
                                "\t\t Action: [turn on windows] + [turn off heater] + [turn on humidifier] + [turn on fan]");

                    }
                }

                // se umidità è normale
                else if (low_humidity < get_humidity && get_humidity < high_humidity) {
                    System.out.println("Condition: Humidity is normal-->" + get_humidity);

                    // se temperatura è bassa e switch = off
                    if (get_temperature <= low_temperature) {
                        System.out.println("\tCondition: Temperature is low-->" + get_temperature);
                        // se windsow è on
                        // window off
                        coapNetworkHandler.turnOffWindow(windowIndex);
                        // heater
                        // coapNetworkHandler.turnOffConditioner(conditionerIndex);
                        coapNetworkHandler.activateHeater(conditionerIndex, normal_temperature, normal_fan_speed); // temperature
                                                                                                                   // fanSpeed
                        mqttHandler.sendTemperatureHumidityCondition(normal_temperature, 1, -1, 0);
                        System.out.println(
                                "\t\t Action: [turn off windows] + [turn on heater] + [turn off humidifier] + [turn on fan]");
                    }
                    // se tempreatura è normale e switch = off
                    if (low_temperature < get_temperature && get_temperature < high_temperature) {
                        System.out.println("\tCondition: Temperature is normal-->" + get_temperature);

                        // se windsow è on
                        // window off
                        coapNetworkHandler.turnOffWindow(windowIndex);
                        // se contidioner on
                        // conditioner off
                        coapNetworkHandler.turnOffConditioner(conditionerIndex);

                        mqttHandler.sendTemperatureHumidityCondition(-1, 0, -1, 0);
                        System.out.println(
                                "\t\t Action: [turn off windows] + [turn off heater] + [turn off humidifier] + [turn off fan]");

                    }
                    // se temperatura è alta e switch = off
                    if (get_temperature >= high_temperature) {
                        System.out.println("\tCondition: Temperature is high-->" + get_temperature);

                        // se windsow è off
                        // window on
                        coapNetworkHandler.turnOnWindow(windowIndex);
                        // wind
                        // coapNetworkHandler.turnOffConditioner(conditionerIndex);
                        coapNetworkHandler.activateWind(conditionerIndex, 50); // fanSpeed
                        mqttHandler.sendTemperatureHumidityCondition(normal_temperature, 2, -1, 0);
                        System.out.println(
                                "\t\t Action: [turn on windows] + [turn off heater] + [turn off humidifier] + [turn on fan]");
                    }

                }

                // se umidità è alta
                else if (get_humidity >= high_humidity) {
                    System.out.println("Condition: Humidity is high-->" + get_humidity);
                    // se temperatura è bassa e switch = off
                    if (get_temperature <= low_temperature) {
                        System.out.println("\tCondition: Temperature is low-->" + get_temperature);
                        // se windsow è on
                        // window off
                        coapNetworkHandler.turnOffWindow(windowIndex);
                        // heater
                        // coapNetworkHandler.turnOffConditioner(conditionerIndex);
                        coapNetworkHandler.activateHeater(conditionerIndex, normal_temperature, normal_fan_speed);
                        mqttHandler.sendTemperatureHumidityCondition(normal_temperature, 1, normal_humidity, 1);
                        System.out.println(
                                "\t\t Action: [turn off windows] + [turn on heater] + [turn off humidifier] + [turn on fan]");
                    } else {
                        System.out.println("\tCondition: Temperature is not low-->" + get_temperature);

                        coapNetworkHandler.turnOnWindow(windowIndex);
                        // coapNetworkHandler.turnOffConditioner(conditionerIndex);
                        coapNetworkHandler.activateWind(conditionerIndex, normal_fan_speed); // fan_speed
                        mqttHandler.sendTemperatureHumidityCondition(-1, 0, normal_humidity, 3);

                        System.out.println(
                                "\t\t Action: [turn on windows] + [turn off heater] + [turn off humidifier] + [turn on fan]");

                    }
                }

                MysqlManager.selectSoilHumidity(timeSec, nodeId, get_soil_humidity);
                for (int j = 0; j < get_soil_humidity.size(); j++) {
                    if (get_humidity == 0.0) {
                        System.err.println("Waiting for soil humidity data");
                        Thread.sleep(1000);
                        continue;
                    }
                    if (!coapNetworkHandler.checkDeviceSoilHumidity(j)) {
                        System.out.println("There is no actuator to pair with this sensor");
                        continue;
                    }
                    // se umidità del suolo bassa e switch = off
                    if (get_soil_humidity.get(j) <= low_soil_humidity) {
                        System.out.println("In Zone" + nodeId.get(j) + ", the soil humidity is NOT good-->"
                                + get_soil_humidity.get(j));

                        // attivo irrigazione
                        coapNetworkHandler.turnOnIrrigation(j);
                        mqttHandler.sendIrrigation(j, normal_soil_humidity, 2);
                        System.out.println("\t Action: nodeID: " + nodeId.get(j) + " [turn on irrigation]");
                    } else {

                        System.out.println("In Zone" + nodeId.get(j) + ", the soil humidity is good-->"
                                + get_soil_humidity.get(j));
                        if (get_soil_humidity.get(j) >= normal_soil_humidity) {
                            coapNetworkHandler.turnOffIrrigation(j);
                            mqttHandler.sendIrrigation(j, -1, 0);
                            System.out.println("\t Action: nodeID: " + nodeId.get(j) + " [turn off irrigation]");
                        }
                    }
                    // se umidità del suolo è alta è switch = on
                    // spengo l'irrigazione
                }
                System.out.println(
                        "_____________________________________________________________________________________________________________________________");
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                // Gestisci l'eccezione qui
                System.out.println("Thread has been interrupted");
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}