package iot.unipi.it.Coap;

import org.eclipse.californium.core.CoapServer;

import java.net.SocketException;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.server.resources.CoapExchange;
import java.nio.charset.StandardCharsets;

public class CoapRegistrationServer extends CoapServer {
    private final static CoapNetworkHandler CNH = CoapNetworkHandler.getInstance();

    public CoapRegistrationServer() throws SocketException {
        this.add(new CoapRegistrationResource());
    }

    class CoapRegistrationResource extends CoapResource {
        public CoapRegistrationResource() {
            super("registration");
        }

        @Override
        public void handlePOST(CoapExchange exchange) {
            String deviceType = exchange.getRequestText();
            String ip = exchange.getSourceAddress().getHostAddress();
            boolean success = true;

            switch (deviceType) {
                case "conditioner_actuator":
                    CNH.registerConditioner(ip);
                    break;
                case "window_switch":
                    CNH.registerWindow(ip);
                    break;
                case "irrigation_switch":
                    CNH.registerIrrigation(ip);
                    break;
                default:
                    success = false;
                    break;
            }

            if (success)
                exchange.respond(CoAP.ResponseCode.CREATED, "Success".getBytes(StandardCharsets.UTF_8));
            else
                exchange.respond(CoAP.ResponseCode.NOT_ACCEPTABLE, "Unsuccessful".getBytes(StandardCharsets.UTF_8));
        }

        @Override
        public void handleDELETE(CoapExchange exchange) {
            String[] request = exchange.getRequestText().split("-");
            String ip = request[0];
            String deviceType = request[1];
            boolean success = true;

            switch (deviceType) {
                case "conditioner_actuator":
                    CNH.unregisterConditioner(ip);
                    break;
                case "window_switch":
                    CNH.unregisterWindow(ip);
                    break;
                case "irrigation_switch":
                    CNH.unregisterIrrigation(ip);
                    break;
                default:
                    success = false;
                    break;
            }

            if (success)
                exchange.respond(CoAP.ResponseCode.DELETED, "Cancellation Completed!".getBytes(StandardCharsets.UTF_8));
            else
                exchange.respond(CoAP.ResponseCode.BAD_REQUEST,
                        "Cancellation not allowed!".getBytes(StandardCharsets.UTF_8));
        }
    }
}