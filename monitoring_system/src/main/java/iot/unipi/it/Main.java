package iot.unipi.it;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;

public class Main {
	public static void main(String[] args) {

		CoapClient client = new CoapClient("coap://127.0.0.1/hello");

		CoapResponse response = client.get();	
		
		System.out.print(response.getResponseText());
	}

}
