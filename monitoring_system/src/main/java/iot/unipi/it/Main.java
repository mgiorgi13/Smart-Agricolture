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
                int temp;
                switch (parts[0]) {
                    case "!help":
                        helpFunction(parts);
                        break;
                    case "!get_temperature":
                        temp = CNH.getTemperature(0);
                        System.out.println(temp);
                        break;
                    case "!put_temperature":
                        //todo va controllato il parts[1]
                        CNH.changeTemperature(Integer.parseInt(parts[1]));
                        break;
                    // case "!set_humidity":
                    // setHumidityFunction(parts, mqttNetworkHandler.getHumidityCollector());
                    // break;
                    // case "!set_temperature":
                    // setTemperatureFunction(parts, mqttNetworkHandler.getTemperatureCollector());
                    // break;
                    // case "!get_air_quality":
                    // getAirQualityFunction(coapRegistrationServer);
                    // break;
                    // case "!set_air_quality":
                    // setAirQualityFunction(coapRegistrationServer, parts);
                    // break;
                    // case "!set_color":
                    // setColorFunction(coapRegistrationServer, parts);
                    // break;
                    // case "!get_number_of_people":
                    // getNumberOfPeopleFunction(coapRegistrationServer);
                    // break;
                    // case "!set_max_number_of_people":
                    // setMaxNumberOfPeopleFunction(coapRegistrationServer, parts);
                    // break;
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
        System.out.println("***************************** SMART SAUNA *****************************\n" +
                "The following commands are available:\n" +
                "1) !help <command> --> shows the details of a command\n" +
                "2) !get_humidity --> recovers the last humidity measurement\n" +
                "3) !set_humidity <lower bound> <upper bound> --> sets the range within which the humidity must stay\n"
                +
                "4) !get_temperature --> recovers the last temperature measurement\n" +
                "5) !set_temperature <lower bound> <upper_bound> --> sets the range within which the temperature must stay\n"
                +
                "6) !get_air_quality --> recovers the last CO2 concentration measurement\n" +
                "7) !set_air_quality <upper bound> --> sets the limit below which the CO2 concentration must stay\n" +
                "8) !set_color <color> --> sets the light color (GREEN, YELLOW or RED)\n" +
                "9) !get_number_of_people --> retrieves the number of people inside the sauna\n" +
                "10) !set_max_number_of_people <number> --> sets a limit on the number of people who can enter\n" +
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
                case "!get_air_quality":
                case "get_air_quality":
                    System.out.println(
                            "!get_air_quality allows you to retrieve the CO2 level inside the sauna, expressed in parts per million (ppm).\n");
                    break;
                case "!set_air_quality":
                case "set_air_quality":
                    System.out.println(
                            "!set_air_quality allows you to set the maximum level of CO2 that can be inside the sauna.\n"
                                    +
                                    "One parameter is required: the upper bound.\n");
                    break;
                case "!set_color":
                case "set_color":
                    System.out.println("!set_color allows you to set the color of the light inside the sauna.\n" +
                            "A parameter is required, i.e. the color, which can take three values: GREEN, YELLOW or RED. \n");
                    break;
                case "!get_number_of_people":
                case "get_number_of_people":
                    System.out.println(
                            "!get_number_of_people allows you to retrieve the number of people who are inside the sauna.\n");
                    break;
                case "!set_max_number_of_people":
                case "set_max_number_of_people":
                    System.out.println(
                            "!set_max_number_of_people allows you to set the maximum number of people who can be inside the sauna at the same time.\n"
                                    +
                                    "One parameter is required: the maximum number of people\n");
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