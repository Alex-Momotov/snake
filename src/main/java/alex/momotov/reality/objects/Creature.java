package alex.momotov.reality.objects;

import alex.momotov.Ink;

/** A creature sitting on the board. Hostile ones twitch, so they read as alive. */
public class Creature extends Obj implements Animated {

    public final Species species;

    public Creature(Species species) {
        this.species = species;
    }

    @Override
    public String glyph(int tick) {
        if (!species.hostile)
            return Ink.bold(species.colour, species.mark);
        boolean bright = Math.floorMod(tick / 4, 2) == 0;
        return bright ? Ink.bold(species.colour, species.mark)
                      : Ink.paint(species.colour, species.mark);
    }
}
