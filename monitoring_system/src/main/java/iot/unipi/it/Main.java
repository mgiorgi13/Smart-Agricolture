package iot.unipi.it;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;

public class Main {
    public static void main(String[] args) {

        CoapClient client = new CoapClient("coap://fd00::202:2:2:2/leds?color=r");

        String payload = "mode=on";
        CoapResponse response = client.put(payload, 0);	
        
        System.out.print(response.getResponseText());
    }

}