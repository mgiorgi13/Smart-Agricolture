package iot.unipi.it.MQTT;

import org.eclipse.paho.client.mqttv3.*;

import org.json.simple.*;
import java.util.ArrayList;
import iot.unipi.it.Database.MysqlManager;
import iot.unipi.it.Logger.Logger;

public class MQTThandler implements MqttCallback {
    private String temperature_humidityTopic = "temperature_humidity";
    private String soilHumidityTopic = "soilHumidity";
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
                    Integer numericTemperatureValue = Integer.parseInt(sensorMessage.get("temperature").toString());
                    Integer numericUmidityValue = Integer.parseInt(sensorMessage.get("umidity").toString());
                    String nodeId = sensorMessage.get("nodeId").toString();
                    Integer nodeIdvalue = Integer.parseInt(nodeId);
                    if (!temperature_humidityList.contains(nodeIdvalue))
                    {
                        temperature_humidityList.add(nodeIdvalue);
                    }
                            
                    Logger.log(String.format(
                            "[MQTT Java Client]: Received temperature_humidity value from node %s: %d %s, %d %s",
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
                    Integer numericSoilUmidityValue = Integer.parseInt(sensorMessage.get("soil_umidity").toString());
                    String nodeId = sensorMessage.get("nodeId").toString();
                    Integer nodeIdvalue = Integer.parseInt(nodeId);
                    if (!soil_humidityList.contains(nodeIdvalue))
                    {
                        soil_humidityList.add(nodeIdvalue);
                    }
                    Logger.log(
                            String.format("[MQTT Java Client]: Received soilHumidity value from node %s: %d %s",
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
