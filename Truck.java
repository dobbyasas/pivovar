package cz.jahodyspeprem.pivovar;

import java.util.ArrayList;
import java.util.List;

public class Truck {

    public static final int MAX_LOAD = 50;
    public static final double SPEED = 80.0; // v km/h

    // každej truck bude mít inerní časomíru dle které ví jesti bude moct jet nebo ne
    // časomíra se přčítá časem, který trval na ujetí z bodu a do bodu b
    // pokud by čas + čas nakladu byl větší než 21:00 tak se truck vypne
    // a nebude fungovat dokud neni časomira nastavena na začatečni hodnu dalšího dne ( 6:00 )

    //represented in minutes of day
    private long time;

    private List<Order> ordersToDeliver;

    private Node currentNode = null;

    private boolean isOnWay = false;

    public Truck() {
        this.ordersToDeliver = new ArrayList<>();
        time = 6 * 60;
    }

    public void putOrder(Order obj) {
        this.ordersToDeliver.add(obj);
    }

    public boolean canFit(Order ob) {
        return ordersToDeliver.stream().mapToInt(o -> o.getBarrels()).sum() + ob.getBarrels() <= MAX_LOAD;
    }

    public List<Order> getOrdersToDeliver() {
        return ordersToDeliver;
    }

    public boolean isOrderSooner(Order order) {
        return order.getOrderTime() > time;
    }

    public static int calculateTravelTimeFromDistance(long distance) {
        return (int) Math.floor(distance / SPEED * 60);
    }

    public boolean willDeliver(long travelTime) {
        if (this.time + travelTime > (21 * 60)) {
            return false;
        }
        return true;
    }

    public void addToTime(long ntime) {
       this.time += ntime;
    }

    public boolean isDelivering() {
        return this.time <= 21 * 60;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getTime() {
        return time;
    }

    public Node getCurrentNode() {
        return currentNode;
    }

    public void setCurrentNode(Node currentNode) {
        this.currentNode = currentNode;
    }

    public void setOnWay(boolean b) {
        this.isOnWay = b;
    }

    public boolean isOnWay() {
        return isOnWay;
    }
}
