package org.evensen.ants;

public interface AntWorld {
    int getWidth();

    int getHeight();

    boolean isObstacle(Position p);

    void dropForagingPheromone(Position p, float amount);

    void dropFoodPheromone(Position p, float amount);

    void dropFood(Position p);

    void pickUpFood(Position p);

    float getDeadAntCount(Position p);

    float getForagingStrength(Position p);

    float getFoodStrength(Position p);

    boolean containsFood(Position p);

    long getFoodCount();
    
    boolean isHome(Position p);

    void dispersePheromones();

    void setObstacle(Position position, boolean value);

    void hitObstacle(Position position, float strength);
}
