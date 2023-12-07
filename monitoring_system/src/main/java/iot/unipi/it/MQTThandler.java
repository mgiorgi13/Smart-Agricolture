package iot.unipi.it;

import java.text.ParseException;

import org.eclipse.paho.client.mqttv3.*;

import org.json.simple.*;

import iot.unipi.it.Logger.Logger;

public class MQTThandler implements MqttCallback {
    private String temperatureTopic = "temperature";
    private String humidityTopic = "humidity";
    private String soilHumidityTopic = "soilHumidity";

    private String broker = "tcp://127.0.0.1:1883";
    private String clientId = "JavaApp";
    private MqttClient mqttClient = null;

    public MQTThandler() throws MqttException {
        this.mqttClient = new MqttClient(this.broker, this.clientId);
        this.mqttClient.setCallback(this);
        this.topicSubscribe();
    }

    public void topicSubscribe() {
        int timeWindow = 3000;

        do {
            if (timeWindow / 128 == timeWindow) {
                System.out.println("Connection lost with the MQTT broker.");
                System.exit(1);
            }
            try {
                this.mqttClient.connect();

                this.mqttClient.subscribe(this.temperatureTopic);
                this.mqttClient.subscribe(this.humidityTopic);
                this.mqttClient.subscribe(this.soilHumidityTopic);
                if (!this.mqttClient.isConnected()) {
                    Thread.sleep(timeWindow);
                    timeWindow *= 2;
                }
            } catch (MqttException me) {
                me.printStackTrace();
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }

        } while (!this.mqttClient.isConnected());
    }

    public void connectionLost(Throwable cause) {
        this.topicSubscribe();
    }

    public void messageArrived(String topic, MqttMessage message) throws Exception {
        byte[] payload = message.getPayload();
        System.out.println("Received message: " + new String(payload));
        try {
            JSONObject sensorMessage = (JSONObject) JSONValue.parseWithException(new String(payload));

            if (topic.equals(this.temperatureTopic)) {
                if (sensorMessage.containsKey("nodeId")
                        && sensorMessage.containsKey("temperature")
                        && sensorMessage.containsKey("unit")) {
                    Double numericValue = Double.parseDouble(sensorMessage.get("temperature").toString());
                    String nodeId = sensorMessage.get("nodeId").toString();
                
                    Logger.log(String.format("[MQTT Java Client]: Received temperature value from node %s: %f %s", nodeId, numericValue, sensorMessage.get("unit").toString()));

                    //receivedReservoirSamples.put(nodeId, numericValue);
                    //IrrigationSystemDbManager.insertWaterLevReservoir(nodeId, numericValue);

                } else {
                    System.out.println("Garbage data from sensor");
                }
            } else {
                Logger.warning(String.format("[MQTT Java Client]: Unknown topic: [%s] %s", topic, new String(payload)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deliveryComplete(IMqttDeliveryToken token) {
        // TODO Auto-generated method stub
    }

}
