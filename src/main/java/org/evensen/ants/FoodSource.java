package org.evensen.ants;

import javafx.geometry.Pos;

import java.util.Random;
import java.security.SecureRandom;

public class FoodSource {
    private Position position;

    private int radius;

    private static SecureRandom rand = new SecureRandom();

    private int amountOfFood;

    public FoodSource(Position position, int radius, int amountOfFood) {
        this.position = position;
        this.radius = radius;
        this.amountOfFood = amountOfFood;
    }

    FoodSource(int worldWidth, int worldHeigth, int radiusOrigin, int radiusBound, int amountOfFoodOrigin, int amountOfFoodBound) {
        // I know this is a mess but Java won't let me declare variables before calling this()
        this(new Position(rand.nextInt(0, worldWidth - 1), rand.nextInt(0, worldHeigth - 1)), rand.nextInt(radiusOrigin, radiusBound), rand.nextInt(amountOfFoodOrigin, amountOfFoodBound));
    }

    public boolean containsFood(Position p) {
        return this.position.isWithinRadius(p, this.radius);
    }

    public void pickupFood() {
        this.amountOfFood--;
    }

    public int getFood() {
        return this.amountOfFood;
    }
}
