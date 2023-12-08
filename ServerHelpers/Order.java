package ServerHelpers;

import java.util.ArrayList;
import java.util.List;

public class Order {
    private String customerName;
    private List<Drink> drinks;

    public Order(String customerName, List<Drink> drinks) {
        this.customerName = customerName;
        this.drinks = new ArrayList<>(drinks);
    }

    public String getCustomerName() {
        return customerName;
    }

    public List<Drink> getDrinks() {
        return drinks;
    }

    public boolean isReady() {
        return drinks.stream().allMatch(drink -> drink.getStatus() == Drink.Status.READY);
    }

    @Override
    public String toString() {
        return "Order for: " + customerName + ", drinks=" + drinks + "]";
    }
}
