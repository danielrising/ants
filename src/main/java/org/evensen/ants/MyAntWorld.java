package org.evensen.ants;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MyAntWorld implements AntWorld {

    private static final int FOOD_SOURCE_RADIUS = 10;
    private static final int FOOD_SOURCE_START_AMOUNT = 5000;

    private static final Random rand = new Random(5);

    private final int width, height;
    private float[][] foodPheromone;
    private float[][] foragingPheromone;
    private boolean[][] containsFood;
    private List<FoodSource> foodSources;

    public MyAntWorld(int width, int height, int foodSources) {
        this.width = width;
        this.height = height;
        this.foodPheromone = new float[width + 1][height + 1];
        this.foragingPheromone = new float[width + 1][height + 1];
        this.foodSources = new ArrayList<>();
        this.containsFood = new boolean[width + 1][height + 1];

        for (int i = 0; i < foodSources; i++) {
            Position p = new Position(rand.nextFloat(0, this.width - 1), rand.nextFloat(0, this.height - 1));
            this.foodSources.add(i, new FoodSource(p, FOOD_SOURCE_RADIUS, FOOD_SOURCE_START_AMOUNT));
        }

        /*
        for (int i = 0; i < foodSources; i++) {
            System.out.println(this.foodSources.get(i).getPosition().getX() + ": " + this.foodSources.get(i).getPosition().getY());
            System.out.println(this.foodSources.get(i).getFoodAmount());
            System.out.println(this.foodSources.get(i).getRadius());
        }
        */

        updateContainsFoodMatrix();
    }

    private void updateContainsFoodMatrix() {
        for (int i = 0; i < this.width; i++) {
            for (int j = 0; j < this.height; j++) {
                Position p = new Position(i, j);
                this.containsFood[i][j] = false;
                for (int k = 0; k < this.foodSources.size(); k++) {
                    FoodSource foodSource = this.foodSources.get(k);
                    if (p.isWithinRadius(foodSource.getPosition(), foodSource.getRadius()) && foodSource.containsFood()) {
                        this.containsFood[i][j] = true;
                    }
                }
            }
        }
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @Override
    public boolean isObstacle(final Position p) {
        return (p.getX() < 0f || p.getY() < 0f || p.getX() > this.width || p.getY() > this.height);
    }

    @Override
    public void dropForagingPheromone(final Position p, final float amount) {
        this.foragingPheromone[(int)p.getX()][(int)p.getY()] += amount;
    }

    @Override
    public void dropFoodPheromone(final Position p, final float amount) {
        this.foodPheromone[(int)p.getX()][(int)p.getY()] += amount;
    }

    @Override
    public void dropFood(final Position p) {

    }

    @Override
    public void pickUpFood(final Position p) {
        if (this.containsFood[(int)p.getX()][(int)p.getY()]) {
            for (int i = 0; i < this.foodSources.size(); i++) {
                FoodSource foodSource = this.foodSources.get(i);
                if (p.isWithinRadius(foodSource.getPosition(), foodSource.getRadius()) && foodSource.containsFood()) {
                    foodSource.pickupFood();
                    updateFoodReserves();
                    //System.out.println(i + ": " + foodSource.getFoodAmount());
                    return;
                }
            }
        }
    }

    private void updateFoodReserves() {
        for (int i = 0; i < this.foodSources.size(); i++) {
            if (!(this.foodSources.get(i).containsFood())) {
                Position p = new Position(rand.nextFloat(0, this.width - 1), rand.nextFloat(0, this.height - 1));
                this.foodSources.set(i, new FoodSource(p, FOOD_SOURCE_RADIUS, FOOD_SOURCE_START_AMOUNT));
                updateContainsFoodMatrix();
            }
        }
    }

    @Override
    public float getDeadAntCount(final Position p) {
        return 0;
    }

    @Override
    public float getForagingStrength(final Position p) {
        return this.foragingPheromone[(int)p.getX()][(int)p.getY()];
    }

    @Override
    public float getFoodStrength(final Position p) {
        return this.foodPheromone[(int)p.getX()][(int)p.getY()];
    }

    @Override
    public boolean containsFood(final Position p) {
        if (this.containsFood[(int)p.getX()][(int)p.getY()]) {
            for (int i = 0; i < this.foodSources.size(); i++) {
                FoodSource foodSource = this.foodSources.get(i);
                if (p.isWithinRadius(foodSource.getPosition(), foodSource.getRadius()) && foodSource.containsFood()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public long getFoodCount() {
        return 0;
    }

    @Override
    public boolean isHome(final Position p) {
        return p.isWithinRadius(new Position(this.width, this.height/2), 20);
    }

    @Override
    public void dispersePheromones() {

    }

    @Override
    public void setObstacle(final Position p, final boolean add) {

    }

    @Override
    public void hitObstacle(final Position p, final float strength) {

    }
}