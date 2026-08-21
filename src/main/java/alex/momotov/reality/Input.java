package alex.momotov.reality;

/** Where key presses come from. Implemented by ui/, so the game never sees a keyboard. */
public interface Input {

    void start();

    /** The next key pressed, or null if nothing is pending. Never blocks. */
    Key poll();

    void stop();
}
