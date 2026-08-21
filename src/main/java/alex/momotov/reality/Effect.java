package alex.momotov.reality;

import alex.momotov.Ink;

/** A temporary power the snake can be under. */
public enum Effect {

    HASTE("HASTE", Ink.ORANGE),
    DRAG("DRAG", Ink.CYAN),
    SHIELD("SHIELD", Ink.GOLD),
    PHASE("PHASE", Ink.VIOLET);

    public final String label;
    public final int colour;

    Effect(String label, int colour) {
        this.label = label;
        this.colour = colour;
    }
}
