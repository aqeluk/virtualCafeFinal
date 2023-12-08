package ServerHelpers;

public interface OrderReadyCallback {
    void onOrderReady(String customerName, Order order);
}
