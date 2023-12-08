package ServerHelpers;

public class StatusEndpoint implements Endpoint {
    private final Cafe cafe;

    public StatusEndpoint(Cafe cafe) {
        this.cafe = cafe;
    }

    @Override
    public String handle(String request, String clientName) {
        return cafe.getOrderStatus(clientName);
    }
}
