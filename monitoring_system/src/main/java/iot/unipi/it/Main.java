package iot.unipi.it;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;

import io.netty.handler.codec.mqtt.MqttMessage;

public class Main {
    public static void main(String[] args) {
        // COAP
        // CoapClient client = new CoapClient("coap://fd00::202:2:2:2/leds?color=r");

        // String payload = "mode=on";
        // CoapResponse response = client.put(payload, 0);

        // System.out.print(response.getResponseText());

        // MQTT

        try {
            MQTThandler mc = new MQTThandler();
        } catch (MqttException me) {
            me.printStackTrace();
        }

    }
}