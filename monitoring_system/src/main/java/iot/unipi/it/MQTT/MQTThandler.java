package iot.unipi.it.MQTT;

import org.eclipse.paho.client.mqttv3.*;

import org.json.simple.*;

import iot.unipi.it.Database.MysqlManager;
import iot.unipi.it.Logger.Logger;

public class MQTThandler implements MqttCallback {
    private String temperature_humidityTopic = "temperature_humidity";
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

                this.mqttClient.subscribe(this.temperature_humidityTopic);
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
        try {
            JSONObject sensorMessage = (JSONObject) JSONValue.parseWithException(new String(payload));

            if (topic.equals(this.temperature_humidityTopic)) {
                if (sensorMessage.containsKey("nodeId")
                        && sensorMessage.containsKey("temperature")
                        && sensorMessage.containsKey("unit")
                        && sensorMessage.containsKey("umidity")
                        && sensorMessage.containsKey("type")) {
                    Double numericTemperatureValue = Double.parseDouble(sensorMessage.get("temperature").toString());
                    Double numericUmidityValue = Double.parseDouble(sensorMessage.get("umidity").toString());
                    String nodeId = sensorMessage.get("nodeId").toString();

                    Logger.log(String.format(
                            "[MQTT Java Client]: Received temperature_humidity value from node %s: %f %s, %f %s",
                            nodeId, numericTemperatureValue, sensorMessage.get("unit").toString(),
                            numericUmidityValue, sensorMessage.get("type").toString()));

                    MysqlManager.insertTemperatureAndUmidity(nodeId, numericTemperatureValue, numericUmidityValue);

                } else {
                    System.out.println("Garbage data from sensor");
                }
            } else if (topic.equals(this.soilHumidityTopic)) {
                if (sensorMessage.containsKey("nodeId")
                        && sensorMessage.containsKey("soil_umidity")
                        && sensorMessage.containsKey("type")) {
                    Double numericSoilUmidityValue = Double.parseDouble(sensorMessage.get("soil_umidity").toString());
                    String nodeId = sensorMessage.get("nodeId").toString();
                    Logger.log(
                            String.format("[MQTT Java Client]: Received soilHumidity value from node %s: %f %s",
                                    nodeId, numericSoilUmidityValue, sensorMessage.get("type").toString()));

                    MysqlManager.insertSoilMoistureValue(nodeId, numericSoilUmidityValue);
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
