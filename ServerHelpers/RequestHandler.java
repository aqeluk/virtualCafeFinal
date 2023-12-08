package ServerHelpers;

import java.util.HashMap;
import java.util.Map;

public class RequestHandler {
    private Map<String, Endpoint> endpoints;

    public RequestHandler(Cafe cafe) {
        this.endpoints = new HashMap<>();
        endpoints.put("status", new StatusEndpoint(cafe));
        endpoints.put("order", new OrderEndpoint(cafe));
        endpoints.put("leave", new LeaveEndpoint());
    }

    public String handle(String request, String clientName) {
        for (String key : endpoints.keySet()) {
            if (request.toLowerCase().startsWith(key)) { 
                return endpoints.get(key).handle(request, clientName);
            }
        }
        return "Unknown command"; // default action for unknown commands
    }
}

