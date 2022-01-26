package cz.jahodyspeprem.pivovar;

import java.util.HashMap;
import java.util.Map;

public abstract class Node {

    private Map<Node, Integer> neighbors = new HashMap<>();
    private int distance = Integer.MAX_VALUE;

    private String name;

    public Node(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void addNode(Node n, int distance) {
        neighbors.put(n, distance);
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public Map<Node, Integer> getNeighbors() {
        return neighbors;
    }
}
