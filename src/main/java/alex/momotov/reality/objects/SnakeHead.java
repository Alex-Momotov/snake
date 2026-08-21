package alex.momotov.reality.objects;

import alex.momotov.Ink;
import alex.momotov.reality.Direction;

/** The head points where it is going. */
public class SnakeHead extends Obj {

    private final Direction facing;
    private int colour = Ink.LIME;

    public SnakeHead(Direction facing) {
        this.facing = facing;
    }

    public void tint(int colour) {
        this.colour = colour;
    }

    @Override
    public String glyph(int tick) {
        return Ink.bold(colour, arrow());
    }

    private String arrow() {
        switch (facing) {
            case UP:    return "^";
            case DOWN:  return "v";
            case LEFT:  return "<";
            default:    return ">";
        }
    }
}
