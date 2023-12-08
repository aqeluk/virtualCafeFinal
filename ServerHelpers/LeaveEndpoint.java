package ServerHelpers;

public class LeaveEndpoint implements Endpoint{
    @Override
    public String handle(String request, String clientName) {
        return "You left the server";
    }
}
