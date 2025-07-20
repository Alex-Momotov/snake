package alex.momotov;

import alex.momotov.reality.Reality;


import java.io.IOException;

public class App {

    public static final int ROWS = 30;
    public static final int COLS = 23;
    public static final int STEP_MS = 121;
    public static final int FOOD_INTERVAL_MS = 2_500;
    public static final boolean WALLS = false;

    public static void main(String[] args) throws IOException {
        Reality reality = new Reality();
    }

}
