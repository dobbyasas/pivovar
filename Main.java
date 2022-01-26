package cz.jahodyspeprem.pivovar;

import javax.sound.midi.Soundbank;
import java.util.*;

public class Main {

    private static Scanner s = new Scanner(System.in);

    private static List<Pub> pubs = new ArrayList<>();

    public static void main(String[] args) {
        DistributionCenter center = new DistributionCenter(80, 40);

        /*List<Pub> pubs = generatePubs(center);

        List<Order> orders = new ArrayList<>();
        for (Pub h : pubs) {
            orders.add(h.generateOrder());
        }*/

        // hospoda 1
        /*Pub p = new Pub("A");
        p.addNode(center, 9);
        center.addNode(p, 9);
        pubs.add(p);
        //hospoda 2
        p = new Pub("B");
        p.addNode(center, 7);
        center.addNode(p, 7);
        pubs.add(p);
        //hospoda 3
        p = new Pub("C");
        p.addNode(center, 8);
        center.addNode(p, 8);
        pubs.add(p);
        // join 2 and 3
        Pub p2 = pubs.get(1);
        Pub p1 = pubs.get(2);
        p2.addNode(p1, 6);
        p1.addNode(p2, 6);*/

        pubs.addAll(generatePubs(center));

        //orders
        while (true) {
            List<Order> orders = new ArrayList<>();
            orders.addAll(generateOrders());

            System.out.println("Počet objednávek: " + orders.size());

            // seřadí objednávky dle času
            Collections.sort(orders, new Comparator<Order>() {
                @Override
                public int compare(Order o1, Order o2) {
                    return o1.getOrderTime() - o2.getOrderTime();
                }
            });

            // TODO: taky kontroluj jestli interni čas trucku neni větší než čas objednávky jinak bude doručovat objednávky z jeho minulosti což je blbosti
            // reset time of all empty trucks
            center.getTrucks().forEach(t -> {
                t.setTime(6 * 60);
            });

            for (Truck truck : center.getTrucks()) {
                if (!truck.getOrdersToDeliver().isEmpty()) {
                    //printTruckOrders(truck);
                    sendTruckOnWay(truck, center);
                }
            }

            center.morningResupply();

            //puting orders to trucks
            for (Order order : orders) {
                Truck truckFound = findTruck(center, order);
                if (truckFound == null) {
                    System.out.println("Order: " + order + " has no available truck");
                    System.exit(-1);
                }
                if (center.canTakeBarrels(order.getBarrels())) {
                    //System.out.println("Processing order: " + order);
                } else {
                    int time = center.processOrdersBeforeTime(order.getOrderTime());
                    truckFound.addToTime(time);
                }
                center.takeBarrels(order.getBarrels(), order.getOrderTime());

            }

            //for each truck, if orders to deliver is not empty and the truck is still travelling then
            // send the trucks on their way since there are no new orders comming in
            for (Truck t : center.getTrucks()) {
                if (t.isDelivering() && !t.getOrdersToDeliver().isEmpty()) {
                    //printTruckOrders(t);
                    sendTruckOnWay(t, center);
                }
            }

            //now check all trucks that did not possibly make it
            /*for (Truck t : center.getTrucks()) {
                if (!t.isDelivering() && !t.getOrdersToDeliver().isEmpty()) {
                    System.out.println("Truck did not deliver all goods, it will try next day");
                }
            }*/
            long nestihlo = center.getTrucks().stream().filter(t ->{
                return !t.getOrdersToDeliver().isEmpty();
            }).count();

            System.out.println("Dnes nestihlo doručit: " + nestihlo + " náklaďáků");

            System.out.println("Chcete pokračovat v simulaci?");
            String in = s.nextLine();
            if (in.startsWith("n")) {
                break;
            }
        }

    }

    private static List<Order> generateOrders() {
        List<Order> orders = new ArrayList<>();
        /*orders.add(new Order(3, 11 * 60 + 30, pubs.get(0)));
        orders.add(new Order(2, 11 * 60 + 40, pubs.get(1)));
        orders.add(new Order(5, 12 * 60 + 50, pubs.get(2)));*/

        for (Pub p : pubs) {
            if (Utils.oneIn(4)) {
                orders.add(new Order(1 + Utils.randomNumber(6), 10 * 60 + Utils.randomNumber(6 * 60), p));
            }
        }

        return orders;
    }

    private static Truck findTruck(DistributionCenter center, Order order) {
        Iterator<Truck> trucks = center.getTrucks().iterator();
        while (trucks.hasNext()) {
            Truck t = trucks.next();
            // if the truck has exhaused its time for traverling skip it
            if (!t.isDelivering()) {
                //System.out.println("Truck is not delivering");
                continue;
            }
            if (t.isOnWay()) {
                continue;
            }
            // if the truck cannot fit the order, dont wait for new order to come in and just send the truck partially empty
            if (!t.canFit(order)) {
                //System.out.println("Truck cannot fit anymore, sending it");
                //printTruckOrders(t);
                sendTruckOnWay(t, center);
                continue;
            }
            //if the truck can fit the order and the order happend sooner then then the time truck has traveled then put the order to the truck
            if (t.canFit(order) && t.isOrderSooner(order)) {
                t.putOrder(order);
                return t;
            }
        }

        return null;
    }

