package alex.momotov;

/** 256-colour ANSI. The one place that knows how to tint a character. */
public final class Ink {

    public static final int CRIMSON = 160, RED = 196, ORANGE = 208, AMBER = 214, GOLD = 220;
    public static final int LIME = 118, GREEN = 46, EMERALD = 42, MOSS = 71;
    public static final int CYAN = 51, SKY = 117, BLUE = 33, INDIGO = 63;
    public static final int VIOLET = 141, PURPLE = 129, PINK = 213, MAGENTA = 201;
    public static final int WHITE = 231, SILVER = 250, GREY = 243, SLATE = 238, COAL = 236;

    /** Hues the snake's body cycles through as it grows. */
    private static final int[] RAINBOW = {
            46, 82, 118, 154, 190, 226, 220, 214, 208, 202,
            196, 197, 199, 201, 165, 129, 93, 57, 27, 33, 39, 45, 51, 50, 48};

    private static final String RESET = "\u001b[0m";

    private Ink() {
    }

    public static String paint(int colour, String text) {
        return "\u001b[38;5;" + colour + "m" + text + RESET;
    }

    public static String bold(int colour, String text) {
        return "\u001b[1;38;5;" + colour + "m" + text + RESET;
    }

    public static String dim(int colour, String text) {
        return "\u001b[2;38;5;" + colour + "m" + text + RESET;
    }

    /** Bright text on a solid block of colour, for banners. */
    public static String badge(int background, int foreground, String text) {
        return "\u001b[1;48;5;" + background + ";38;5;" + foreground + "m" + text + RESET;
    }

    public static int rainbow(int step) {
        return RAINBOW[Math.floorMod(step, RAINBOW.length)];
    }
}
