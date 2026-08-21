package alex.momotov.reality.objects;

import alex.momotov.Ink;
import alex.momotov.reality.Effect;

/**
 * Everything edible, as data. Adding a new one is a single line here.
 *
 *  growth   segments gained (negative sheds them)
 *  points   added to the score
 *  effect   temporary power granted, or null
 *  life     ticks before it rots away, 0 = stays forever
 *  weight   relative chance of being the next thing to appear
 */
public enum Treat {

    APPLE  ("@", Ink.RED,     1,  10, null,          0,   46, "APPLE"),
    PLUM   ("p", Ink.MAGENTA, 1,  15, null,          0,   14, "PLUM"),
    MELON  ("%", Ink.EMERALD, 3,  35, null,        260,   13, "MELON +3"),
    ROT    ("*", Ink.PURPLE, -2, -20, null,        320,   10, "URGH"),
    PEPPER ("!", Ink.ORANGE,  1,  25, Effect.HASTE, 220,   6, "HASTE"),
    FROST  ("#", Ink.CYAN,    1,  25, Effect.DRAG,  220,   5, "DRAG"),
    STAR   ("$", Ink.GOLD,    2,  60, Effect.SHIELD,150,   4, "SHIELD"),
    ELIXIR ("&", Ink.VIOLET,  1,  40, Effect.PHASE, 170,   4, "PHASE"),
    GOLDEN ("8", Ink.AMBER,   5, 150, null,          90,   2, "JACKPOT");

    public final String mark;
    public final int colour;
    public final int growth;
    public final int points;
    public final Effect effect;
    public final int life;
    public final int weight;
    public final String shout;

    Treat(String mark, int colour, int growth, int points, Effect effect,
          int life, int weight, String shout) {
        this.mark = mark;
        this.colour = colour;
        this.growth = growth;
        this.points = points;
        this.effect = effect;
        this.life = life;
        this.weight = weight;
        this.shout = shout;
    }

    public boolean perishable() {
        return life > 0;
    }

    public static int totalWeight() {
        int total = 0;
        for (Treat treat : values())
            total += treat.weight;
        return total;
    }

    /** Picks a treat in proportion to its weight. */
    public static Treat roll(int weightedIndex) {
        int running = 0;
        for (Treat treat : values()) {
            running += treat.weight;
            if (weightedIndex < running)
                return treat;
        }
        return APPLE;
    }
}
