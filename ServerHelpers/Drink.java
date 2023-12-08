package ServerHelpers;

import java.util.concurrent.atomic.AtomicInteger;

public class Drink {

    private static final AtomicInteger uniqueId = new AtomicInteger(0);

    private String ownerName;
    private String drinkID;

    public enum Type {
        COFFEE, TEA
    }

    public enum Status {
        WAITING, BREWING, READY
    }

    private Type type;
    private Status status;

    public Drink(Type type, String ownerName) {
        this.type = type;
        this.status = Status.WAITING;
        this.ownerName = ownerName;
        this.drinkID = Integer.toString(uniqueId.incrementAndGet());
    }

    public Type getType() {
        return type;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public String getDrinkID() {
        return drinkID;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    @Override
    public String toString() {
        return "Drink{" +
                "type=" + type +
                ", status=" + status +
                '}';
    }
}
