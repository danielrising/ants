package org.evensen.ants;

public class MyDispersalPolicy implements DispersalPolicy {

    // Constants
    private static final float PHEROMONE_DROPOFF = 0.95F;
    private static final float PHEROMONE_NEIGHBOUR_KEEP = 0.5f;
    private static final float NUMBER_OF_NEIGHBOURS = 8.0f;
    private static final int[][] ADJACENT_CELL_DELTAS = {{-1,  1}, { 0,  1}, {1,  1},
                                                         {-1,  0}, /*CELL*/  {1,  0},
                                                         {-1, -1}, { 0, -1}, {1, -1}}; // Pls don't auto format :(

    @Override
    public float[] getDispersedValue(final AntWorld w, final Position p) {
        // Floored coordinates of position, for indexing
        final int x = p.floorX();
        final int y = p.floorY();

        // New pheromones levels for food and forage
        float nplFood = 0.0F;
        float nplForage = 0.0F;
        if (!w.isObstacle(p)) {
            // New levels for food pheromone
            nplFood = sumAdjacentCellsFood(x, y, w);
            nplFood = ((1.0F - PHEROMONE_NEIGHBOUR_KEEP) * nplFood) / NUMBER_OF_NEIGHBOURS + (PHEROMONE_NEIGHBOUR_KEEP * (w.getFoodStrength(p)));
            nplFood = nplFood * PHEROMONE_DROPOFF;

            // New levels for forage pheromone
            nplForage = sumAdjacentCellsForage(x, y, w);
            nplForage = ((1.0F - PHEROMONE_NEIGHBOUR_KEEP) * nplForage) / NUMBER_OF_NEIGHBOURS + (PHEROMONE_NEIGHBOUR_KEEP * (w.getForagingStrength(p)));
            nplForage = nplForage * PHEROMONE_DROPOFF;

        }

        return new float[]{nplFood, nplForage};

    }

    private static float sumAdjacentCellsFood(final int x0, final int y0, final AntWorld w) {
        float sum = 0.0F;
        for (final int[] deltas : ADJACENT_CELL_DELTAS) {

            // Truncate coordinates to the closest inbound value
            final int x = truncate(0, w.getWidth() - 1, x0 + deltas[0]);
            final int y = truncate(0, w.getHeight() - 1, y0 + deltas[1]);
            final Position p = new Position((float) x, (float) y);

            sum += w.getFoodStrength(p);
        }
        return sum;
    }

    private static float sumAdjacentCellsForage(final int x0, final int y0, final AntWorld w) {
        float sum = 0.0F;
        for (final int[] deltas : ADJACENT_CELL_DELTAS) {

            // Truncate coordinates to the closest inbound value
            final int x = truncate(0, w.getWidth() - 1, x0 + deltas[0]);
            final int y = truncate(0, w.getHeight() - 1, y0 + deltas[1]);
            final Position p = new Position((float) x, (float) y);

            sum += w.getForagingStrength(p);
        }
        return sum;
    }

    private static int truncate(final int min, final int max, final int val) {
        // If value is too big, round down to max value ELSE if value is too small round up to minimum value
        return ((val > max) ? max : Math.max(val, min));

    }
}