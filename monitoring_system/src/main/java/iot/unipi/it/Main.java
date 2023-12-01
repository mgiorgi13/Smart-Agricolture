package iot.unipi.it;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.californium.core.coap.MediaTypeRegistry;

import io.netty.handler.codec.mqtt.MqttMessage;

public class Main {
    public static void main(String[] args) {
        // COAP
        // CoapClient client = new CoapClient("coap://fd00::202:2:2:2:5683/leds?color=r");
        // String payload = "mode=on";
        // CoapResponse response = client.put(payload, MediaTypeRegistry.TEXT_PLAIN);
        // System.out.print(response.getResponseText());

        CoapClient client = new CoapClient("coap://[fd00::202:2:2:2]:5683/hello");
        CoapResponse response = client.get();

        if (response!=null) {
            System.out.println(response.getResponseText());
        } else {
            System.out.println("Request failed");
        }

        // MQTT

        // try {
        //     MQTThandler mc = new MQTThandler();
        // } catch (MqttException me) {
        //     me.printStackTrace();
        // }

    }
}