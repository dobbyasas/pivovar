package cz.jahodyspeprem.pivovar;

public class Order {

    private int barrels;
    private int orderTime;
    private Pub pub;

    public Order(int barrels, int orderTime, Pub pub) {
        this.barrels = barrels;
        this.orderTime = orderTime;
        this.pub = pub;
    }

    public Pub getPub() {
        return pub;
    }

    public int getOrderTime() {
        return orderTime;
    }

    public int getBarrels() {
        return barrels;
    }

    @Override
    public String toString() {
        return "Order{" +
                "sudy=" + barrels +
                ", minuty=" + orderTime +
                '}';
    }
}
