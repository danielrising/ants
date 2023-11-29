package org.evensen.ants;

import javafx.geometry.Pos;

import java.util.Arrays;

public class MyAntWorld implements AntWorld {

    private static final float HOME_RADIUS = 20.0F;
    private final int worldWidth;

    private final int worldHeight;

    private final int numberOfFoodSources;

    private float[][] foragingPheromone;
    private float[][] foodPheromone;

    private FoodSource[] foodSources;

    private boolean[][] containsFood;

    public MyAntWorld(final int worldWidth, final int worldHeight, final int i) {
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
        this.numberOfFoodSources = i;
        this.foragingPheromone = new float[this.worldWidth][this.worldHeight];
        this.foodPheromone = new float[this.worldWidth][this.worldHeight];
        this.foodSources = new FoodSource[this.numberOfFoodSources];
        Arrays.fill(this.foodSources, createFoodSource());
        this.containsFood = new boolean[this.worldWidth][this.worldHeight];
        updateContainsFoodMatrix();
    }

    @Override
    public int getWidth() {
        return this.worldWidth;
    }

    @Override
    public int getHeight() {
        return this.worldHeight;
    }

    @Override
    public boolean isObstacle(final Position p) {
        final boolean outsideLeft = p.getX() < 0;
        final boolean outsideRight = p.getX() >= this.worldWidth;
        final boolean outsideUp = p.getY() < 0;
        final boolean outsideDown = p.getY() >= this.worldHeight;
        // Is this inefficient? (Doesn't utilize lazy evaluation) Perhaps. But I think this makes the code easier to
        // read
        // Average execution time  ~1.5E-5
        return (outsideLeft || outsideRight || outsideUp || outsideDown);
        // Average execution time ~1.5E-5 => No difference
        // return (p.getX() < 0 || p.getX() >= this.worldWidth || p.getY() < 0 || p.getY() >= this.worldHeight);
    }

    @Override
    public void dropForagingPheromone(final Position p, final float amount) {
        final int x = (int) p.getX();
        final int y = (int) p.getY();
        this.foragingPheromone[x][y] = amount;
    }

    @Override
    public void dropFoodPheromone(final Position p, final float amount) {
        final int x = (int) p.getX();
        final int y = (int) p.getY();
        this.foodPheromone[x][y] = amount;
    }

    @Override
    public void dropFood(final Position p) {

    }

    @Override
    public void pickUpFood(final Position p) {
        // @TODO
        for (int i = 0; i < this.numberOfFoodSources; i++) {
            // Assumes at this point that foodSources contains food
            if (this.foodSources[i].containsFood(p)) {
                this.foodSources[i].pickupFood();
                if (this.foodSources[i].getFood() < 1) {
                    this.foodSources[i] = createFoodSource();
                }
                updateContainsFoodMatrix();
                return;
            }
        }
    }

    @Override
    public float getDeadAntCount(final Position p) {
        return 0;
    }

    @Override
    public float getForagingStrength(final Position p) {
        final int x = (int) p.getX();
        final int y = (int) p.getY();
        return this.foragingPheromone[x][y];
    }

    @Override
    public float getFoodStrength(final Position p) {
        final int x = (int) p.getX();
        final int y = (int) p.getY();
        return this.foodPheromone[x][y];
    }

    // Abstraction layer containsFood() that checks the containsFood-matrix
    @Override
    public boolean containsFood(final Position p) {
        int x = (int) p.getX();
        int y = (int) p.getY();
        return this.containsFood[x][y];
    }

    // Actual containsFood() that is used to update matrix
    private boolean containsFood(final int x, final int y) {
        final Position p = new Position(x, y);
        for (int i = 0; i < this.numberOfFoodSources; i++) {
            if (this.foodSources[i].containsFood(p)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public long getFoodCount() {
        return 0L;
    }

    @Override
    public boolean isHome(final Position p) {
        final float centerY = this.worldHeight / 2.0F;
        final Position center = new Position(this.worldWidth, centerY);
        return center.isWithinRadius(p, HOME_RADIUS);
    }

    @Override
    public void dispersePheromones() {

    }

    @Override
    public void setObstacle(final Position position, final boolean value) {

    }

    @Override
    public void hitObstacle(final Position position, final float strength) {

    }

    private FoodSource createFoodSource() {
        return new FoodSource(this.worldWidth, this.worldHeight, 5, 10, 500, 1000);
    }

    private void updateContainsFoodMatrix() {
        for (int x = 0; x < this.worldWidth; x++) {
            for (int y = 0; y < this.worldHeight; y++) {
                this.containsFood[x][y] = containsFood(x, y);
            }
        }
    }
}
