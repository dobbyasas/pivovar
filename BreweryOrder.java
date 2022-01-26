package cz.jahodyspeprem.pivovar;

public class BreweryOrder {

    private int amount;
    private int deliveryTime;

    public BreweryOrder(int amount, int deliveryTime) {
        this.amount = amount;
        this.deliveryTime = deliveryTime;
    }

    public int getDeliveryTime() {
        return deliveryTime;
    }

    public int getAmount() {
        return amount;
    }
}
