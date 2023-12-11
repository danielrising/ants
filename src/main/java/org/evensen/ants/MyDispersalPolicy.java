package org.evensen.ants;

public class MyDispersalPolicy implements DispersalPolicy{
    private static final float PHEROMONE_DROPOFF = 0.95F;
    private static final float PHEROMONE_NEIGHBOUR_KEEP = 0.5f;
    private static final float NUMBER_OF_NEIGHBOURS = 8.0F;
    private static final int[][] ADJACENT_CELL_DELTAS = {{-1,  1}, { 0,  1}, {1,  1},
                                                         {-1,  0}, /*CELL*/  {1,  0},
                                                         {-1, -1}, { 0, -1}, {1, -1}}; // Pls don't auto format :(

    @Override
    public float getDispersedValue(AntWorld w, Position p, boolean isForage){
        float npl = 0.0F;
        if (!w.isObstacle(p)) {
            // Calculate new pheromone strength
            float strength = (isForage) ? w.getForagingStrength(p) : w.getFoodStrength(p);
            npl = sumAdjacentCells(w, p, isForage);
            npl = ((1.0F - PHEROMONE_NEIGHBOUR_KEEP) * npl) / NUMBER_OF_NEIGHBOURS + (PHEROMONE_NEIGHBOUR_KEEP * (strength));
            npl *= PHEROMONE_DROPOFF;
        }
        return npl;
    }


    private float sumAdjacentCells(AntWorld w, Position p0, boolean isForage) {
        final int x0 = (int) p0.getX();
        final int y0 = (int) p0.getY();
        float sum = 0.0F;
        for (int[] deltas : ADJACENT_CELL_DELTAS) {

            // Truncate coordinates to the closest inbound value
            int x = truncate(0, w.getWidth() - 1, x0 + deltas[0]);
            int y = truncate(0, w.getHeight() - 1, y0 + deltas[1]);

            Position p = new Position(x, y);
            sum += (isForage) ? w.getForagingStrength(p) : w.getFoodStrength(p);
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
}