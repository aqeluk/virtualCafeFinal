package ServerHelpers;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class Cafe {
    private static final int BREW_LIMIT_TOTAL = 4;
    private static final int BREW_LIMIT_TYPE = 2;

    private Queue<Order> waitingArea = new LinkedList<>();
    private Queue<Order> brewingArea = new LinkedList<>();
    private Queue<Order> trayArea = new LinkedList<>();
    private ConcurrentHashMap<String, Order> orderByCustomer = new ConcurrentHashMap<>();

    private Queue<Drink> waitingTeas = new LinkedList<>();
    private Queue<Drink> brewingTeas = new LinkedList<>();
    private Queue<Drink> waitingCoffees = new LinkedList<>();
    private Queue<Drink> brewingCoffees = new LinkedList<>();

    public synchronized boolean orderDrinks(List<Drink.Type> types, String customerName) {
        List<Drink> drinks = types.stream()
                .map(type -> new Drink(type, customerName))
                .collect(Collectors.toList());

        for (Drink drink : drinks) {
            if (drink.getType() == Drink.Type.TEA) {
                waitingTeas.add(drink);
            } else {
                waitingCoffees.add(drink);
            }
        }

        Order existingOrder = orderByCustomer.get(customerName);
        if (existingOrder != null) {
            existingOrder.getDrinks().addAll(drinks);
            System.out.println("Added drinks to existing order: " + existingOrder.toString());
        } else {
            Order order = new Order(customerName, drinks);
            System.out.println("Order placed in waiting area: " + order.toString());
            orderByCustomer.put(customerName, order);
        }
        processDrinks();
        return true;
    }

    public synchronized void processDrinks() {
        processSpecificDrink(Drink.Type.TEA, waitingTeas, brewingTeas);
        processSpecificDrink(Drink.Type.COFFEE, waitingCoffees, brewingCoffees);
    }

    private void processSpecificDrink(Drink.Type type, Queue<Drink> waiting, Queue<Drink> brewing) {
        while (!waiting.isEmpty() && canBrewDrink(type)) {
            Drink drink = waiting.poll();
            System.out.println("Drink moved from waiting area to brewing area: " + drink);
            brewing.add(drink);
            drink.setStatus(Drink.Status.BREWING);

            new Thread(() -> {
                try {
                    Thread.sleep(drink.getType() == Drink.Type.COFFEE ? 4500 : 3000);
                    drink.setStatus(Drink.Status.READY);
                    System.out.println("Drink brewed and ready: " + drink);
                    moveToTrayArea(drink, brewing);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    private boolean canBrewDrink(Drink.Type type) {
        long countBrewing = (type == Drink.Type.COFFEE ? brewingCoffees : brewingTeas).size();
        return countBrewing < BREW_LIMIT_TYPE && (brewingTeas.size() + brewingCoffees.size()) < BREW_LIMIT_TOTAL;
    }

    public synchronized boolean isOrderReady(String customerName) {
        Order order = orderByCustomer.get(customerName);
        if (order != null && order.isReady()) {
            return true;
        }
        return false;
    }

    private OrderReadyCallback orderReadyCallback;

    public void setOrderReadyCallback(OrderReadyCallback callback) {
        this.orderReadyCallback = callback;
    }

    private synchronized void moveToTrayArea(Drink drink, Queue<Drink> brewing) {
        if (brewing.remove(drink)) {
            Order order = orderByCustomer.get(drink.getOwnerName());
            if (order != null && order.isReady()) {
                trayArea.add(order);
                System.out.println("Order moved to tray area: " + order);
                System.out.println("Order for " + order.getCustomerName() + " is ready: " + order);
                orderByCustomer.remove(order.getCustomerName());
                trayArea.remove(order);

                if (orderReadyCallback != null) {
                    orderReadyCallback.onOrderReady(drink.getOwnerName(), order);
                }
            }
        }
        processDrinks();
    }

    public synchronized void customerLeft(String customerName) {
        Order order = orderByCustomer.remove(customerName);
        if (order != null) {
            waitingArea.remove(order);
            brewingArea.remove(order);
            trayArea.remove(order);
            System.out.println("Customer left: " + customerName + ". Reassigning their order.");
            order.getDrinks().forEach(this::reassignDrink);
        }
    }

    private void reassignDrink(Drink drink) {
        for (Order waitingOrder : waitingArea) {
            if (waitingOrder.getDrinks().contains(drink)) {
                orderByCustomer.put(waitingOrder.getCustomerName(), waitingOrder);
                drink.setOwnerName(waitingOrder.getCustomerName());
                System.out.println("Reassigned drink " + drink + " to " + waitingOrder.getCustomerName());
                waitingArea.remove(waitingOrder);
                break;
            }
        }
    }

    public synchronized Order serveCustomer(String customerName) {
        Order order = orderByCustomer.get(customerName);
        if (order != null && order.isReady()) {
            trayArea.remove(order);
            System.out.println("Served order to customer: " + customerName + " - " + order);
            return order;
        }
        System.out.println("Order not ready for customer: " + customerName);
        return null;
    }

    public synchronized String getOrderStatus(String customerName) {
        Order order = orderByCustomer.get(customerName);
        if (order == null) {
            return "No order found for customer: " + customerName;
        }

        StringBuilder status = new StringBuilder();
        status.append("Order Details:\n");
        status.append(order.toString()).append("\n");

        // Location of the order
        if (waitingArea.contains(order)) {
            status.append("Location: Waiting Area\n");
        } else if (brewingArea.contains(order)) {
            status.append("Location: Brewing Area\n");
        } else if (trayArea.contains(order)) {
            status.append("Location: Tray Area (Ready for Pickup)\n");
        }

        // Status of each drink in the order
        for (Drink drink : order.getDrinks()) {
            status.append(drink.toString()).append("\n");
        }

        return status.toString();
    }
}
