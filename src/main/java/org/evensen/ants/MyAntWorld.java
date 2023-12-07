package org.evensen.ants;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MyAntWorld implements AntWorld {

    // Constants
    private static final int FOOD_SOURCE_RADIUS = 10;
    private static final int FOOD_SOURCE_START_AMOUNT = 50000;
    private static final float MAX_PHEROMONE_LEVEL = 1.0f;
    private static final float PHEROMONE_DROPOFF = 0.95F;
    private static final float PHEROMONE_NEIGHBOUR_KEEP = 0.5f;
    private static final int[][] ADJACENT_CELL_DELTAS = {{-1,  1}, { 0,  1}, {1,  1},
                                                         {-1,  0},           {1,  0},
                                                         {-1, -1}, { 0, -1}, {1, -1}}; // Pls don't auto format :(

    // Generators
    private static final Random rand = new Random(5L);

    // Instance variables
    private final int width, height;
    private float[][] foodPheromone;
    private float[][] foragingPheromone;
    private final int[][] containsFood;
    private final List<FoodSource> foodSources;

    // Constructor, width of world, height of world, number of food sources
    public MyAntWorld(final int w, final int h, final int foodSources) {
        // Initialize instance variables and set capacity
        this.width = w;
        this.height = h;
        this.foodPheromone = new float[w][h];
        this.foragingPheromone = new float[w][h];
        this.foodSources = new ArrayList<>(foodSources);
        this.containsFood = new int[w][h];

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
        for (int x = 0; x < this.width; x++) {
            for (int y = 0; y < this.height; y++) {

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

    public boolean isObstacle(final float x, final float y) {
        final boolean tooFarLeft = 0.0F > x;
        final boolean tooFarRight = x >= this.width;
        final boolean tooFarDown = 0.0F > y ;
        final boolean tooFarUp = y >= this.height;
        return tooFarLeft || tooFarRight || tooFarDown || tooFarUp;
    }

    @Override
    public void dropForagingPheromone(final Position p, final float amount) {
        dropPheromone(this.foragingPheromone, p, amount);
    }

    @Override
    public void dropFoodPheromone(final Position p, final float amount) {
        dropPheromone(this.foodPheromone, p, amount);
    }

    // Abstract drop pheromone method
    private void dropPheromone(final float[][] pheromoneMatrix, final Position p, final float amount) {
        // Round to nearest cell
        final int x = (int) Math.floor(p.getX());
        final int y = (int) Math.floor(p.getY());

        // Already max value // TODO remove this?
        if (pheromoneMatrix[x][y] == MAX_PHEROMONE_LEVEL) {
            return;
        }

        // Goes above max value, cap it
        if (pheromoneMatrix[x][y] + amount > MAX_PHEROMONE_LEVEL) {
            pheromoneMatrix[x][y] = MAX_PHEROMONE_LEVEL;
        } else { // Add appropriate amount to cell
            pheromoneMatrix[x][y] += amount;
        }
    }

    @Override
    public void dropFood(final Position p) {

    }

    @Override
    public void pickUpFood(final Position p) {
        final float x = p.getX();
        final float y = p.getY();

        // Index of first (close-enough) food source
        final int i = this.containsFood[(int) Math.floor(x)][(int )Math.floor(y)];

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
        return this.foragingPheromone[(int) Math.floor(x)][(int) Math.floor(y)];
    }

    // Rounds position to the closest integer cell
    @Override
    public float getFoodStrength(final Position p) {
        final float x = p.getX();
        final float y = p.getY();
        return this.foodPheromone[(int) Math.floor(x)][(int) Math.floor(y)];
    }

    // Simply checks the already calculated matrix
    @Override
    public boolean containsFood(final Position p) {
        final float x = p.getX();
        final float y = p.getY();
        return this.containsFood[(int) Math.floor(x)][(int) Math.floor(y)] != -1;
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
        dispersePheromone(this.foragingPheromone);
        dispersePheromone(this.foodPheromone);

        dropFoodSourcePheromones();
    }

    private void dispersePheromone(final float[][] pheromone) {
        final float[][] tmpP = new float[this.width][this.height];
        final float numberOfNeighbours = 8.0F;
        for(int x = 0; x < this.width; x++) {
            for(int y = 0; y < this.height; y++) {
                float npl = 0.0F;
                if (!isObstacle(x, y)) {
                    npl = sumAdjacentCells(x, y, pheromone);
                    npl = ((1.0F - PHEROMONE_NEIGHBOUR_KEEP) * npl) / numberOfNeighbours + (PHEROMONE_NEIGHBOUR_KEEP * (pheromone[x][y]));
                }
                tmpP[x][y] = npl * PHEROMONE_DROPOFF;
            }
        }
        if (pheromone == this.foodPheromone) {
            this.foodPheromone = tmpP;
        } else {
            this.foragingPheromone = tmpP;
        }
    }

    private void dropFoodSourcePheromones() {
        for (final FoodSource source : this.foodSources) {
            dropFoodPheromone(source.getPosition(), 1.0F);
        }
    }

    private float sumAdjacentCells(final int x0, final int y0, float[][] matrix) {
        float sum = 0.0F;
        for (int[] deltas : ADJACENT_CELL_DELTAS) {

                // Truncate coordinates to the closest inbound value
                int x = truncate(0, this.width - 1, x0 + deltas[0]);
                int y = truncate(0, this.height - 1, y0 + deltas[1]);

                sum += matrix[x][y];
        }
        return sum;
    }

    private static int truncate(final int min, final int max, int val) {
        if (val < min) {
            return min;
        }
        if (val > max) {
            return max;
        }
        return val;
    }

    @Override
    public void setObstacle(final Position p, final boolean add) {

    }

    @Override
    public void hitObstacle(final Position p, final float strength) {

    }
                 }