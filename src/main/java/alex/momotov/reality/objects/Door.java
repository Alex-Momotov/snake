package alex.momotov.reality.objects;

import alex.momotov.Ink;

/** A gap in the wall to somewhere else. Pulses so it is hard to miss. */
public class Door extends Obj implements Animated {

    private static final String[] PULSE = {
            Ink.bold(Ink.GOLD, "D"),
            Ink.bold(Ink.AMBER, "D"),
            Ink.bold(Ink.ORANGE, "D"),
            Ink.bold(Ink.AMBER, "D")};

    @Override
    public String glyph(int tick) {
        return PULSE[Math.floorMod(tick / 3, PULSE.length)];
    }
}
