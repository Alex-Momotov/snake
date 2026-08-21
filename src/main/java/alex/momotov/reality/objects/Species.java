package alex.momotov.reality.objects;

import alex.momotov.Ink;

/**
 * Everything that moves on its own, as data.
 *
 *  moveEvery  ticks between its steps (lower is faster)
 *  hostile    touching it is fatal
 *  phases     walks through walls
 */
public enum Species {

    SPIDER ("A", Ink.CRIMSON, Behaviour.HUNT,   3, true,  false, 0,   0),
    WASP   ("W", Ink.AMBER,   Behaviour.DRIFT,  2, true,  false, 0,   0),
    WRAITH ("~", Ink.INDIGO,  Behaviour.HUNT,   5, true,  true,  0,   0),
    MOUSE  ("m", Ink.SILVER,  Behaviour.FLEE,   2, false, false, 2,  45),
    HERMIT ("?", Ink.SKY,     Behaviour.DRIFT,  7, false, false, 0,  30);

    public final String mark;
    public final int colour;
    public final Behaviour behaviour;
    public final int moveEvery;
    public final boolean hostile;
    public final boolean phases;
    public final int growth;
    public final int points;

    Species(String mark, int colour, Behaviour behaviour, int moveEvery,
            boolean hostile, boolean phases, int growth, int points) {
        this.mark = mark;
        this.colour = colour;
        this.behaviour = behaviour;
        this.moveEvery = moveEvery;
        this.hostile = hostile;
        this.phases = phases;
        this.growth = growth;
        this.points = points;
    }
}
