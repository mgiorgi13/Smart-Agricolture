package iot.unipi.it.MQTT;

import java.util.ArrayList;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import iot.unipi.it.Database.MysqlManager;
import iot.unipi.it.Logger.Logger;

public class MQTThandler implements MqttCallback {
    private String temperature_humidityTopic = "temperature_humidity";
    private String soilHumidityTopic = "soilHumidity";
    private String irrigation = "irrigation";
    private String temperature_humidity_condition = "condition";
    private ArrayList<Integer> temperature_humidityList;
    private ArrayList<Integer> soil_humidityList;

    private String broker = "tcp://127.0.0.1:1883";
    private String clientId = "JavaApp";
    private MqttClient mqttClient = null;

    private final MqttConnectOptions connOpts = new MqttConnectOptions();

    public MQTThandler() throws MqttException {
        this.mqttClient = new MqttClient(this.broker, this.clientId);
        this.mqttClient.setCallback(this);
        this.topicSubscribe();

        temperature_humidityList = new ArrayList<>();
        soil_humidityList = new ArrayList<>();
    }

    public void publish(final String topic, final String content) {
        try {
            MqttMessage message = new MqttMessage(content.getBytes());
            this.mqttClient.publish(topic, message);
        } catch (MqttException me) {
            me.printStackTrace();
        }
    }

    // send the irrigation this json {"humidity": value}
    public void sendIrrigation(int nodeId, int humidity, int increment) {
        JSONObject irrigationMessage = new JSONObject();
        int id = -1;
        
        if (nodeId >= 0 && nodeId < soil_humidityList.size())
            id = soil_humidityList.get(nodeId);

        irrigationMessage.put("nodeId", id);
        irrigationMessage.put("humidity", humidity);
        irrigationMessage.put("increment", increment);
        this.publish(this.irrigation, irrigationMessage.toJSONString());
    }

    // send the temperature condition this json {"temperature": value}
    public void sendTemperatureHumidityCondition(int temperature, int tempIncrement, int humidity,
            int humIncrement) {
        JSONObject temperatureHumidityConditionMessage = new JSONObject();
        temperatureHumidityConditionMessage.put("temperature", temperature);
        temperatureHumidityConditionMessage.put("temp_increment", tempIncrement);
        temperatureHumidityConditionMessage.put("humidity", humidity);
        temperatureHumidityConditionMessage.put("hum_increment", humIncrement);
        this.publish(this.temperature_humidity_condition, temperatureHumidityConditionMessage.toJSONString());
    }

    public void topicSubscribe() {
        connOpts.setCleanSession(true);
        connOpts.setConnectionTimeout(60);
        connOpts.setKeepAliveInterval(30);
        connOpts.setAutomaticReconnect(true);
        int timeWindow = 3000;

        do {
            if (timeWindow / 128 == timeWindow) {
                System.out.println("Connection lost with the MQTT broker.");
                System.exit(1);
            }
            try {
                this.mqttClient.connect(connOpts);

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
                        && sensorMessage.containsKey("humidity")) {
                    Integer numericTemperatureValue = Integer.parseInt(sensorMessage.get("temperature").toString());
                    Integer numericUmidityValue = Integer.parseInt(sensorMessage.get("humidity").toString());
                    String nodeId = sensorMessage.get("nodeId").toString();
                    Integer nodeIdvalue = Integer.parseInt(nodeId);
                    if (!temperature_humidityList.contains(nodeIdvalue)) {
                        temperature_humidityList.add(nodeIdvalue);
                    }

                    Logger.log(String.format(
                            "[MQTT Java Client]: Received temperature_humidity value from node %s: %d Celsius, %d percentage",
                            nodeId, numericTemperatureValue, numericUmidityValue));

                    MysqlManager.insertTemperatureAndUmidity(nodeId, numericTemperatureValue, numericUmidityValue);

                } else {
                    System.out.println("Garbage data from sensor");
                }
            } else if (topic.equals(this.soilHumidityTopic)) {
                if (sensorMessage.containsKey("nodeId")
                        && sensorMessage.containsKey("soil_humidity")) {
                    Integer numericSoilUmidityValue = Integer.parseInt(sensorMessage.get("soil_humidity").toString());
                    String nodeId = sensorMessage.get("nodeId").toString();
                    Integer nodeIdvalue = Integer.parseInt(nodeId);
                    if (!soil_humidityList.contains(nodeIdvalue)) {
                        soil_humidityList.add(nodeIdvalue);
                    }
                    Logger.log(
                            String.format("[MQTT Java Client]: Received soilHumidity value from node %s: %d percentage",
                                    nodeId, numericSoilUmidityValue));

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

    public ArrayList<Integer> getSoilHumiditylist() {
        return soil_humidityList;
    }

    public void printAllDevices() {
        System.out.println("Soil Humidity sensors:");
        for (int i = 0; i < soil_humidityList.size(); i++) {
            System.out.println("\t" + i + ": " + soil_humidityList.get(i));
        }
        System.out.println("Temperature and Humidity sensors:");
        for (int i = 0; i < temperature_humidityList.size(); i++) {
            System.out.println("\t" + i + ": " + temperature_humidityList.get(i));
        }
    }

}
