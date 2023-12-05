package org.evensen.ants;

public class FoodSource {

    private final Position position;
    private final int radius;
    private int foodAmount;

    public FoodSource(final Position position, final int radius, int foodAmount) {
        this.position = position;
        this.radius = radius;
        this.foodAmount = foodAmount;
    }

    public boolean containsFood() {
        if (this.foodAmount > 0) {
            return true;
        }
        return false;
    }

    public void pickupFood() {
        this.foodAmount--;
    }

    public Position getPosition() {
        return this.position;
    }

    public int getRadius() {
        return this.radius;
    }

    public int getFoodAmount() {
        return this.foodAmount;
    }
}
