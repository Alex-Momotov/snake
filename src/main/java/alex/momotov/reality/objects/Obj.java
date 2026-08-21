package alex.momotov.reality.objects;

/** Something that can sit in a cell and knows how it looks this frame. */
public abstract class Obj {

    /** The tick is passed in so an object can animate — pulse, blink, shimmer. */
    public abstract String glyph(int tick);

    @Override
    public String toString() {
        return glyph(0);
    }
}
