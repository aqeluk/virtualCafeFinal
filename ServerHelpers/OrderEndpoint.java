package ServerHelpers;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OrderEndpoint implements Endpoint {

    private final Cafe cafe;

    public OrderEndpoint(Cafe cafe) {
        this.cafe = cafe;
    }

    @Override
    public String handle(String command, String clientName) {
        if (command.startsWith("order")) {
            List<Drink.Type> drinks = parseOrder(command);
            if (drinks.isEmpty()) {
                return "The order is empty!";
            }
            String customerName = clientName; // Using clientName as customer name for the order
            boolean orderAccepted = cafe.orderDrinks(drinks, customerName);
            return orderAccepted ? "Order received: " + drinks : "Order not processed due to capacity";
        }
        return "Invalid command";
    }

    private List<Drink.Type> parseOrder(String command) {
        List<Drink.Type> drinks = new ArrayList<>();

        Pattern patternTea = Pattern.compile("(\\d+)\\s*tea");
        Pattern patternCoffee = Pattern.compile("(\\d+)\\s*coffee");

        Matcher matcherTea = patternTea.matcher(command);
        Matcher matcherCoffee = patternCoffee.matcher(command);

        if (matcherTea.find()) {
            int countTea = Integer.parseInt(matcherTea.group(1));
            for (int i = 0; i < countTea; i++) {
                drinks.add(Drink.Type.TEA);
            }
        }

        if (matcherCoffee.find()) {
            int countCoffee = Integer.parseInt(matcherCoffee.group(1));
            for (int i = 0; i < countCoffee; i++) {
                drinks.add(Drink.Type.COFFEE);
            }            
        }

        return drinks;
    }
}
