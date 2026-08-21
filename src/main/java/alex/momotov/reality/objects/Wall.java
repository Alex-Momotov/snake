package alex.momotov.reality.objects;

import alex.momotov.Ink;

public class Wall extends Obj {

    private static final String GLYPH = Ink.paint(Ink.SLATE, "+");

    @Override
    public String glyph(int tick) {
        return GLYPH;
    }
}
