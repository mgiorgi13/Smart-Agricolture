package iot.unipi.it;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketException;
import java.util.ArrayList;
import java.lang.Thread;  
import org.eclipse.paho.client.mqttv3.MqttException;

import iot.unipi.it.Coap.CoapNetworkHandler;
import iot.unipi.it.Coap.CoapRegistrationServer;
import iot.unipi.it.Database.MysqlManager;
import iot.unipi.it.MQTT.MQTThandler;


public class Automation {
    public static void main(String[] args) {
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

        

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        String command;
        String[] parts;

        double get_temperature=0.0;
        int low_temperature = 15;
        int normal_temperature = 25;
        int high_temperature = 35;
        
        double get_humidity = 0.0;
        int low_humidity = 25;
        int normal_humidity = 50;
        int high_humidity = 75;

        
        int low_soil_humidity = 20;
        int norma_soil_humidity = 50;
        

        int normal_fan_speed = 50;
        
        ArrayList<Integer> nodeId = new ArrayList<>();
        ArrayList<Double> get_soil_humidity = new ArrayList<>();
        int timeMin = 1;
        MysqlManager.deleteAllRecords("humidity");
        MysqlManager.deleteAllRecords("temperature");
        MysqlManager.deleteAllRecords("soilHumidity");

        
        while (true) {
            try {
            
          
                
                get_humidity = MysqlManager.selectHumidity(timeMin);
                get_temperature = MysqlManager.selectTemperature(timeMin);
                
                //System.out.println("humidity" + get_humidity);
                //System.out.println("temperature" + get_temperature);
                
                // se umidità è bassa
                if (get_humidity == 0.0)
                {
                    System.err.println("I'm waiting...");
                    Thread.sleep(1000);
                    continue;
                }
                if (get_humidity <= low_humidity){
                    System.out.println("Condition: Humidity is low-->" + get_humidity);
                    //se temperatura è bassa e switch = off
                    if(get_temperature <=low_temperature){
                        System.out.println("\tCondition: Temperature is low-->" + get_temperature);
                        // se windsow è on
                            // window off
                            coapNetworkHandler.turnOffWindow();
                        //heater_humidify
                        coapNetworkHandler.turnOffConditioner();
                        coapNetworkHandler.activateHeaterHumidifier(normal_temperature,normal_fan_speed,normal_humidity); //temperature, faanspeed,humidity
                        System.out.println("\t\t Action: [turn off windows] + [turn on heater] + [turn on humidifier] + [turn on fan]");
                    }
                    //se tempreatura è normale e switch = off
                    if (low_temperature < get_temperature && get_temperature < high_temperature) {
                        System.out.println("\tCondition: Temperature is normal-->" + get_temperature);
                        // se windsow è on
                            // window off
                            coapNetworkHandler.turnOffWindow();
                        //humidify
                        coapNetworkHandler.turnOffConditioner();
                        coapNetworkHandler.activateHumidifier(normal_fan_speed, normal_humidity); // fan speed ,humidify
                        System.out.println("\t\t Action: [turn off windows] + [turn off heater] + [turn on humidifier] + [turn on fan]");
                    }
                    //se temperatura è alta e switch = off
                    if(get_temperature >= high_temperature){
                        System.out.println("\tCondition: Temperature is high-->" + get_temperature);

                        // se windsow è off
                            // window
                            coapNetworkHandler.turnOnWindow();
                        //humidify
                        coapNetworkHandler.turnOffConditioner();
                        coapNetworkHandler.activateHumidifier(normal_fan_speed, normal_humidity);
                        System.out.println("\t\t Action: [turn on windows] + [turn off heater] + [turn on humidifier] + [turn on fan]");

                    }
                }

                // se umidità è normaleorm
                else if (low_humidity < get_humidity && get_humidity < high_humidity ){
                    System.out.println("Condition: Humidity is normal-->" + get_humidity);

                    //se temperatura è bassa e switch = off
                    if (get_temperature <= low_temperature){ 
                        System.out.println("\tCondition: Temperature is low-->" + get_temperature);
                        // se windsow è on
                            // window off
                            coapNetworkHandler.turnOffWindow();
                        //heater
                        coapNetworkHandler.turnOffConditioner();
                        coapNetworkHandler.activateHeater(normal_temperature,normal_fan_speed); //temperature fanSpeed
                        System.out.println("\t\t Action: [turn off windows] + [turn on heater] + [turn off humidifier] + [turn on fan]");

                    }
                    //se tempreatura è normale e switch = off
                    if (low_temperature < get_temperature && get_temperature < high_temperature){
                        System.out.println("\tCondition: Temperature is normal-->" + get_temperature);

                        // se windsow è on
                            // window off
                            coapNetworkHandler.turnOffWindow();
                        // se contidioner on
                            // conditioner off
                            coapNetworkHandler.turnOffConditioner();
                        System.out.println("\t\t Action: [turn off windows] + [turn off heater] + [turn off humidifier] + [turn off fan]");

                     }
                    //se temperatura è alta e switch = off
                    if(get_temperature >= high_temperature){
                        System.out.println("\tCondition: Temperature is high-->" + get_temperature);

                        // se windsow è off
                            // window on
                            coapNetworkHandler.turnOnWindow();
                        //wind
                        coapNetworkHandler.turnOffConditioner();
                        coapNetworkHandler.activateWind(50); //fanSpeed
                        System.out.println("\t\t Action: [turn on windows] + [turn off heater] + [turn off humidifier] + [turn on fan]");
                    }
                   
                }

                // se umidità è alta
                else if (get_humidity >= high_humidity){
                    System.out.println("Condition: Humidity is high-->" + get_humidity);
                    //se temperatura è bassa e switch = off
                    if(get_temperature <= low_temperature){
                        System.out.println("\tCondition: Temperature is low-->" + get_temperature);
                        // se windsow è on
                            // window off
                            coapNetworkHandler.turnOffWindow();
                        //heater
                        coapNetworkHandler.turnOffConditioner();
                        coapNetworkHandler.activateHeater(normal_temperature,normal_fan_speed);
                        System.out.println("\t\t Action: [turn off windows] + [turn on heater] + [turn off humidifier] + [turn on fan]");
                    }
                    if (get_temperature >= low_temperature)
                        System.out.println("\tCondition: Temperature is not low-->" + get_temperature);

                    {
                        coapNetworkHandler.turnOnWindow();
                        coapNetworkHandler.turnOffConditioner();
                        coapNetworkHandler.activateWind(normal_fan_speed); //fan_speed
                        System.out.println("\t\t Action: [turn on windows] + [turn off heater] + [turn off humidifier] + [turn on fan]");
                    }
                    //se tempreatura è normale e switch = off
                        // se windsow è off
                            // window on
                        //wind 
                    //se temperatura è alta e switch = off
                        // se windsow è off
                            // window
                        //wind
                }
                MysqlManager.selectSoilHumidity(timeMin,  nodeId, get_soil_humidity);
                for(int j=0; j < get_soil_humidity.size(); j++){
                    if (get_humidity == 0.0)
                    {
                        System.err.println("I'm waiting...");
                        Thread.sleep(1000);
                        continue;
                    }
                    if (!coapNetworkHandler.checkDeviceSoilHumidity(j))
                        {
                            System.out.println("There is no actuator to pair with this sensor");
                            continue;
                        }
                    //se umidità del suolo bassa e switch = off
                    if (get_soil_humidity.get(j) <= low_soil_humidity){
                        System.out.println("In Zone"+ nodeId.get(j)+", the soil humidity is NOT good-->"+ get_soil_humidity.get(j));
                        
                        //attivo irrigazione
                        coapNetworkHandler.turnOnIrrigation(j);
                        System.out.println("\t Action: nodeID: " + nodeId.get(j) + " [turn on irrgation]");
                    }
                    else{
                        
                        System.out.println("In Zone"+ nodeId.get(j)+", the soil humidity is good-->"+ get_soil_humidity.get(j));
                        coapNetworkHandler.turnOffIrrigation(j);
                        System.out.println("\t Action: nodeID: " + nodeId.get(j) + " [turn off irrgation]");
                    }
                    //se umidità del suolo è alta è switch = om
                        //spengo l'irrigazione
                }
            System.out.println("_____________________________________________________________________________________________________________________________");
                Thread.sleep(3000);   
            }catch (InterruptedException e) {
                // Gestisci l'eccezione qui
                e.printStackTrace();
            }
            
                
        }
    }
}