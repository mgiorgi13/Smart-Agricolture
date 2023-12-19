package iot.unipi.it.MQTT;

import org.eclipse.paho.client.mqttv3.*;

import org.json.simple.*;
import java.util.ArrayList;
import iot.unipi.it.Database.MysqlManager;
import iot.unipi.it.Logger.Logger;

public class MQTThandler implements MqttCallback {
    private String temperature_humidityTopic = "temperature_humidity";
    private String soilHumidityTopic = "soilHumidity";
    private String irrigation = "irrigation";
    private String temperature_condition = "temperature_condition";
    private String humidity_condition = "humidity_condition";
    private ArrayList<Integer> temperature_humidityList;
    private ArrayList<Integer> soil_humidityList;

    private String broker = "tcp://127.0.0.1:1883";
    private String clientId = "JavaApp";
    private MqttClient mqttClient = null;

    public MQTThandler() throws MqttException {
        this.mqttClient = new MqttClient(this.broker, this.clientId);
        this.mqttClient.setCallback(this);
        this.topicSubscribe();
        temperature_humidityList = new ArrayList();
        soil_humidityList = new ArrayList();
    }

    public void publish(final String topic, final String content) throws MqttException{
        try {
            MqttMessage message = new MqttMessage(content.getBytes());
            this.mqttClient.publish(topic, message);
        } catch(MqttException me) {
            me.printStackTrace();
        }
    }

    // send the irrigation this json {"humidity": value}
    public void sendIrrigation(int nodeId, int humidity) throws MqttException {
        JSONObject irrigationMessage = new JSONObject();
        irrigationMessage.put("nodeId", nodeId);
        irrigationMessage.put("humidity", humidity);
        this.publish(this.irrigation, irrigationMessage.toJSONString());
    } 

    // send the temperature condition this json {"temperature": value}
    public void sendTemperatureCondition(int temperature) throws MqttException {
        JSONObject temperatureConditionMessage = new JSONObject();
        temperatureConditionMessage.put("temperature", temperature);
        this.publish(this.temperature_condition, temperatureConditionMessage.toJSONString());
    }

    // send the humidity condition this json {"humidity": value}
    public void sendHumidityCondition(int humidity) throws MqttException {
        JSONObject humidityConditionMessage = new JSONObject();
        humidityConditionMessage.put("humidity", humidity);
        this.publish(this.humidity_condition, humidityConditionMessage.toJSONString());
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
                        && sensorMessage.containsKey("humidity")) {
                    Integer numericTemperatureValue = Integer.parseInt(sensorMessage.get("temperature").toString());
                    Integer numericUmidityValue = Integer.parseInt(sensorMessage.get("humidity").toString());
                    String nodeId = sensorMessage.get("nodeId").toString();
                    Integer nodeIdvalue = Integer.parseInt(nodeId);
                    if (!temperature_humidityList.contains(nodeIdvalue))
                    {
                        temperature_humidityList.add(nodeIdvalue);
                    }
                            
                    Logger.log(String.format(
                            "[MQTT Java Client]: Received temperature_humidity value from node %s: %d Celsius, %d percentage",
                            nodeId, numericTemperatureValue,numericUmidityValue));

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
                    if (!soil_humidityList.contains(nodeIdvalue))
                    {
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
    public ArrayList<Integer> getSoilHumiditylist()
    {
        return soil_humidityList;
    }
    public void printAllDevices()
    {
        System.out.println("Soil Humidity sensors:");
        for (int i= 0; i < soil_humidityList.size(); i++)
        {
            System.out.println("\t"+i +": " + soil_humidityList.get(i));
        }
        System.out.println("Temperature and Humidity sensors:");
        for (int i= 0; i < temperature_humidityList.size(); i++)
        {
            System.out.println("\t"+i +": "  + temperature_humidityList.get(i));
        }
    }

    

}
