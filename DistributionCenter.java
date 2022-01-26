package cz.jahodyspeprem.pivovar;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DistributionCenter extends Node {

    private static final int MAX_BARRELS = 600;
    private List<Truck> trucks = new ArrayList<>();
    private List<BreweryOrder> ordersToBrewery = new ArrayList<>();
    private int distanceToBrewery;
    private int timeToDeliverFromBrewery;
    private int barrels = 500;

    public DistributionCenter(int nTrucks, int distanceToBrewery) {
        super("sklad");
        for (int i = 0; i < nTrucks; i++) {
            Truck t = new Truck();
            t.setCurrentNode(this);
            this.trucks.add(t);
        }
        this.timeToDeliverFromBrewery = Truck.calculateTravelTimeFromDistance(distanceToBrewery);
    }

    public List<Truck> getTrucks() {
        return this.trucks;
    }

    public int getDistanceToBrewery() {
        return distanceToBrewery;
    }

    public int getBarrels() {
        return barrels;
    }

    public boolean canTakeBarrels(int c) {
        return this.barrels - c > 0;
    }

    //takes barrels from the center, if the missing amount of barrels isnt max then return true so we can request them
    public void takeBarrels(int c, int orderTime) {
        this.barrels -= c;
        processOrdersBeforeTime(orderTime);
        //System.out.println("Took " + c + " barrels, storage now has: " + this.barrels + " barrels");
        if (this.barrels < MAX_BARRELS - Truck.MAX_LOAD) {
            int totalRequestSize = ordersToBrewery.stream().mapToInt(order -> order.getAmount()).sum();
            // do not request if the total amount to deliver would be too high to store
            if (this.barrels + totalRequestSize + Truck.MAX_LOAD > MAX_BARRELS) {
                return;
            }
            BreweryOrder order = new BreweryOrder(Truck.MAX_LOAD, orderTime + timeToDeliverFromBrewery);
            this.ordersToBrewery.add(order);
            //System.out.println("Order for brewery with size of 50 sent at: " + Utils.longToTime(orderTime) + " it will arrive at: " + Utils.longToTime(orderTime + timeToDeliverFromBrewery));
        }
    }

    public void morningResupply() {
        int missingBarrels = MAX_BARRELS - this.barrels;
        //toto je hnus, jde to udelat nejak normal matematicky
        int cnt = 0;
        while (missingBarrels > 0) {
            cnt++;
            if (missingBarrels >= Truck.MAX_LOAD) {
                BreweryOrder order = new BreweryOrder(Truck.MAX_LOAD, (6 * 60) + timeToDeliverFromBrewery);
                this.ordersToBrewery.add(order);
                missingBarrels -= Truck.MAX_LOAD;
            } else {
                BreweryOrder order = new BreweryOrder(missingBarrels, (6 * 60) + timeToDeliverFromBrewery);
                this.ordersToBrewery.add(order);
                missingBarrels = 0;
            }
        }

        if (cnt != 0) {
            System.out.println("Sent " + cnt + " trucks for resupply, they will arrive at: " + Utils.longToTime(6 * 60 + timeToDeliverFromBrewery));
        }
    }

    public int processOrdersBeforeTime(int orderTime) {
        Iterator<BreweryOrder> orderIter = this.ordersToBrewery.iterator();
        while (orderIter.hasNext()) {
            BreweryOrder order = orderIter.next();
            if (order.getDeliveryTime() < orderTime) {
                this.barrels += order.getAmount();
                orderIter.remove();

                return timeToDeliverFromBrewery;
            }
        }
        return -1;
    }
}
