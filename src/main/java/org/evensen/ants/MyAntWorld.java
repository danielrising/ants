package org.evensen.ants;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * ANT WORLD
 */
public class MyAntWorld implements AntWorld {

    // Constants
    private static final int FOOD_SOURCE_RADIUS = 10;
    private static final int FOOD_SOURCE_START_AMOUNT = 50000;
    private static final float MAX_PHEROMONE_LEVEL = 1.0f;
    private static final float PHEROMONE_DROPOFF = 0.95F;
    private static final float PHEROMONE_NEIGHBOUR_KEEP = 0.5f;
    private static final float NUMBER_OF_NEIGHBOURS = 8.0f;
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
    private final DispersalPolicy dispersalPolicy;

    /**
     * @param w width of world
     * @param h height of world
     * @param foodSources number of food sources to create
     */
    // Constructor, width of world, height of world, number of food sources
    public MyAntWorld(final int w, final int h, final int foodSources, final DispersalPolicy dispersalPolicy) {
        // Initialize instance variables and set capacity
        this.width = w;
        this.height = h;
        this.foodPheromone = new float[w][h];
        this.foragingPheromone = new float[w][h];
        this.foodSources = new ArrayList<>(foodSources);
        this.containsFood = new int[w][h];
        this.dispersalPolicy = dispersalPolicy;

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
    @SuppressWarnings({"MethodWithMultipleLoops", "FeatureEnvy"})
    private void updateContainsFoodMatrix() {
        for (int x = 0; x < this.width; x++) {
            for (int y = 0; y < this.height; y++) {

                // Set every element to not contain food as default
                this.containsFood[x][y] = -1;

                // For each food (But with index as that is needed)
                final int amountOfSources = this.foodSources.size();
                for (int i = 0; i < amountOfSources; i++) {
                    final FoodSource source = this.foodSources.get(i);

                    // Food source variables
                    final Position sourceP = source.getPosition();
                    final float sourceR = (float) source.getRadius();
                    final boolean containsFood = source.containsFood();

                    // Current matrix position
                    final Position p = new Position((float) x, (float) y);

                    // Current source has food there -> Update matrix
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

    @Override
    public void dropForagingPheromone(final Position p, final float amount) {
        dropPheromone(this.foragingPheromone, p, amount);
    }

    @Override
    public void dropFoodPheromone(final Position p, final float amount) {
        dropPheromone(this.foodPheromone, p, amount);
    }

    // Abstract drop pheromone method
    private static void dropPheromone(final float[][] pheromoneMatrix, final Position p, final float amount) {
        // x & y floored indexes
        final int x = p.floorX();
        final int y = p.floorY();

        // Goes above max value, cap it
        if (pheromoneMatrix[x][y] + amount > MAX_PHEROMONE_LEVEL) {
            pheromoneMatrix[x][y] = MAX_PHEROMONE_LEVEL;
        }

        // Add appropriate amount to cell
        else {
            pheromoneMatrix[x][y] += amount;
        }
    }

    @Override
    public void dropFood(final Position p) { }

    @Override
    public void pickUpFood(final Position p) {

        // Index of first (close-enough) food source
        final int i = this.containsFood[p.floorX()][p.floorY()];

        // There is food to pickup
        if (i != -1) {
            final FoodSource source = this.foodSources.get(i);
            source.pickupFood();

            // If it was the last piece of food, create new source and update matrix
            if (!source.containsFood()) {
                final FoodSource newSource = newFoodSource();
                this.foodSources.set(i, newSource);
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
        return 0.0f;
    }

    @Override
    public float getForagingStrength(final Position p) {
        return this.foragingPheromone[p.floorX()][p.floorY()];
    }

    @Override
    public float getFoodStrength(final Position p) {
        return this.foodPheromone[p.floorX()][p.floorY()];
    }

    // Simply checks the already calculated matrix
    @Override
    public boolean containsFood(final Position p) {
        return this.containsFood[p.floorX()][p.floorY()] != -1; // -1 Represents no food present
    }

    @Override
    public long getFoodCount() {
        return 0L;
    }

    @Override
    public boolean isHome(final Position p) {
        return p.isWithinRadius(new Position((float) this.width, (float) this.height / 2.0F), 20.0F);
    }

    public void dispersePheromones() {
        // Temporary matrices for new pheromone levels - Food and Forage respectively
        final float[][] tmpFood = new float[this.width][this.height];
        final float[][] tmpForage = new float[this.width][this.height];

        for(int x = 0; x < this.width; x++) {
            for(int y = 0; y < this.height; y++) {
                // Current cell-position
                final Position p = new Position((float) x, (float) y);

                // New pheromone levels
                final float[] newLevels = this.dispersalPolicy.getDispersedValue(this, p);

                // Update temp matrices
                tmpFood[x][y] = newLevels[0];
                tmpForage[x][y] = newLevels[1];
            }
        }

        // Update matrices with new pheromone levels
        this.foodPheromone = tmpFood;
        this.foragingPheromone = tmpForage;
        dropFoodSourcePheromones();
    }

    private void dropFoodSourcePheromones() {
        for (final FoodSource source : this.foodSources) {
            final Position p = source.getPosition();
            dropFoodPheromone(p, 1.0F);
        }
    }

    /**
     * Calls the selfContainedDispersePheromone function for each relevant pheromone type
     *  Also drops pheromones from all food sources
     */
    @SuppressWarnings({"unused", "PublicMethodNotExposedInInterface"})
    public void selfContainedDispersePheromones() {
        selfContainedDispersePheromone(this.foragingPheromone);
        selfContainedDispersePheromone(this.foodPheromone);

        dropFoodSourcePheromones();
    }

    /**
     * Self-contained implementation of disperse pheromones
     * not using dispersal policy interface
     */
    @SuppressWarnings("ArrayEquality")
    private void selfContainedDispersePheromone(final float[][] pheromone) {
        final float[][] tmpP = new float[this.width][this.height];
        for(int x = 0; x < this.width; x++) {
            for(int y = 0; y < this.height; y++) {
                float npl = 0.0F;
                final Position p = new Position((float) x, (float) y);
                if (!isObstacle(p)) {
                    npl = sumAdjacentCells(x, y, pheromone);
                    npl = ((1.0F - PHEROMONE_NEIGHBOUR_KEEP) * npl) / NUMBER_OF_NEIGHBOURS + (PHEROMONE_NEIGHBOUR_KEEP * (pheromone[x][y]));
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

    private float sumAdjacentCells(final int x0, final int y0, final float[][] matrix) {
        float sum = 0.0F;
        for (final int[] deltas : ADJACENT_CELL_DELTAS) {

                // Truncate coordinates to the closest inbound value
                final int x = truncate(0, this.width - 1, x0 + deltas[0]);
                final int y = truncate(0, this.height - 1, y0 + deltas[1]);

                sum += matrix[x][y];
        }
        return sum;
    }

    private static int truncate(final int min, final int max, final int val) {
        // If value is too big, round down to max value ELSE if value is too small round up to minimum value
        return ((val > max) ? max : Math.max(val, min));

    }

    @Override
    public void setObstacle(final Position p, final boolean add) { }

    @Override
    public void hitObstacle(final Position p, final float strength) { }

    @Override
    public String toString() {
        return "MyAntWorld{" +
                "width=" + this.width +
                ", height=" + this.height +
                ", foodSources=" + this.foodSources +
                '}';
    }
}