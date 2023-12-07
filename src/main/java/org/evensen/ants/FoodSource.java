package org.evensen.ants;

public class FoodSource {

    private final int r;
    private final Position pos;
    private int amount;

    public FoodSource(int foodAmount, Position pos, int r) {
        this.pos = pos;
        this.r = r;
        this.amount = foodAmount;
    }

    public boolean containsFood() {
        return this.amount > 0;
    }

    public void pickupFood() {
        this.amount--;
    }

    public Position getPosition() {
        return this.pos;
    }

    public int getRadius() {
        return this.r;
    }
}
