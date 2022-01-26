package cz.jahodyspeprem.pivovar;

public class Pub extends Node {

    public Pub(String name) {
        super(name);
    }

    public Order generateOrder() {
        return new Order(1 + Utils.randomNumber(6), 1 + Utils.randomNumber(6 * 60), this);
    }

}
