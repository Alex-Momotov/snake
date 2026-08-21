package alex.momotov.reality.objects;

import alex.momotov.Ink;

/** A treat sitting on the board. Perishable ones flash as their time runs out. */
public class Edible extends Obj implements Animated {

    private static final int PANIC_TICKS = 45;      // when it starts flashing

    public final Treat treat;
    private int expiresAt;

    public Edible(Treat treat) {
        this.treat = treat;
    }

    public void expiresAt(int tick) {
        this.expiresAt = tick;
    }

    public boolean expired(int tick) {
        return treat.perishable() && tick >= expiresAt;
    }

    @Override
    public String glyph(int tick) {
        if (!treat.perishable() || expiresAt - tick > PANIC_TICKS)
            return Ink.bold(treat.colour, treat.mark);
        // running out: flash, and flash faster the closer it gets
        int urgency = Math.max(1, (expiresAt - tick) / 12);
        boolean bright = Math.floorMod(tick / urgency, 2) == 0;
        return bright ? Ink.bold(Ink.WHITE, treat.mark) : Ink.dim(treat.colour, treat.mark);
    }
}
