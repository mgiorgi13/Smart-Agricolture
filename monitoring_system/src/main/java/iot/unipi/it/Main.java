package iot.unipi.it;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketException;
import org.eclipse.paho.client.mqttv3.MqttException;

import iot.unipi.it.Coap.CoapNetworkHandler;
import iot.unipi.it.Coap.CoapRegistrationServer;
import iot.unipi.it.MQTT.MQTThandler;

public class Main {
    public static void main(String[] args) {
        try {
            MQTThandler mqttHandler = new MQTThandler();
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
                        coapNetworkHandler.activateHeaterHumidifier(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
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
                "5) !turn_on_heater_humidifier <temperature> <fanSpeed> <humidity>--> activates the heater-humidifier\n" +    
                "6) !turn_on_humidifier <fanSpeed> <humidity> --> activates the umidifier\n" +
                "7) !turn_on_wind <fanSpeed> --> activates the wind\n" +
                "8) !turn_off_conditioner --> turns off the conditioner\n" +
                "11) !exit --> terminates the program\n");
    }

    private static void helpFunction(String[] parts) {
        if (parts.length != 2) {
            System.out.println("Incorrect use of the command. Please use !help <command>\n");
        } else {
            switch (parts[1]) {
                case "!help":
                case "help":
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
                    System.out.println("!turn_on_heater_humidifier <temperature> <fanSpeed> <humidity> activates the heater-humidifier.\n");
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
                case "!exit":
                case "exit":
                    System.out.println("!exit allows you to terminate the program.\n");
                    break;
                default:
                    System.out.println("Command not recognized, try again\n");
                    break;
            }
        }
    }
}