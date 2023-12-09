package iot.unipi.it;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketException;
import org.eclipse.paho.client.mqttv3.MqttException;

import iot.unipi.it.Coap.CoapNetworkHandler;
import iot.unipi.it.Coap.CoapRegistrationServer;
import iot.unipi.it.Database.MysqlManager;
import iot.unipi.it.MQTT.MQTThandler;

public class Main {
    public static void main(String[] args) {
        MQTThandler mqttHandler;
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

        printAvailableCommands();

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        String command;
        String[] parts;

        while (true) {
            System.out.print("> ");
            try {
                command = bufferedReader.readLine();
                parts = command.split(" ");
                String status;
                switch (parts[0]) {
                    case "!help":
                        helpFunction(parts);
                        break;
                    case "!get_conditioner_status":
                        // controllare che la lista di client non sia vuota
                        status = coapNetworkHandler.getConditionerStatus(0);
                        System.out.println(status);
                        break;
                    case "!get_conditioner_switch":
                        status = coapNetworkHandler.getConditionerSwitchStatus(0);
                        System.out.println(status);
                        break;
                    case "!turn_on_heater_humidifier":
                        coapNetworkHandler.activateHeaterHumidifier(Integer.parseInt(parts[1]),
                                Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
                        break;
                    case "!turn_on_heater":
                        coapNetworkHandler.activateHeater(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
                        break;
                    case "!turn_on_humidifier":
                        coapNetworkHandler.activateHumidifier(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
                        break;
                    case "!turn_on_wind":
                        coapNetworkHandler.activateWind(Integer.parseInt(parts[1]));
                        break;
                    case "!turn_off_conditioner":
                        coapNetworkHandler.turnOffConditioner();
                        break;
                    case "!get_window_switch_status":
                        status = coapNetworkHandler.getWindowSwitchStatus(0);
                        System.out.println(status);
                        break;
                    case "!turn_on_windows":
                        coapNetworkHandler.turnOnWindow();
                        break;
                    case "!turn_off_windows":
                        coapNetworkHandler.turnOffWindow();
                        break;
                    case "!get_irrigation_switch_status":
                        coapNetworkHandler.getIrrigationSwitchStatus();
                        break;
                    case "!turn_on_irrigation":
                        coapNetworkHandler.turnOnIrrigation(Integer.parseInt(parts[1]));
                        break;
                    case "!turn_off_irrigation":
                        coapNetworkHandler.turnOffIrrigation(Integer.parseInt(parts[1]));
                        break;
                    case "!get_avg_soil_humidity":
                        MysqlManager.selectSoilHumidity(Integer.parseInt(parts[1]));
                        break;
                    case "!get_avg_temperature":
                        MysqlManager.selectTemperature(Integer.parseInt(parts[1]));
                        System.out.println("AVG Temperature: " + MysqlManager.selectTemperature(Integer.parseInt(parts[1])) + "°C\n");
                        break;
                    case "!get_avg_humidity":
                        MysqlManager.selectHumidity(Integer.parseInt(parts[1]));
                        System.out.println("AVG Humidity: " + MysqlManager.selectHumidity(Integer.parseInt(parts[1])) + "%\n");
                        break;
                    case "!print_all_device":
                        coapNetworkHandler.printAllDevices();
                        break;
                    case "!exit":
                        System.out.println("Bye!");
                        System.exit(0);
                        break;
                    default:
                        System.out.println("Command not recognized, try again\n");
                        break;
                }
                printAvailableCommands();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void printAvailableCommands() {
        System.out.println("***************************** Smart Agricolture *****************************\n" +
                "The following commands are available:\n" +
                "1) !help <command> --> shows the details of a command\n" +
                "2) !get_conditioner_status --> shows the status of the conditioner\n" +
                "3) !get_conditioner_switch --> shows the switch status of the conditioner\n" +
                "4) !turn_on_heater <temperature> <fanSpeed> --> activates the heater\n" +
                "5) !turn_on_heater_humidifier <temperature> <fanSpeed> <humidity>--> activates the heater-humidifier\n"
                +
                "6) !turn_on_humidifier <fanSpeed> <humidity> --> activates the umidifier\n" +
                "7) !turn_on_wind <fanSpeed> --> activates the wind\n" +
                "8) !turn_off_conditioner --> turns off the conditioner\n" +
                "9) !get_window_switch_status --> shows the switch status of the window\n" +
                "10) !turn_on_windows --> open the window \n" +
                "11) !turn_off_windows --> closed the window\n" +
                "12) !get_irrigation_switch_status --> shows the switch status of the irrigation\n" +
                "13) !turn_on_irrigation <index> --> I turn on the index-th irrigation actuator \n" +
                "14) !turn_off_irrigation <index> --> I turn off the index-th irrigation actuator\n" +
                "15) !print_all_device --> all device acutator device\n" +
                "16) !get_avg_soil_humidity <number> --> get the average soil humidity of the last number of minutes for each nodeId\n"
                +
                "17) !get_avg_temperature <number> --> get the average temperature of the last number of minutes\n" +
                "18) !get_avg_humidity <number> --> get the average humidity of the last number of minutes\n" +
                "16) !exit --> terminates the program\n");

    }

    private static void helpFunction(String[] parts) {
        if (parts.length != 2) {
            System.out.println("Incorrect use of the command. Please use !help <command>\n");
        } else {
            switch (parts[1]) {
                case "!help":
                    System.out.println("!help shows the details of the command passed as parameter.\n");
                    break;
                case "!get_conditioner_status":
                    System.out.println("!get_conditioner_status shows the status of the conditioner.\n");
                    break;
                case "!get_conditioner_switch":
                    System.out.println("!get_conditioner_switch shows the switch status of the conditioner.\n");
                    break;
                case "!turn_on_heater":
                    System.out.println("!turn_on_heater <temperature> <fanSpeed> activates the heater.\n");
                    break;
                case "!turn_on_heater_humidifier":
                    System.out.println(
                            "!turn_on_heater_humidifier <temperature> <fanSpeed> <humidity> activates the heater-humidifier.\n");
                    break;
                case "!turn_on_humidifier":
                    System.out.println("!turn_on_humidifier <fanSpeed> <humidity> activates the umidifier.\n");
                    break;
                case "!turn_on_wind":
                    System.out.println("!turn_on_wind <fanSpeed> activates the wind.\n");
                    break;
                case "!turn_off_conditioner":
                    System.out.println("!turn_off_conditioner turns off the conditioner.\n");
                    break;
                case "!get_window_switch_status":
                    System.out.println("!get_window_switch_status shows the switch status of the window.\n");
                    break;
                case "!turn_on_windows":
                    System.out.println("!turn_on_windows open the window.\n");
                    break;
                case "!turn_off_windows":
                    System.out.println("!turn_off_windows closed the window.\n");
                    break;
                case "!get_irrigation_switch_status":
                    System.out.println("!get_irrigation_switch_status shows the switch status of the irrigation\n");
                    break;
                case "!turn_on_irrigation":
                    System.out.println("!turn_on_irrigation I turn on the index-th irrigation actuator\n");
                    break;
                case "!turn_off_irrigation":
                    System.out.println("!turn_off_irrigation I turn on the index-th irrigation actuator.\n");
                    break;
                case "!print_all_device":
                    System.out.println("!print_all_device print all actuator devices.\n");
                    break;
                case "!get_avg_soil_humidity":
                    System.out.println(
                            "!get_avg_soil_humidity <number> get the average soil humidity of the last number of minutes for each nodeId.\n");
                    break;
                case "!get_avg_temperature":
                    System.out.println(
                            "!get_avg_temperature <number> get the average temperature of the last number of minutes.\n");
                    break;
                case "!get_avg_humidity":
                    System.out.println(
                            "!get_avg_humidity <number> get the average humidity of the last number of minutes.\n");
                    break;
                case "!exit":
                    System.out.println("!exit allows you to terminate the program.\n");
                    break;
                default:
                    System.out.println("Command not recognized, try again\n");
                    break;
            }
        }
    }
}
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


public class Main {
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
        int timeMin = 5;
        
        
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
                    Thread.sleep(100000);
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
                    if (low_temperature < get_temperature && get_temperature < high_humidity) {
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
                    if (low_temperature < get_temperature && get_temperature < high_humidity){
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
                    if (get_temperature >= low_humidity)
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
                    Thread.sleep(100000);
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