    private static void printTruckOrders(Truck t) {
        List<Order> ordersFromTruck = t.getOrdersToDeliver();
        if (ordersFromTruck.size() == 0) {
            System.out.println("Truck is empty");
            return;
        }
        System.out.println("Truck  will deliver orders:");
        for (Order o : ordersFromTruck) {
            System.out.print(o + ", ");
        }
        System.out.println();
    }

    private static void sendTruckOnWay(Truck t, DistributionCenter c) {
        //System.out.println("Truck paths");
        t.setOnWay(true);
        Order lastOrder = t.getOrdersToDeliver().get(t.getOrdersToDeliver().size() - 1);
        t.setTime(lastOrder.getOrderTime());

        Iterator<Order> orderIterator = t.getOrdersToDeliver().iterator();
        int distanceSum = 0;
        while (orderIterator.hasNext()) {
            // we are done for today
            if (!t.isDelivering()) {
                break;
            }

            Node finalNode = orderIterator.next().getPub();
            //System.out.println("Path for order to: " + finalNode.getName() + " from all nodes");
            List<Node> travelNodes = calculateShortestPath(finalNode);
            /*travelNodes.forEach(n -> {
                System.out.println(n.getName() + ", vzdalenost: " + n.getDistance());
            });*/

            Node ourNode = getDistanceFromListOfDistances(travelNodes, t.getCurrentNode());
            if (ourNode == null) {
                System.out.println("není cesta z " + t.getCurrentNode().getName() + " do " + finalNode.getName());
                finalNode.getNeighbors().entrySet().forEach(e ->{
                    System.out.println(e.getKey().getName());
                });
            }
            int distance = ourNode.getDistance();
            int time = t.calculateTravelTimeFromDistance(distance);
            //System.out.println("Truck traveled: " + distance + " it took: " + time);
            t.addToTime(time);

            //System.out.println("Objednávka do : " + finalNode.getName() + " od: " + t.getCurrentNode().getName() + " doručena v: " + Utils.longToTime(t.getTime()));
            System.out.println("Objednávka do : " + finalNode.getName() + " doručena v: " + Utils.longToTime(t.getTime()));

            //reset paths
            t.setCurrentNode(finalNode);
            pubs.forEach(p -> p.setDistance(Integer.MAX_VALUE));
            c.setDistance(Integer.MAX_VALUE);
            orderIterator.remove();
        }
        if (t.getOrdersToDeliver().isEmpty()) {
            t.setOnWay(false);
            t.setCurrentNode(c);
        }
    }

    private static Node getDistanceFromListOfDistances(List<Node> travelNodes, Node startNode) {
        for (Node travelNode : travelNodes) {
            if (travelNode.equals(startNode)) {
                return travelNode;
            }
        }
        return null;
    }

    public static List<Node> calculateShortestPath(Node endNode) {
        LinkedList<Node> path = new LinkedList<>();
        endNode.setDistance(0);

        Set<Node> visitedNodes = new HashSet<>();
        Set<Node> unvisitedNodes = new HashSet<>();

        unvisitedNodes.add(endNode);
        while (!unvisitedNodes.isEmpty()) {
            Node currentNode = getNextNode(unvisitedNodes);
            unvisitedNodes.remove(currentNode);
            for (Map.Entry<Node, Integer> entry : currentNode.getNeighbors().entrySet()) {
                Node adjNode = entry.getKey();
                int distance = entry.getValue();

                if (!visitedNodes.contains(adjNode)) {
                    Node nextPath = calculateNextNodeInPath(adjNode, distance, currentNode);

                    if (nextPath != null) {
                        path.add(nextPath);
                    }
                    unvisitedNodes.add(adjNode);
                }
             }

            visitedNodes.add(currentNode);
        }

        return path;
    }

    private static Node calculateNextNodeInPath(Node adjNode, int distance, Node srcNode) {
        int srcDistance = srcNode.getDistance();
        if (srcDistance + distance < adjNode.getDistance()) {
            adjNode.setDistance(srcDistance + distance);
            return adjNode;
        }
        return null;
    }

    private static Node getNextNode(Set<Node> unvisitedNodes) {
        Node bestNode = null;
        int distance = Integer.MAX_VALUE;
        for (Node n : unvisitedNodes) {
            if (n.getDistance() < distance) {
                distance = n.getDistance();
                bestNode = n;
            }
        }

        return bestNode;
    }
    public static List<Pub> generatePubs(DistributionCenter center) {
        List<Pub> pubs = new ArrayList<>();
        for (int i = 0; i < 500; i++) {
            Pub p = new Pub("Hospoda č." + i);
            if (!pubs.isEmpty()) {
                if (Utils.oneIn(4)) {
                    Pub randomPub = pubs.get(Utils.randomNumber(pubs.size()));
                    int distance = 1 + Utils.randomNumber(20
                    );
                    p.addNode(randomPub, distance);
                    randomPub.addNode(p, distance);
                }
            }
            int distanceToDistribution = 1 + Utils.randomNumber(40);
            center.addNode(p, distanceToDistribution);
            p.addNode(center, distanceToDistribution);
            pubs.add(p);
        }

        return pubs;
    }
}