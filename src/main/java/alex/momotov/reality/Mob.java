package alex.momotov.reality;

import alex.momotov.reality.objects.Creature;
import alex.momotov.reality.objects.Species;

/** A creature plus where it currently is. The board holds the token; this holds the life. */
public class Mob {

    public final Creature token;
    private Pos pos;

    public Mob(Species species, Pos pos) {
        this.token = new Creature(species);
        this.pos = pos;
    }

    public Species species() {
        return token.species;
    }

    public Pos pos() {
        return pos;
    }

    void moveTo(Pos target) {
        this.pos = target;
    }

    public boolean readyToMove(int tick) {
        return Math.floorMod(tick, species().moveEvery) == 0;
    }
}
