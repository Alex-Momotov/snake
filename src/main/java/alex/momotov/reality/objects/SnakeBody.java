package alex.momotov.reality.objects;

import alex.momotov.Ink;

/** One segment. Each carries its own hue, so a long snake is a moving rainbow. */
public class SnakeBody extends Obj {

    private final String normal;
    private final String shielded;

    public SnakeBody(int hue) {
        this.normal = Ink.bold(Ink.rainbow(hue), "o");
        this.shielded = Ink.bold(Ink.GOLD, "o");
    }

    @Override
    public String glyph(int tick) {
        return normal;
    }

    public String glyphShielded() {
        return shielded;
    }
}
