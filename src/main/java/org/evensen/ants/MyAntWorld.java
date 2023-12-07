package org.evensen.ants;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.lang.Math;

public class MyAntWorld implements AntWorld {

    // Constants
    private static final int FOOD_SOURCE_RADIUS = 10;
    private static final int FOOD_SOURCE_START_AMOUNT = 5000;

    // Generators
    private static final Random rand = new Random(5L);

    // Instance variables
    private final int width, height;
    private final float[][] foodPheromone;
    private final float[][] foragingPheromone;
    private final int[][] containsFood;
    private final List<FoodSource> foodSources;

    // Constructor, width of world, height of world, number of food sources
    public MyAntWorld(final int w, final int h, final int foodSources) {
        // Initialize instance variables and set capacity
        this.width = w;
        this.height = h;
        this.foodPheromone = new float[w + 1][h + 1];
        this.foragingPheromone = new float[w + 1][h + 1];
        this.foodSources = new ArrayList<>(foodSources);
        this.containsFood = new int[w + 1][h + 1];

        // Initialize food sources
        for (int i = 0; i < foodSources; i++) {
            final FoodSource source = newFoodSource();
            this.foodSources.add(source);
        }

        // Initialize contains food matrix
        updateContainsFoodMatrix();
    }

    // Used for optimizing, through avoiding unnecessary multiple similar calculations
    // Contains the index of the food source that contains the associated food (-1 if there is no food)
    private void updateContainsFoodMatrix() {
        for (int x = 0; x < this.width + 1; x++) {
            for (int y = 0; y < this.height + 1; y++) {

                // Set every element to not contain food as default
                this.containsFood[x][y] = -1;

                // For each food (But with index as that is needed)
                for (int i = 0; i < this.foodSources.size(); i++) {
                    FoodSource source = this.foodSources.get(i);

                    // Food source variables
                    final Position sourceP = source.getPosition();
                    final float sourceR = (float) source.getRadius();
                    final boolean containsFood = source.containsFood();

                    // Current matrix position
                    Position p = new Position(x, y);

                    // Does current cell contain food
                    if (p.isWithinRadius(sourceP, sourceR) && containsFood){
                        this.containsFood[x][y] = i;
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
        return !p.isInBounds(this.width, this.height);
    }

    // Rounds position to the closest integer cell
    @Override
    public void dropForagingPheromone(final Position p, final float amount) {
        final float x = p.getX();
        final float y = p.getY();
        this.foragingPheromone[Math.round(x)][Math.round(y)] += amount;
    }

    // Rounds position to the closest integer cell
    @Override
    public void dropFoodPheromone(final Position p, final float amount) {
        final float x = p.getX();
        final float y = p.getY();
        this.foodPheromone[Math.round(x)][Math.round(y)] += amount;
    }

    @Override
    public void dropFood(final Position p) {

    }

    @Override
    public void pickUpFood(final Position p) {
        final float x = p.getX();
        final float y = p.getY();

        // Index of first (close-enough) food source
        final int i = this.containsFood[Math.round(x)][Math.round(y)];

        // There is food to pickup
        if (i != -1) {
            FoodSource source = this.foodSources.get(i);
            source.pickupFood();

            // If it was the last piece of food, create new source and update matrix
            if (!source.containsFood()) {
                this.foodSources.set(i, newFoodSource());
                updateContainsFoodMatrix();
            }
        }
    }

    // Creates a new (randomly positioned) food reserve at index i (with default amount and radius)
    private FoodSource newFoodSource() {
        // Random position for new food source
        final float x = rand.nextFloat(0.0f, (float) (this.width - 1));
        final float y = rand.nextFloat(0.0f, (float) (this.height - 1));
        final Position p = new Position(x, y);

        // Set new food reserve
        return new FoodSource(FOOD_SOURCE_START_AMOUNT, p, FOOD_SOURCE_RADIUS);
    }

    @Override
    public float getDeadAntCount(final Position p) {
        return 0;
    }

    // Rounds position to the closest integer cell
    @Override
    public float getForagingStrength(final Position p) {
        final float x = p.getX();
        final float y = p.getY();
        return this.foragingPheromone[Math.round(x)][Math.round(y)];
    }

    // Rounds position to the closest integer cell
    @Override
    public float getFoodStrength(final Position p) {
        final float x = p.getX();
        final float y = p.getY();
        return this.foodPheromone[Math.round(x)][Math.round(y)];
    }

    // Simply checks the already calculated matrix
    @Override
    public boolean containsFood(final Position p) {
        final float x = p.getX();
        final float y = p.getY();
        return this.containsFood[Math.round(x)][Math.round(y)] != -1;
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