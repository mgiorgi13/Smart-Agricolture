package iot.unipi.it;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketException;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.californium.core.coap.MediaTypeRegistry;

import io.netty.handler.codec.mqtt.MqttMessage;
import iot.unipi.it.Coap.CoapNetworkHandler;
import iot.unipi.it.Coap.CoapRegistrationServer;

public class Main {
    public static void main(String[] args) {
        CoapRegistrationServer CRS;
        try {
            CRS = new CoapRegistrationServer();
            CRS.start();
        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        CoapNetworkHandler CNH = CoapNetworkHandler.getInstance();

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
                        status = CNH.getConditionerStatus(0);
                        System.out.println(status);
                        break;
                    case "!get_conditioner_switch":
                        status = CNH.getConditionerSwitchStatus(0);
                        System.out.println(status);
                        break;
                    case "!put_conditioner_status":
                        //todo va controllato il parts[1]
                        CNH.changeConditionerStatus(Integer.parseInt(parts[1]),Integer.parseInt(parts[2]),Integer.parseInt(parts[3]));
                        break;
                    case "!put_conditioner_switch":
                        //todo va controllato il parts[1]
                        CNH.changeConditionerSwitchStatus(parts[1]);
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
                "2) !get_humidity --> recovers the last humidity measurement\n" +
                "3) !set_humidity <lower bound> <upper bound> --> sets the range within which the humidity must stay\n" +
                "4) !get_temperature --> recovers the last temperature measurement\n" +
                "5) !set_temperature <lower bound> <upper_bound> --> sets the range within which the temperature must stay\n" +
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
                case "!get_humidity":
                case "get_humidity":
                    System.out.println(
                            "!get_humidity allows to retrieve the percentage value of humidity in the air inside the sauna.\n");
                    break;
                case "!set_humidity":
                case "set_humidity":
                    System.out.println(
                            "!set_humidity allows you to set the range within which the humidity level should be found inside the sauna.\n"
                                    +
                                    "Two parameters are required: the lower and the upper bounds.\n");
                    break;
                case "!get_temperature":
                case "get_temperature":
                    System.out.println(
                            "!get_temperature allows to retrieve the temperature inside the sauna, expressed in degrees Celsius.\n");
                    break;
                case "!set_temperature":
                case "set_temperature":
                    System.out.println(
                            "!set_temperature allows you to set the range within which the temperature should be inside the sauna.\n"
                                    +
                                    "Two parameters are required: the lower and the upper bounds.\n");
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