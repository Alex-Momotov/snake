package alex.momotov;

import alex.momotov.reality.Reality;


import java.io.IOException;

public class App {

    public static final int ROWS = 30;
    public static final int COLS = 23;
    public static final int STEP_MS = 121;
    public static final int FOOD_INTERVAL_MS = 2500;
    public static final boolean WALLS = true;
    public static final boolean SNAKE_COLLISION = true;

    public static void main(String[] args) throws IOException {
        Reality reality = new Reality();
    }

}
